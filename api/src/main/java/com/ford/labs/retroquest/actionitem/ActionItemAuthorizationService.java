package com.ford.labs.retroquest.actionitem;

import com.ford.labs.retroquest.exception.ActionItemDoesNotExistException;
import com.ford.labs.retroquest.teamusermapping.TeamUserAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ActionItemAuthorizationService {

    private final TeamUserAuthorizationService authorizationService;
    private final ActionItemService actionItemService;

    public ActionItemAuthorizationService(TeamUserAuthorizationService authorizationService, ActionItemService actionItemService) {
        this.authorizationService = authorizationService;
        this.actionItemService = actionItemService;
    }

    public boolean requestIsAuthorized(Authentication authentication, UUID teamId, Long actionItemId) {
        return authorizationService.isUserMemberOfTeam(authentication, teamId) && isActionItemPartOfTeam(teamId, actionItemId);
    }

    private boolean isActionItemPartOfTeam(UUID teamId, Long actionItemId) {
        try {
            actionItemService.fetchActionItem(teamId.toString(), actionItemId);
            return true;
        } catch(ActionItemDoesNotExistException e) {
            return false;
        }
    }
}
