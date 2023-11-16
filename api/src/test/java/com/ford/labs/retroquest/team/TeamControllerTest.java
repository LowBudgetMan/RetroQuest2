package com.ford.labs.retroquest.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.retroquest.team.exception.InviteExpiredException;
import com.ford.labs.retroquest.team.exception.InviteNotFoundException;
import com.ford.labs.retroquest.team.exception.TeamAlreadyExistsException;
import com.ford.labs.retroquest.team.exception.TeamNotFoundException;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
class TeamControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private TeamService service;

    @MockBean
    private TeamUserAuthorizationService authorizationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTeam_Returns201WithLocationIncludingTeamId() throws Exception {
        var teamId = UUID.randomUUID();
        var teamName = "Team name";
        when(service.createTeam(teamName, "user")).thenReturn(new Team(teamId, teamName, LocalDateTime.now()));

        mockMvc.perform(post("/api/team2")
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest(teamName)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/team/%s".formatted(teamId)));
    }

    @Test
    void createTeam_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(post("/api/team2")
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest("Team name")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTeam_WhenTeamAlreadyExists_Throws409() throws Exception {
        var teamName = "Team name";
        doThrow(TeamAlreadyExistsException.class).when(service).createTeam(teamName, "user");
        mockMvc.perform(post("/api/team2")
                        .with(jwt())
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest(teamName)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void getTeam_returnsTeam() throws Exception {
        var teamId = UUID.randomUUID();
        var team = new Team(teamId, "Team name", LocalDateTime.now());
        var authentication = createAuthentication();
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(service.getTeam(teamId)).thenReturn(Optional.of(team));
        mockMvc.perform(get("/api/team2/%s".formatted(teamId))
                .with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(teamId.toString()))
            .andExpect(jsonPath("$.name").value(team.getName()))
            .andExpect(jsonPath("$.createdAt").value(team.getCreatedAt().toString()));
    }

    @Test
    void getTeam_WhenInvalidToken_Returns401() throws Exception{
        mockMvc.perform(get("/api/team2/%s".formatted(UUID.randomUUID()))
                    .with(SecurityMockMvcRequestPostProcessors.anonymous()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getTeam_WhenUserNotOnTeam_Returns403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(get("/api/team2/%s".formatted(teamId))
                .with(jwt()))
            .andExpect(status().isForbidden());

    }

    @Test
    void getTeam_WhenTeamDoesNotExist_Returns404() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(service.getTeam(teamId)).thenReturn(Optional.empty());
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        mockMvc.perform(get("/api/team2/%s".formatted(teamId))
                    .with(jwt()))
            .andExpect(status().isNotFound());
    }

    @Test
    void addUser_WithGoodInvite_Returns200() throws Exception {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        mockMvc.perform(post("/api/team2/%s/users".formatted(teamId.toString()))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddUserToTeamRequest(inviteId))))
                .andExpect(status().isOk());
        verify(service).addUser(teamId, "user", inviteId);
    }

    @Test
    void addUser_WithInvalidToken_Throws401() throws Exception {
        mockMvc.perform(post("/api/team2/%s/users".formatted(UUID.randomUUID()))
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())
                        .content(objectMapper.writeValueAsString(new AddUserToTeamRequest(UUID.randomUUID())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addUser_WhenTeamNotFound_Returns404() throws Exception {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        doThrow(TeamNotFoundException.class).when(service).addUser(teamId, "user", inviteId);
        mockMvc.perform(post("/api/team2/%s/users".formatted(teamId.toString()))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddUserToTeamRequest(inviteId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUser_WithInvalidInviteId_Returns404() throws Exception {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        doThrow(InviteNotFoundException.class).when(service).addUser(teamId, "user", inviteId);
        mockMvc.perform(post("/api/team2/%s/users".formatted(teamId.toString()))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddUserToTeamRequest(inviteId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUser_WithExpiredInvite_Returns400() throws Exception {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        doThrow(InviteExpiredException.class).when(service).addUser(teamId, "user", inviteId);
        mockMvc.perform(post("/api/team2/%s/users".formatted(teamId.toString()))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddUserToTeamRequest(inviteId))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeUser_WhenUserIsOnTeam_Returns200AndRemovesUser() throws Exception{
        var teamId = UUID.randomUUID();
        var userToRemoveId = "user2";
        var authentication = createAuthentication();
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);

        mockMvc.perform(delete("/api/team2/%s/users/%s".formatted(teamId.toString(), userToRemoveId))
                .with(jwt()))
            .andExpect(status().isOk());
        verify(service).removeUser(teamId, userToRemoveId);
    }

    @Test
    void removeUser_WithInvalidToken_Throws401() throws Exception{
        var teamId = UUID.randomUUID();
        var userToRemoveId = "user2";
        mockMvc.perform(delete("/api/team2/%s/users/%s".formatted(teamId.toString(), userToRemoveId))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
        verify(service, never()).removeUser(teamId, userToRemoveId);
    }

    @Test
    void removeUser_WhenUserIsNotOnTeam_Returns403() throws Exception{
        var teamId = UUID.randomUUID();
        var userToRemoveId = "user2";
        var authentication = createAuthentication();
        when(authorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);

        mockMvc.perform(delete("/api/team2/%s/users/%s".formatted(teamId.toString(), userToRemoveId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
        verify(service, never()).removeUser(teamId, userToRemoveId);
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