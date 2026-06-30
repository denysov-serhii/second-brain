package com.secondbrain.server.stream.repository;

import com.secondbrain.server.stream.domain.Stream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamRepository extends JpaRepository<Stream, UUID> {

    List<Stream> findAllByUserEmail(String email);

    Optional<Stream> findByIdAndUserEmail(UUID id, String email);
}
