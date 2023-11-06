package com.ford.labs.retroquest.retro;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RetroTest {

    private final RetroType retroType = new RetroType(
            "A retro",
            List.of(
                    new Column("Column 1", "Description 1"),
                    new Column("Column 2", "Description 2"),
                    new Column("Column 3", "Description 3")
            ),
            "Retro Description"
    );

    @Test
    void from_SetsIdToNull() {
        var actual = Retro.from(retroType, null);
        assertThat(actual.getId()).isNull();
    }

    @Test
    void from_shouldInitializeThoughtsAsAnEmptyList() {
        var actual = Retro.from(retroType, null);
        assertThat(actual.getThoughts()).isEmpty();
    }
    
    @Test
    void from_shouldInitializeCreateTimeAsNull() {
        var actual = Retro.from(retroType, null);
        assertThat(actual.getCreatedAt()).isNull();
    }

    @Test
    void from_shouldInitializeRetroAsActive() {
        var actual = Retro.from(retroType, null);
        assertThat(actual.isActive()).isTrue();
    }

    @Test
    void from_shouldContainRetroTypeDescription() {
        var actual = Retro.from(retroType, null);
        assertThat(actual.getDescription()).isEqualTo(retroType.description());
    }

    @Test
    void from_shouldContainRetroTypeColumns() {
        var actual = Retro.from(retroType, null);
        assertThat(actual.getColumns()).containsExactlyElementsOf(retroType.columns());
    }

    @Test
    void from_shouldContainTeamId() {
        var teamId = UUID.randomUUID();
        var actual = Retro.from(retroType, teamId);
        assertThat(actual.getTeamId()).isEqualTo(teamId);
    }
}
