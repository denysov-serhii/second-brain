package com.secondbrain.server.logs.api;

import com.secondbrain.server.logs.service.LogQueryService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private static final int MAX_PAGE_SIZE = 200;

    private final LogQueryService logQueryService;

    public LogController(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LogIngestionResponse> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        return logQueryService.getRecentLogs(page, safeSize).stream()
                .map(entry -> new LogIngestionResponse(
                        entry.getId(),
                        entry.getRawContent(),
                        entry.getExtractedText(),
                        entry.getSummary(),
                        entry.getLogType(),
                        entry.getSourceDevice(),
                        entry.getCreatedAt()
                ))
                .toList();
    }
}
