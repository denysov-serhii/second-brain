package com.secondbrain.server.event.domain;

import com.secondbrain.server.stream.domain.Stream;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stream_events")
public class LogEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 10)
    private String level;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }

    public Stream getStream() { return stream; }

    public void setStream(Stream stream) { this.stream = stream; }

    public Instant getTimestamp() { return timestamp; }

    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getLevel() { return level; }

    public void setLevel(String level) { this.level = level; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
}
