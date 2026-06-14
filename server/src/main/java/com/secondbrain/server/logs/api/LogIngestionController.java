package com.secondbrain.server.logs.api;

import com.secondbrain.server.logs.domain.LogEntry;
import com.secondbrain.server.logs.domain.SourceDevice;
import com.secondbrain.server.logs.service.IngestionProcessingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/logs")
public class LogIngestionController {

    private final IngestionProcessingService ingestionProcessingService;

    public LogIngestionController(IngestionProcessingService ingestionProcessingService) {
        this.ingestionProcessingService = ingestionProcessingService;
    }

    @PostMapping(
            path = "/ingest",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LogIngestionResponse ingest(
            @RequestParam(value = "raw_content", required = false) String rawContent,
            @RequestParam("source_device") SourceDevice sourceDevice,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        LogEntry savedEntry = ingestionProcessingService.process(rawContent, sourceDevice, file);
        return new LogIngestionResponse(
                savedEntry.getId(),
                savedEntry.getRawContent(),
                savedEntry.getExtractedText(),
                savedEntry.getSummary(),
                savedEntry.getLogType(),
                savedEntry.getSourceDevice(),
                savedEntry.getCreatedAt()
        );
    }
}
