package com.ford.labs.retroquest.board;

import com.ford.labs.retroquest.security.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BoardAuthorizationService {

    private final AuthorizationService authorizationService;
    private final BoardService boardService;

    public BoardAuthorizationService(AuthorizationService authorizationService, BoardService boardService) {
        this.authorizationService = authorizationService;
        this.boardService = boardService;
    }

    public boolean requestIsAuthorized(Authentication authentication, UUID teamId, Long boardId) {
        return authorizationService.requestIsAuthorized(authentication, teamId) && isBoardPartOfTeam(teamId, boardId);
    }

    private boolean isBoardPartOfTeam(UUID teamId, Long boardId) {
        var maybeBoard = boardService.getArchivedRetroForTeam(teamId.toString(), boardId);
        return maybeBoard != null;
    }

}
