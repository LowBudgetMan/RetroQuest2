package com.ford.labs.retroquest.column;

import com.ford.labs.retroquest.exception.ColumnNotFoundException;
import com.ford.labs.retroquest.security.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ColumnAuthorizationService {

    private final AuthorizationService authorizationService;
    private final ColumnService columnService;

    public ColumnAuthorizationService(AuthorizationService authorizationService, ColumnService columnService) {
        this.authorizationService = authorizationService;
        this.columnService = columnService;
    }

    public boolean requestIsAuthorized(Authentication authentication, UUID teamId, Long columnId) {
        return authorizationService.requestIsAuthorized(authentication, teamId) && isColumnPartOfTeam(teamId, columnId);
    }

    private boolean isColumnPartOfTeam(UUID teamId, Long columnId) {
        try {
            columnService.fetchColumn(teamId.toString(), columnId);
            return true;
        } catch (ColumnNotFoundException e) {
            return false;
        }
    }
}
