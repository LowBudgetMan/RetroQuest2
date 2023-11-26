package com.ford.labs.retroquest.team;

import com.ford.labs.retroquest.security.AuthorizationService;
import com.ford.labs.retroquest.thought.Thought;
import com.ford.labs.retroquest.thought.ThoughtService;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThoughtAuthorizationServiceTest {

    private final AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
    private final ThoughtService mockThoughtService = mock(ThoughtService.class);
    private final ThoughtAuthorizationService thoughtAuthorizationService = new ThoughtAuthorizationService(mockAuthorizationService, mockThoughtService);

    @Test
    void isAuthorizedRequest_WhenUserOnTeamAndThoughtBelongsToTeam_ReturnsTrue() {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var thought = new Thought(thoughtId, "Message", 0, false, teamId.toString(), 10L, 100L);
        when(mockAuthorizationService.requestIsAuthorized(null, teamId)).thenReturn(true);
        when(mockThoughtService.getThought(thoughtId)).thenReturn(Optional.of(thought));
        assertThat(thoughtAuthorizationService.requestIsAuthorized(null, teamId, thoughtId)).isTrue();
    }

    @Test
    void isAuthorizedRequest_WhenUserNotOnTeam_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        when(mockAuthorizationService.requestIsAuthorized(null, teamId)).thenReturn(false);
        assertThat(thoughtAuthorizationService.requestIsAuthorized(null, teamId, thoughtId)).isFalse();
    }

    @Test
    void isAuthorizedRequest_WhenThoughtDoesNotExist_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        when(mockAuthorizationService.requestIsAuthorized(null, teamId)).thenReturn(false);
        when(mockThoughtService.getThought(thoughtId)).thenReturn(Optional.empty());
        assertThat(thoughtAuthorizationService.requestIsAuthorized(null, teamId, thoughtId)).isFalse();
    }

    @Test
    void isAuthorizedRequest_WhenThoughtIsNotOnTeam_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var thought = new Thought(thoughtId, "Message", 0, false, "different team ID", 10L, 100L);
        when(mockAuthorizationService.requestIsAuthorized(null, teamId)).thenReturn(false);
        when(mockThoughtService.getThought(thoughtId)).thenReturn(Optional.of(thought));
        assertThat(thoughtAuthorizationService.requestIsAuthorized(null, teamId, thoughtId)).isFalse();
    }
}