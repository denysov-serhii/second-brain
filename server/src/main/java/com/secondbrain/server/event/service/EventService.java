package com.secondbrain.server.event.service;

import com.secondbrain.server.event.api.CreateEventRequest;
import com.secondbrain.server.event.api.EventResponse;
import com.secondbrain.server.event.domain.LogEvent;
import com.secondbrain.server.event.repository.LogEventRepository;
import com.secondbrain.server.stream.domain.Stream;
import com.secondbrain.server.stream.repository.StreamRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

    private final LogEventRepository logEventRepository;
    private final StreamRepository streamRepository;

    public EventService(LogEventRepository logEventRepository, StreamRepository streamRepository) {
        this.logEventRepository = logEventRepository;
        this.streamRepository = streamRepository;
    }

    public List<EventResponse> getEvents(String email, UUID streamId) {
        verifyStreamOwnership(email, streamId);
        return logEventRepository.findAllByStreamIdOrderByTimestampAsc(streamId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EventResponse createEvent(String email, UUID streamId, CreateEventRequest request) {
        Stream stream = verifyStreamOwnership(email, streamId);
        LogEvent event = new LogEvent();
        event.setStream(stream);
        event.setTimestamp(request.timestamp());
        event.setLevel(request.level());
        event.setMessage(request.message());
        return toResponse(logEventRepository.save(event));
    }

    private Stream verifyStreamOwnership(String email, UUID streamId) {
        return streamRepository.findByIdAndUserEmail(streamId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stream not found"));
    }

    private EventResponse toResponse(LogEvent event) {
        return new EventResponse(
                event.getId(),
                event.getStream().getId(),
                event.getTimestamp(),
                event.getLevel(),
                event.getMessage(),
                event.getCreatedAt()
        );
    }
}
