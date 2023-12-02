package com.ford.labs.retroquest.column;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.retroquest.teamusermapping.TeamUserAuthorizationService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ColumnControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private TeamUserAuthorizationService teamUserAuthorizationService;

    @MockBean
    private ColumnAuthorizationService columnAuthorizationService;

    @MockBean
    private ColumnService columnService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getColumns_ReturnsListOfColumnsPerOrg() throws Exception {
        var teamId = UUID.randomUUID();
        var column1 = new Column(1L, "topic1", "title1", teamId.toString());
        var column2 = new Column(2L, "topic2", "title2", teamId.toString());
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(columnService.getColumns(teamId.toString())).thenReturn(List.of(column1, column2));
        mockMvc.perform(get("/api/team/%s/columns".formatted(teamId.toString()))
                .with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].id").value(1L))
            .andExpect(jsonPath("$.[0].topic").value("topic1"))
            .andExpect(jsonPath("$.[0].title").value("title1"))
            .andExpect(jsonPath("$.[0].teamId").value(teamId.toString()))
            .andExpect(jsonPath("$.[1].id").value(2L))
            .andExpect(jsonPath("$.[1].topic").value("topic2"))
            .andExpect(jsonPath("$.[1].title").value("title2"))
            .andExpect(jsonPath("$.[1].teamId").value(teamId.toString()));
    }

    @Test
    void getColumns_WhenUserIsUnauthorized_Throws401() throws Exception {
        mockMvc.perform(get("/api/team/%s/columns".formatted(UUID.randomUUID().toString()))
                .with(anonymous()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getColumns_WhenUserIsNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(get("/api/team/%s/columns".formatted(teamId.toString()))
                .with(jwt()))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateTitle_UpdatesColumn() throws Exception {
        var teamId = UUID.randomUUID();
        var columnId = 1L;
        var authentication = createAuthentication();
        when(columnAuthorizationService.requestIsAuthorized(authentication, teamId, columnId)).thenReturn(true);

        mockMvc.perform(put("/api/team/%s/column/%d/title".formatted(teamId.toString(), columnId))
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateColumnTitleRequest("New title"))))
            .andExpect(status().isOk());

        verify(columnService).editTitle(columnId, "New title", teamId.toString());
    }

    @Test
    void updateTitle_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var columnId = 1L;
        mockMvc.perform(put("/api/team/%s/column/%d/title".formatted(teamId.toString(), columnId))
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateColumnTitleRequest("New title"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTitle_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var columnId = 1L;
        var authentication = createAuthentication();
        when(columnAuthorizationService.requestIsAuthorized(authentication, teamId, columnId)).thenReturn(false);

        mockMvc.perform(put("/api/team/%s/column/%d/title".formatted(teamId.toString(), columnId))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateColumnTitleRequest("New title"))))
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