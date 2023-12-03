package com.ford.labs.retroquest.actionitem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.retroquest.exception.ActionItemDoesNotExistException;
import com.ford.labs.retroquest.teamusermapping.TeamUserAuthorizationService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ActionItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActionItemService actionItemService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private TeamUserAuthorizationService teamUserAuthorizationService;

    @MockBean
    private ActionItemAuthorizationService actionItemAuthorizationService;

    @Test
    void createActionItem_CreatesActionItem() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var createRequest = new CreateActionItemRequest("task", false, "assignee", new Date(100000), false);
        var expectedActionItem = new ActionItem(actionItemId, "task", false, teamId.toString(), "assignee", new Date(100000), false);
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(actionItemService.createActionItem(teamId.toString(), createRequest)).thenReturn(expectedActionItem);
        mockMvc.perform(post("/api/team/%s/action-item".formatted(teamId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/team/%s/action-item/%d".formatted(teamId.toString(), actionItemId)));
    }

    @Test
    void createActionItem_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var createRequest = new CreateActionItemRequest("task", false, "assignee", new Date(100000), false);
        mockMvc.perform(post("/api/team/%s/action-item".formatted(teamId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createActionItem_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var createRequest = new CreateActionItemRequest("task", false, "assignee", new Date(100000), false);
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(post("/api/team/%s/action-item".formatted(teamId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getActionItems_ReturnsActionItems() throws Exception {
        var teamId = UUID.randomUUID();
        var expectedActionItem = new ActionItem(1L, "task", false, teamId.toString(), "assignee", new Date(100000), false);
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(actionItemService.getActionItems(teamId.toString(), Optional.empty())).thenReturn(List.of(expectedActionItem));
        mockMvc.perform(get("/api/team/%s/action-item".formatted(teamId))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(1L))
                .andExpect(jsonPath("$.[0].task").value("task"))
                .andExpect(jsonPath("$.[0].completed").value(false))
                .andExpect(jsonPath("$.[0].teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.[0].assignee").value("assignee"))
                .andExpect(jsonPath("$.[0].archived").value(false));
    }

    @Test
    void getActionItems_WithArchivedTrue_RetrievesArchivedActionItems() throws Exception {
        var teamId = UUID.randomUUID();
        var expectedActionItem = new ActionItem(1L, "task", false, teamId.toString(), "assignee", new Date(100000), false);
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(actionItemService.getActionItems(teamId.toString(), Optional.empty())).thenReturn(List.of(expectedActionItem));
        mockMvc.perform(get("/api/team/%s/action-item".formatted(teamId))
                        .with(jwt())
                        .param("archived", "true"))
                .andExpect(status().isOk());
        verify(actionItemService).getActionItems(teamId.toString(), Optional.of(true));
    }

    @Test
    void getActionItems_WithArchivedFalse_RetrievesUnarchivedActionItems() throws Exception {
        var teamId = UUID.randomUUID();
        var expectedActionItem = new ActionItem(1L, "task", false, teamId.toString(), "assignee", new Date(100000), false);
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(actionItemService.getActionItems(teamId.toString(), Optional.empty())).thenReturn(List.of(expectedActionItem));
        mockMvc.perform(get("/api/team/%s/action-item".formatted(teamId))
                        .with(jwt())
                        .param("archived", "false"))
                .andExpect(status().isOk());
        verify(actionItemService).getActionItems(teamId.toString(), Optional.of(false));
    }

    @Test
    void getActionItems_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        mockMvc.perform(get("/api/team/%s/action-item".formatted(teamId))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getActionItems_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(get("/api/team/%s/action-item".formatted(teamId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void completeActionItem_UpdatesActionItem() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemCompletedRequest(true);
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/action-item/%d/completed".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(actionItemService).updateCompletedStatus(teamId.toString(), actionItemId, request);
    }

    @Test
    void completeActionItem_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemCompletedRequest(true);
        mockMvc.perform(put("/api/team/%s/action-item/%d/completed".formatted(teamId.toString(), actionItemId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void completeActionItem_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemCompletedRequest(true);
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/action-item/%d/completed".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void completeActionItem_WhenActionItemDoesNotExist_Throws404() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemCompletedRequest(true);
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        doThrow(new ActionItemDoesNotExistException()).when(actionItemService).updateCompletedStatus(teamId.toString(), actionItemId, request);
        mockMvc.perform(put("/api/team/%s/action-item/%d/completed".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteActionItem_RemovesActionItem() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        mockMvc.perform(delete("/api/team/%s/action-item/%d".formatted(teamId.toString(), actionItemId))
                        .with(jwt()))
                .andExpect(status().isOk());
        verify(actionItemService).deleteOneActionItem(teamId.toString(), actionItemId);
    }

    @Test
    void deleteActionItem_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        mockMvc.perform(delete("/api/team/%s/action-item/%d".formatted(teamId.toString(), actionItemId))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteActionItem_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(false);
        mockMvc.perform(delete("/api/team/%s/action-item/%d".formatted(teamId.toString(), actionItemId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMultipleActionItems_RemovesMultipleActionItems() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemIds = List.of(1L, 2L, 3L);
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        mockMvc.perform(delete("/api/team/%s/action-item".formatted(teamId.toString()))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeleteActionItemsRequest(actionItemIds))))
                .andExpect(status().isOk());
        verify(actionItemService).deleteMultipleActionItems(teamId.toString(), actionItemIds);
    }

    @Test
    void deleteMultipleActionItems_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemIds = List.of(1L, 2L, 3L);
        mockMvc.perform(delete("/api/team/%s/action-item".formatted(teamId.toString()))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeleteActionItemsRequest(actionItemIds))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMultipleActionItems_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemIds = List.of(1L, 2L, 3L);
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(delete("/api/team/%s/action-item".formatted(teamId.toString()))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeleteActionItemsRequest(actionItemIds))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTask_UpdatesActionItem() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemTaskRequest("new task");
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/action-item/%d/task".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(actionItemService).updateTask(teamId.toString(), actionItemId, request);
    }

    @Test
    void updateTask_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemTaskRequest("new task");
        mockMvc.perform(put("/api/team/%s/action-item/%d/task".formatted(teamId.toString(), actionItemId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTask_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemTaskRequest("new task");
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/action-item/%d/task".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTask_WhenTaskDoesNotExist_Throws404() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemTaskRequest("new task");
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        doThrow(ActionItemDoesNotExistException.class).when(actionItemService).updateTask(teamId.toString(), actionItemId, request);
        mockMvc.perform(put("/api/team/%s/action-item/%d/task".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAssignee_UpdatesActionItem() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemAssigneeRequest("New assignee");
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/action-item/%d/assignee".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(actionItemService).updateAssignee(teamId.toString(), actionItemId, request);
    }

    @Test
    void updateAssignee_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemAssigneeRequest("New assignee");
        mockMvc.perform(put("/api/team/%s/action-item/%d/assignee".formatted(teamId.toString(), actionItemId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAssignee_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemAssigneeRequest("New assignee");
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/action-item/%d/assignee".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateAssignee_WhenTaskDoesNotExist_Throws404() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemAssigneeRequest("New assignee");
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        doThrow(ActionItemDoesNotExistException.class).when(actionItemService).updateAssignee(teamId.toString(), actionItemId, request);
        mockMvc.perform(put("/api/team/%s/action-item/%d/assignee".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void archiveActionItem_UpdatesActionItem() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemArchivedRequest(true);
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/action-item/%d/archived".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(actionItemService).updateArchivedStatus(teamId.toString(), actionItemId, request);
    }

    @Test
    void archiveActionItem_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemArchivedRequest(true);
        mockMvc.perform(put("/api/team/%s/action-item/%d/archived".formatted(teamId.toString(), actionItemId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void archiveActionItem_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemArchivedRequest(true);
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/action-item/%d/archived".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void archiveActionItem_WhenTaskDoesNotExist_Throws404() throws Exception {
        var teamId = UUID.randomUUID();
        var actionItemId = 1L;
        var request = new UpdateActionItemArchivedRequest(true);
        var authentication = createAuthentication();
        when(actionItemAuthorizationService.requestIsAuthorized(authentication, teamId, actionItemId)).thenReturn(true);
        doThrow(ActionItemDoesNotExistException.class).when(actionItemService).updateArchivedStatus(teamId.toString(), actionItemId, request);
        mockMvc.perform(put("/api/team/%s/action-item/%d/archived".formatted(teamId.toString(), actionItemId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    Authentication createAuthentication() {
        var headers = new HashMap<String, Object>();
        headers.put("alg", "none");
        var claims = new HashMap<String, Object>();
        claims.put("sub", "user");
        claims.put("scope", "read");
        var authorities = Lists.list(new SimpleGrantedAuthority("SCOPE_read"));
        return new JwtAuthenticationToken(
                new Jwt(
                        "token",
                        null,
                        null,
                        headers,
                        claims
                ),
                authorities,
                "user"
        );
    }
}