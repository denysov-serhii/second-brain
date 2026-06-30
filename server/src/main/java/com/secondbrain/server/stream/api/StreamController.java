package com.secondbrain.server.stream.api;

import com.secondbrain.server.stream.service.StreamService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streams")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping
    public List<StreamResponse> getStreams(@AuthenticationPrincipal UserDetails principal) {
        return streamService.getStreams(principal.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StreamResponse createStream(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateStreamRequest request) {
        return streamService.createStream(principal.getUsername(), request);
    }
}
