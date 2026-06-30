package com.secondbrain.server.event.repository;

import com.secondbrain.server.event.domain.LogEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEventRepository extends JpaRepository<LogEvent, UUID> {

    List<LogEvent> findAllByStreamIdOrderByTimestampAsc(UUID streamId);
}
