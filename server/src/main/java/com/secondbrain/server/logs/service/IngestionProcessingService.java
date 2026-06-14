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
        mockLlmCompletion(prompt);
        String normalized = safe(extractedText).toLowerCase(Locale.ROOT);

        LogType logType;
        if (normalized.contains("todo") || normalized.contains("task")) {
            logType = LogType.TASK_INPUT;
        } else if (normalized.contains("meeting")) {
            logType = LogType.MEETING_NOTE;
        } else if (normalized.contains("idea")) {
            logType = LogType.IDEA;
        } else if (normalized.contains("insight") || normalized.contains("learned")) {
            logType = LogType.INSIGHT;
        } else {
            logType = LogType.PERSONAL;
        }

        String normalizedText = safe(extractedText);
        String summary = normalizedText.isBlank()
                ? "Captured a personal log entry."
                : normalizedText.trim().replaceAll("\\s+", " ");
        if (summary.length() > 140) {
            summary = summary.substring(0, 137) + "...";
        }
        return new ClassificationResult(logType, summary);
    }

    private String mockLlmCompletion(String prompt) {
        return """
                {"log_type":"PERSONAL","summary":"Captured a personal log entry."}
                """;
    }

    private float[] generateMockEmbedding(String text) {
        float[] vector = new float[1536];
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
