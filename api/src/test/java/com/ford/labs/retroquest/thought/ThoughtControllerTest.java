package com.ford.labs.retroquest.thought;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class ThoughtControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private TeamUserAuthorizationService authorizationService;

    @MockBean
    private ThoughtAuthorizationService thoughtAuthorizationService;

    @MockBean
    private ThoughtService thoughtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createThought_createsThought() throws Exception {
        var teamId = UUID.randomUUID();
        var columnId = 2L;
        var authentication = createAuthentication();
        var createThoughtRequest = new CreateThoughtRequest(
                "Hello",
                columnId
        );
        var expectedThought = new Thought(1L, "Hello", 0, false, teamId.toString(), 3L, 2L);
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(thoughtService.createThought(teamId.toString(), createThoughtRequest)).thenReturn(expectedThought);
        mockMvc.perform(post("/api/team/%s/thoughts".formatted(teamId))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createThoughtRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, "/api/team/%s/thought/1".formatted(teamId)));
    }

    @Test
    void createThought_WhenUserIsUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var columnId = 2L;
        var createThoughtRequest = new CreateThoughtRequest(
                "Hello",
                columnId
        );
        mockMvc.perform(post("/api/team/%s/thoughts".formatted(teamId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createThoughtRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createThought_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var columnId = 2L;
        var authentication = createAuthentication();
        var createThoughtRequest = new CreateThoughtRequest(
                "Hello",
                columnId
        );
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(post("/api/team/%s/thoughts".formatted(teamId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createThoughtRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getThoughts_ReturnsListOfThoughts() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        var expectedThoughts = List.of(
                new Thought(1L, "Thought 1", 0, false, teamId.toString(), 10L, 20L),
                new Thought(2L, "Thought 2", 0, false, teamId.toString(), 10L, 20L),
                new Thought(3L, "Thought 3", 0, false, teamId.toString(), 10L, 20L)
        );
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(thoughtService.fetchAllActiveThoughts(teamId.toString())).thenReturn(expectedThoughts);
        mockMvc.perform(get("/api/team/%s/thoughts".formatted(teamId))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(1L))
                .andExpect(jsonPath("$.[0].message").value("Thought 1"))
                .andExpect(jsonPath("$.[0].hearts").value(0))
                .andExpect(jsonPath("$.[0].discussed").value(false))
                .andExpect(jsonPath("$.[0].teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.[0].boardId").value(10L))
                .andExpect(jsonPath("$.[0].columnId").value(20L))
                .andExpect(jsonPath("$.[1].id").value(2L))
                .andExpect(jsonPath("$.[1].message").value("Thought 2"))
                .andExpect(jsonPath("$.[1].hearts").value(0))
                .andExpect(jsonPath("$.[1].discussed").value(false))
                .andExpect(jsonPath("$.[1].teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.[1].boardId").value(10L))
                .andExpect(jsonPath("$.[1].columnId").value(20L))
                .andExpect(jsonPath("$.[2].id").value(3L))
                .andExpect(jsonPath("$.[2].message").value("Thought 3"))
                .andExpect(jsonPath("$.[2].hearts").value(0))
                .andExpect(jsonPath("$.[2].discussed").value(false))
                .andExpect(jsonPath("$.[2].teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.[2].boardId").value(10L))
                .andExpect(jsonPath("$.[2].columnId").value(20L));
    }

    @Test
    void getThoughts_WhenUserIsUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        mockMvc.perform(get("/api/team/%s/thoughts".formatted(teamId))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getThoughts_WhenUserIsNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(get("/api/team/%s/thoughts".formatted(teamId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void likeThought_UpdatesThought() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/heart".formatted(teamId, thoughtId))
                        .with(jwt()))
                .andExpect(status().isOk());
        verify(thoughtService).likeThought(teamId.toString(), thoughtId);
    }

    @Test
    void likeThought_WhenUserIsUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        mockMvc.perform(put("/api/team/%s/thoughts/%d/heart".formatted(teamId, thoughtId))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void likeThought_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/heart".formatted(teamId, thoughtId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void discussThought_UpdatesThought() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        var discussThought = new UpdateThoughtDiscussedRequest(true);
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/discuss".formatted(teamId.toString(), thoughtId))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(discussThought)))
            .andExpect(status().isOk());
        verify(thoughtService).discussThought(teamId.toString(), thoughtId, true);
    }

    @Test
    void discussThought_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var discussThought = new UpdateThoughtDiscussedRequest(true);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/discuss".formatted(teamId.toString(), thoughtId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discussThought)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void discussThought_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        var discussThought = new UpdateThoughtDiscussedRequest(true);
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/discuss".formatted(teamId.toString(), thoughtId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discussThought)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateMessage_UpdatesThought() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        var updateMessage = new UpdateThoughtMessageRequest("New Message");
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/message".formatted(teamId.toString(), thoughtId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMessage)))
                .andExpect(status().isOk());
        verify(thoughtService).updateThoughtMessage(teamId.toString(), thoughtId, "New Message");
    }

    @Test
    void updateMessage_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var updateMessage = new UpdateThoughtMessageRequest("New Message");
        mockMvc.perform(put("/api/team/%s/thoughts/%d/message".formatted(teamId.toString(), thoughtId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMessage)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMessage_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        var updateMessage = new UpdateThoughtMessageRequest("New Message");
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/message".formatted(teamId.toString(), thoughtId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMessage)))
                .andExpect(status().isForbidden());
    }

    @Test
    void moveThought_UpdatesThought() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        var updateMessage = new MoveThoughtRequest(11L);
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/column-id".formatted(teamId.toString(), thoughtId))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateMessage)))
            .andExpect(status().isOk());
        verify(thoughtService).updateColumn(teamId.toString(), thoughtId, 11L);
    }

    @Test
    void moveThought_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var updateMessage = new MoveThoughtRequest(11L);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/column-id".formatted(teamId.toString(), thoughtId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMessage)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void moveThought_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        var updateMessage = new MoveThoughtRequest(11L);
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/thoughts/%d/column-id".formatted(teamId.toString(), thoughtId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMessage)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteThought_RemovesThought() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(true);
        mockMvc.perform(delete("/api/team/%s/thoughts/%d".formatted(teamId.toString(), thoughtId))
                    .with(jwt()))
                .andExpect(status().isOk());
        verify(thoughtService).deleteThought(teamId.toString(), thoughtId);
    }

    @Test
    void deleteThought_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        mockMvc.perform(delete("/api/team/%s/thoughts/%d".formatted(teamId.toString(), thoughtId))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteThought_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var thoughtId = 1L;
        var authentication = createAuthentication();
        when(thoughtAuthorizationService.requestIsAuthorized(authentication, teamId, thoughtId)).thenReturn(false);
        mockMvc.perform(delete("/api/team/%s/thoughts/%d".formatted(teamId.toString(), thoughtId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
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
