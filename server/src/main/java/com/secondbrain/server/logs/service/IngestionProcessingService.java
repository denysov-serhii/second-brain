package com.secondbrain.server.logs.service;

import com.secondbrain.server.logs.domain.LogEntry;
import com.secondbrain.server.logs.domain.LogType;
import com.secondbrain.server.logs.domain.SourceDevice;
import com.secondbrain.server.logs.repository.LogEntryRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestionProcessingService {

    private static final int EMBEDDING_DIMENSION = 1536;
    private static final String DEFAULT_SUMMARY = "Captured a personal log entry.";
    private static final String STRUCTURED_PROMPT = """
            You are classifying life-log text into one of these enum values:
            IDEA, TASK_INPUT, MEETING_NOTE, INSIGHT, PERSONAL.
            Return strict JSON with keys:
            {
              "log_type": "IDEA|TASK_INPUT|MEETING_NOTE|INSIGHT|PERSONAL",
              "summary": "single-sentence summary"
            }
            Text:
            %s
            """;

    private final LogEntryRepository logEntryRepository;

    public IngestionProcessingService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
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
        entry.setEmbedding(generateMockEmbedding(extractedText));
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
        return "[WHISPER_STUB] " + safe(fallbackText);
    }

    private String visionExtractText(MultipartFile imageFile, String fallbackText) {
        return "[VISION_OCR_STUB] " + safe(fallbackText);
    }

    private ClassificationResult classifyAndSummarize(String extractedText) {
        String prompt = STRUCTURED_PROMPT.formatted(extractedText);
        String llmJson = mockLlmCompletion(prompt);
        String llmLogType = extractLogTypeFromMock(llmJson);
        String llmSummary = extractSummaryFromMock(llmJson);
        String safeText = safe(extractedText);
        String classificationText = safeText.toLowerCase(Locale.ROOT);

        LogType logType = parseLogType(llmLogType);
        if (logType == null) {
            if (classificationText.contains("todo") || classificationText.contains("task")) {
                logType = LogType.TASK_INPUT;
            } else if (classificationText.contains("meeting")) {
                logType = LogType.MEETING_NOTE;
            } else if (classificationText.contains("idea")) {
                logType = LogType.IDEA;
            } else if (classificationText.contains("insight") || classificationText.contains("learned")) {
                logType = LogType.INSIGHT;
            } else {
                logType = LogType.PERSONAL;
            }
        }

        String summary = llmSummary.isBlank()
                ? DEFAULT_SUMMARY
                : llmSummary.trim().replaceAll("\\s+", " ");
        if (summary.length() > 140) {
            summary = summary.substring(0, 137) + "...";
        }
        return new ClassificationResult(logType, summary);
    }

    private String mockLlmCompletion(String prompt) {
        String normalizedPrompt = safe(prompt).toLowerCase(Locale.ROOT);
        String mockType = normalizedPrompt.contains("meeting") ? "MEETING_NOTE" : "PERSONAL";
        return """
                {"log_type":"%s","summary":"%s"}
                """.formatted(mockType, DEFAULT_SUMMARY);
    }

    private String extractSummaryFromMock(String llmJson) {
        int keyIndex = llmJson.indexOf("\"summary\":\"");
        if (keyIndex < 0) {
            return DEFAULT_SUMMARY;
        }
        int start = keyIndex + "\"summary\":\"".length();
        int end = llmJson.indexOf("\"", start);
        if (end < 0) {
            return DEFAULT_SUMMARY;
        }
        String value = llmJson.substring(start, end).trim();
        return value.isEmpty() ? DEFAULT_SUMMARY : value;
    }

    private String extractLogTypeFromMock(String llmJson) {
        int keyIndex = llmJson.indexOf("\"log_type\":\"");
        if (keyIndex < 0) {
            return "";
        }
        int start = keyIndex + "\"log_type\":\"".length();
        int end = llmJson.indexOf("\"", start);
        if (end < 0) {
            return "";
        }
        return llmJson.substring(start, end).trim();
    }

    private LogType parseLogType(String value) {
        try {
            return LogType.valueOf(safe(value).trim());
        } catch (IllegalArgumentException ex) {
            return null;
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record ClassificationResult(LogType logType, String summary) {
    }
}
