package com.secondbrain.server.logs.service;

import com.secondbrain.server.logs.domain.LogEntry;
import com.secondbrain.server.logs.repository.LogEntryRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class LogQueryService {

    private final LogEntryRepository logEntryRepository;

    public LogQueryService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public List<LogEntry> getRecentLogs(int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return logEntryRepository.findAll(pageRequest).getContent();
    }
}
