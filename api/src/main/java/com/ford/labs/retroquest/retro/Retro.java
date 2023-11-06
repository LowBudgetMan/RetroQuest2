package com.ford.labs.retroquest.retro;

import com.ford.labs.retroquest.thought.Thought;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Retro {
    @Id
    @GeneratedValue
    private UUID id;
    private String description;
    private List<Thought> thoughts;
    private List<Column> columns;
    private UUID teamId;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private boolean active;

    public static Retro from(RetroType type, UUID teamId) {
        return new Retro(
                null,
                type.description(),
                List.of(),
                type.columns(),
                teamId,
                null,
                true
        );
    }
}
