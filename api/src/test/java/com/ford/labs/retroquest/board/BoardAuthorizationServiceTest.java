package com.ford.labs.retroquest.board;

import com.ford.labs.retroquest.security.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoardAuthorizationServiceTest {

    private final AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
    private final BoardService mockBoardService = mock(BoardService.class);
    private final BoardAuthorizationService boardAuthorizationService = new BoardAuthorizationService(mockAuthorizationService, mockBoardService);

    @Test
    void requestIsAuthorized_WhenUserAndBoardBothOnTeam_ReturnTrue() {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        var authentication = mock(Authentication.class);
        var expectedBoard = new Retro(boardId, teamId.toString(), LocalDate.now(), List.of(), List.of());
        when(mockAuthorizationService.requestIsAuthorized(authentication, teamId)).thenReturn(true);
        when(mockBoardService.getArchivedRetroForTeam(teamId.toString(), boardId)).thenReturn(expectedBoard);
        assertThat(boardAuthorizationService.requestIsAuthorized(authentication, teamId, boardId)).isTrue();
    }

    @Test
    void requestIsAuthorized_WhenUserNotOnTeam_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        var authentication = mock(Authentication.class);
        var expectedBoard = new Retro(boardId, teamId.toString(), LocalDate.now(), List.of(), List.of());
        when(mockAuthorizationService.requestIsAuthorized(authentication, teamId)).thenReturn(false);
        when(mockBoardService.getArchivedRetroForTeam(teamId.toString(), boardId)).thenReturn(expectedBoard);
        assertThat(boardAuthorizationService.requestIsAuthorized(authentication, teamId, boardId)).isFalse();
    }

    @Test
    void requestIsAuthorized_WhenBoardNotOnTeam_ReturnsFalse() {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        var authentication = mock(Authentication.class);
        when(mockAuthorizationService.requestIsAuthorized(authentication, teamId)).thenReturn(true);
        when(mockBoardService.getArchivedRetroForTeam(teamId.toString(), boardId)).thenReturn(null);
        assertThat(boardAuthorizationService.requestIsAuthorized(authentication, teamId, boardId)).isFalse();
    }
}