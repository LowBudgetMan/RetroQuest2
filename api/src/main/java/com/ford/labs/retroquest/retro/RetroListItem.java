package com.ford.labs.retroquest.retro;

import com.ford.labs.retroquest.thought.Thought;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RetroListItem(
        UUID id,
        String description,
        LocalDateTime createdAt,
        boolean active
) {
}
