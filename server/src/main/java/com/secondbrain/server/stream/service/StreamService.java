package com.secondbrain.server.stream.service;

import com.secondbrain.server.auth.domain.User;
import com.secondbrain.server.auth.repository.UserRepository;
import com.secondbrain.server.stream.api.CreateStreamRequest;
import com.secondbrain.server.stream.api.StreamResponse;
import com.secondbrain.server.stream.domain.Stream;
import com.secondbrain.server.stream.repository.StreamRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StreamService {

    private final StreamRepository streamRepository;
    private final UserRepository userRepository;

    public StreamService(StreamRepository streamRepository, UserRepository userRepository) {
        this.streamRepository = streamRepository;
        this.userRepository = userRepository;
    }

    public List<StreamResponse> getStreams(String email) {
        return streamRepository.findAllByUserEmail(email).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StreamResponse createStream(String email, CreateStreamRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Stream stream = new Stream();
        stream.setUser(user);
        stream.setName(request.name());
        return toResponse(streamRepository.save(stream));
    }

    private StreamResponse toResponse(Stream stream) {
        return new StreamResponse(stream.getId(), stream.getName(), stream.getCreatedAt());
    }
}
