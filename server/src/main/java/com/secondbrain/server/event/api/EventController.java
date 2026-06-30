package com.secondbrain.server.event.api;

import com.secondbrain.server.event.service.EventService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streams/{streamId}/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> getEvents(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID streamId) {
        return eventService.getEvents(principal.getUsername(), streamId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID streamId,
            @Valid @RequestBody CreateEventRequest request) {
        return eventService.createEvent(principal.getUsername(), streamId, request);
    }
}
