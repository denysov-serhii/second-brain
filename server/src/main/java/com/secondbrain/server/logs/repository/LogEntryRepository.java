package com.secondbrain.server.logs.repository;

import com.secondbrain.server.logs.domain.LogEntry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntry, UUID> {
}
