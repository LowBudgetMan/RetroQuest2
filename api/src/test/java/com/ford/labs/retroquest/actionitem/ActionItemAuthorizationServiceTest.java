package com.ford.labs.retroquest.actionitem;

import com.ford.labs.retroquest.exception.ActionItemDoesNotExistException;
import com.ford.labs.retroquest.teamusermapping.TeamUserAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.sql.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionItemAuthorizationServiceTest {

    private final TeamUserAuthorizationService authorizationService = mock(TeamUserAuthorizationService.class);
    private final ActionItemService actionItemService = mock(ActionItemService.class);
    private final ActionItemAuthorizationService actionItemAuthorizationService = new ActionItemAuthorizationService(authorizationService, actionItemService);

    @Test
    void requestIsAuthorized_WhenUserAndActionItemMemberOfTeam_ReturnsTrue() {
        var authentication = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(actionItemService.fetchActionItem(teamId.toString(), actionItemId)).thenReturn(new ActionItem(actionItemId, "task", false, teamId.toString(), "assignee", new Date(100000), false));
        assertThat(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).isTrue();
    }

    @Test
    void requestIsAuthorized_WhenUserNotOnTeam_ReturnsFalse() {
        var authentication = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        assertThat(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).isFalse();
    }

    @Test
    void requestIsAuthorized_WhenActionItemNotOnTeam_ReturnsFalse() {
        var authentication = mock(Authentication.class);
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(actionItemService.fetchActionItem(teamId.toString(), actionItemId)).thenThrow(ActionItemDoesNotExistException.class);
        assertThat(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).isFalse();
    }
}