package com.ford.labs.retroquest.column;

import com.ford.labs.retroquest.exception.ColumnNotFoundException;
import com.ford.labs.retroquest.security.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ColumnAuthorizationServiceTest {

    private final AuthorizationService authorizationService = mock(AuthorizationService.class);
    private final ColumnService columnService = mock(ColumnService.class);
    private final ColumnAuthorizationService columnAuthorizationService = new ColumnAuthorizationService(authorizationService, columnService);

    @Test
    void requestIsAuthorized_WhenColumnAndUserOnTeam_ReturnsTrue() {
        var authentication = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        var columnId = 1L;
        var column = new Column(columnId, "topic", "title", teamId.toString());

        when(authorizationService.requestIsAuthorized(authentication, teamId)).thenReturn(true);
        when(columnService.fetchColumn(teamId.toString(), 1L)).thenReturn(column);

        assertThat(columnAuthorizationService.requestIsAuthorized(authentication, teamId, columnId)).isTrue();
    }

    @Test
    void requestIsAuthorized_WhenColumnNotOnTeam_ReturnsFalse() {
        var authentication = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        var columnId = 1L;

        when(authorizationService.requestIsAuthorized(authentication, teamId)).thenReturn(true);
        when(columnService.fetchColumn(teamId.toString(), 1L)).thenThrow(new ColumnNotFoundException());

        assertThat(columnAuthorizationService.requestIsAuthorized(authentication, teamId, columnId)).isFalse();
    }

    @Test
    void requestIsAuthorized_WhenUserNotOnTeam_ReturnsFalse() {
        var authentication = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        var columnId = 1L;

        when(authorizationService.requestIsAuthorized(authentication, teamId)).thenReturn(false);

        assertThat(columnAuthorizationService.requestIsAuthorized(authentication, teamId, columnId)).isFalse();
    }
}