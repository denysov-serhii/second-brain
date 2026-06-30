package com.secondbrain.server.logs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secondbrain.server.logs.domain.LogEntry;
import com.secondbrain.server.logs.domain.LogType;
import com.secondbrain.server.logs.domain.SourceDevice;
import com.secondbrain.server.logs.repository.LogEntryRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(IngestionProcessingService.class);
    private static final int EMBEDDING_DIMENSION = 1536;
    private static final String DEFAULT_SUMMARY = "Captured a personal log entry.";
    private static final String STRUCTURED_PROMPT = """
            You are classifying life-log text into one of these enum values:
            IDEA, TASK_INPUT, MEETING_NOTE, INSIGHT, PERSONAL.
            Return ONLY strict JSON with no extra text, using these keys:
            {
              "log_type": "IDEA|TASK_INPUT|MEETING_NOTE|INSIGHT|PERSONAL",
              "summary": "single-sentence summary"
            }
            Text:
            %s
            """;

    private final LogEntryRepository logEntryRepository;
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final OpenAiAudioTranscriptionModel audioTranscriptionModel;
    private final ObjectMapper objectMapper;

    public IngestionProcessingService(
            LogEntryRepository logEntryRepository,
            ChatClient.Builder chatClientBuilder,
            EmbeddingModel embeddingModel,
            OpenAiAudioTranscriptionModel audioTranscriptionModel,
            ObjectMapper objectMapper) {
        this.logEntryRepository = logEntryRepository;
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
        this.audioTranscriptionModel = audioTranscriptionModel;
        this.objectMapper = objectMapper;
    }

    public LogEntry process(String rawContent, SourceDevice sourceDevice, MultipartFile file) {
        String extractedText = extractText(rawContent, file);
        ClassificationResult result = classifyAndSummarize(extractedText);

        LogEntry entry = new LogEntry();
        entry.setRawContent(rawContent);
        entry.setExtractedText(extractedText);
        entry.setSummary(result.summary());
        entry.setLogType(result.logType());
        entry.setSourceDevice(sourceDevice);
        entry.setEmbedding(generateEmbedding(extractedText));
        return logEntryRepository.save(entry);
    }

    private String extractText(String rawContent, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return safe(rawContent);
        }
        String contentType = safe(file.getContentType()).toLowerCase(Locale.ROOT);
        if (contentType.startsWith("audio/")) {
            return whisperTranscribe(file, rawContent);
        }
        if (contentType.startsWith("image/")) {
            return visionExtractText(file, rawContent);
        }
        return safe(rawContent);
    }

    private String whisperTranscribe(MultipartFile audioFile, String fallbackText) {
        try {
            byte[] bytes = audioFile.getBytes();
            String filename = safe(audioFile.getOriginalFilename());
            if (filename.isEmpty()) {
                filename = "audio.mp3";
            }
            final String finalFilename = filename;
            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return finalFilename;
                }
            };
            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource);
            return audioTranscriptionModel.call(prompt).getResult().getOutput();
        } catch (Exception e) {
            log.warn("Whisper transcription failed, using fallback text", e);
            return safe(fallbackText);
        }
    }

    private String visionExtractText(MultipartFile imageFile, String fallbackText) {
        try {
            byte[] bytes = imageFile.getBytes();
            String contentType = safe(imageFile.getContentType());
            if (contentType.isEmpty()) {
                contentType = "image/jpeg";
            }
            ByteArrayResource imageResource = new ByteArrayResource(bytes);
            MimeType mimeType = MimeType.valueOf(contentType);
            String response = chatClient.prompt()
                    .user(u -> u
                            .text("Extract all visible text from this image. Return only the extracted text, nothing else.")
                            .media(mimeType, imageResource))
                    .call()
                    .content();
            return safe(response);
        } catch (Exception e) {
            log.warn("Vision text extraction failed, using fallback text", e);
            return safe(fallbackText);
        }
    }

    private ClassificationResult classifyAndSummarize(String extractedText) {
        try {
            String prompt = STRUCTURED_PROMPT.formatted(extractedText);
            String llmJson = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return parseClassificationResult(safe(llmJson), extractedText);
        } catch (Exception e) {
            log.warn("LLM classification failed, using keyword heuristics", e);
            return heuristicClassify(extractedText);
        }
    }

    private ClassificationResult parseClassificationResult(String llmJson, String extractedText) {
        try {
            String json = llmJson.trim();
            if (json.startsWith("```")) {
                int start = json.indexOf('\n') + 1;
                int end = json.lastIndexOf("```");
                if (end > start) {
                    json = json.substring(start, end).trim();
                }
            }
            var node = objectMapper.readTree(json);
            String rawType = node.path("log_type").asText("");
            String rawSummary = node.path("summary").asText("");

            LogType logType = parseLogType(rawType);
            if (logType == null) {
                logType = heuristicClassify(extractedText).logType();
            }

            String summary = rawSummary.isBlank()
                    ? DEFAULT_SUMMARY
                    : rawSummary.trim().replaceAll("\\s+", " ");
            if (summary.length() > 140) {
                summary = summary.substring(0, 137) + "...";
            }
            return new ClassificationResult(logType, summary);
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON response, using heuristics. Response was: {}", llmJson, e);
            return heuristicClassify(extractedText);
        }
    }

    private ClassificationResult heuristicClassify(String extractedText) {
        String lower = safe(extractedText).toLowerCase(Locale.ROOT);
        LogType logType;
        if (lower.contains("todo") || lower.contains("task")) {
            logType = LogType.TASK_INPUT;
        } else if (lower.contains("meeting")) {
            logType = LogType.MEETING_NOTE;
        } else if (lower.contains("idea")) {
            logType = LogType.IDEA;
        } else if (lower.contains("insight") || lower.contains("learned")) {
            logType = LogType.INSIGHT;
        } else {
            logType = LogType.PERSONAL;
        }
        return new ClassificationResult(logType, DEFAULT_SUMMARY);
    }

    private float[] generateEmbedding(String text) {
        try {
            return embeddingModel.embed(safe(text));
        } catch (Exception e) {
            log.warn("Embedding generation failed, using mock embedding", e);
            return generateMockEmbedding(text);
        }
    }

    private float[] generateMockEmbedding(String text) {
        float[] vector = new float[EMBEDDING_DIMENSION];
        int hash = safe(text).hashCode();
        for (int i = 0; i < vector.length; i++) {
            vector[i] = ((hash + i) % 1000) / 1000.0f;
        }
        return vector;
    }

    private LogType parseLogType(String value) {
        try {
            return LogType.valueOf(safe(value).trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record ClassificationResult(LogType logType, String summary) {
    }
}
