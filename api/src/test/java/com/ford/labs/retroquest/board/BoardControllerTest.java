package com.ford.labs.retroquest.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.retroquest.column.Column;
import com.ford.labs.retroquest.teamusermapping.TeamUserAuthorizationService;
import com.ford.labs.retroquest.thought.Thought;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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
class BoardControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private TeamUserAuthorizationService teamUserAuthorizationService;

    @MockBean
    private BoardAuthorizationService boardAuthorizationService;

    @MockBean
    private BoardService boardService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getBoards_ReturnsBoardsWithPaginationInfo() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        var expectedResponse = createPaginatedBoardListResponse(teamId);
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(boardService.getPaginatedBoardListWithHeaders(teamId.toString(), 0, 2, "dateCreated", "DESC")).thenReturn(expectedResponse);
        mockMvc.perform(get("/api/team/%s/boards?pageIndex=0&pageSize=2".formatted(teamId.toString()))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", Matchers.is(2)))
                .andExpect(header().string(
                        "Access-Control-Expose-Headers",
                        "Sort-Order,Sort-By,Page-Index,Page-Size,Total-Board-Count,Total-Pages,Page-Range"
                ))
                .andExpect(header().string("Sort-By", "dateCreated"))
                .andExpect(header().string("Sort-Order", "DESC"))
                .andExpect(header().string("Page-Index", "0"))
                .andExpect(header().string("Page-Size", "2"))
                .andExpect(header().string("Page-Range", "1-2"))
                .andExpect(header().string("Total-Board-Count", "3"))
                .andExpect(header().string("Total-Pages", "2"))
                .andExpect(jsonPath("$.[0].id").value(1L))
                .andExpect(jsonPath("$.[0].dateCreated").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.[0].teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.[0].thoughts.size()", Matchers.is(0)))
                .andExpect(jsonPath("$.[1].id").value(2L))
                .andExpect(jsonPath("$.[1].dateCreated").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.[1].teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.[1].thoughts.size()", Matchers.is(0)));
    }

    @Test
    void getBoards_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        mockMvc.perform(get("/api/team/%s/boards?pageIndex=0&pageSize=2".formatted(teamId.toString()))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getBoards_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(get("/api/team/%s/boards?pageIndex=0&pageSize=2".formatted(teamId.toString()))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBoard_createsBoard() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        var expectedBoard = new Board(1L, teamId.toString(), LocalDate.now(), List.of());
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        when(boardService.createBoard(teamId.toString())).thenReturn(expectedBoard);
        mockMvc.perform(post("/api/team/%s/board".formatted(teamId.toString()))
                .with(jwt()))
            .andExpect(status().isCreated())
            .andExpect(header().string(HttpHeaders.LOCATION, "/api/team/%s/board/%d".formatted(teamId.toString(), 1L)));
    }

    @Test
    void createBoard_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        mockMvc.perform(post("/api/team/%s/board".formatted(teamId.toString()))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBoard_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(post("/api/team/%s/board".formatted(teamId.toString()))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBoard_ReturnsBoard() throws Exception {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        var columnId = 100L;
        var column1 = new Column(columnId, "topic", "title", teamId.toString());
        var thought1 = new Thought(10L, "message 1", 0, false, teamId.toString(), boardId, columnId);
        var thought2 = new Thought(11L, "message 2", 0, false, teamId.toString(), boardId, columnId);
        var expectedBoard = new Retro(1L, teamId.toString(), LocalDate.now(), List.of(thought1, thought2), List.of(column1));
        var authentication = createAuthentication();
        when(boardAuthorizationService.requestIsAuthorized(authentication, teamId, boardId)).thenReturn(true);
        when(boardService.getArchivedRetroForTeam(teamId.toString(), boardId)).thenReturn(expectedBoard);
        mockMvc.perform(get("/api/team/%s/boards/%d".formatted(teamId.toString(), boardId))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andExpect(jsonPath("$.dateCreated").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.thoughts.[0]").value(thought1))
                .andExpect(jsonPath("$.thoughts.[1]").value(thought2))
                .andExpect(jsonPath("$.columns.[0]").value(column1));
    }

    @Test
    void getBoard_WhenUserUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        mockMvc.perform(get("/api/team/%s/boards/%d".formatted(teamId.toString(), boardId))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getBoard_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        var authentication = createAuthentication();
        when(boardAuthorizationService.requestIsAuthorized(authentication, teamId, boardId)).thenReturn(false);
        mockMvc.perform(get("/api/team/%s/boards/%d".formatted(teamId.toString(), boardId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBoard_DeletesBoard() throws Exception {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        var authentication = createAuthentication();
        when(boardAuthorizationService.requestIsAuthorized(authentication, teamId, boardId)).thenReturn(true);
        mockMvc.perform(delete(String.format("/api/team/%s/board/%s".formatted(teamId.toString(), boardId)))
                .with(jwt()))
            .andExpect(status().isOk());
        verify(boardService).deleteBoard(teamId.toString(), boardId);
    }

    @Test
    void deleteBoard_WhenUserIsUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        mockMvc.perform(delete(String.format("/api/team/%s/board/%s".formatted(teamId.toString(), boardId)))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteBoard_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var boardId = 1L;
        var authentication = createAuthentication();
        when(boardAuthorizationService.requestIsAuthorized(authentication, teamId, boardId)).thenReturn(false);
        mockMvc.perform(delete("/api/team/%s/board/%s".formatted(teamId.toString(), boardId))
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void endRetro_ArchivesRetro() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        mockMvc.perform(put("/api/team/%s/end-retro".formatted(teamId.toString()))
                .with(jwt()))
            .andExpect(status().isOk());
        verify(boardService).endRetro(teamId.toString());
    }

    @Test
    void endRetro_WhenUserIsUnauthorized_Throws401() throws Exception {
        var teamId = UUID.randomUUID();
        mockMvc.perform(put("/api/team/%s/end-retro".formatted(teamId.toString()))
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endRetro_WhenUserNotOnTeam_Throws403() throws Exception {
        var teamId = UUID.randomUUID();
        var authentication = createAuthentication();
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        mockMvc.perform(put("/api/team/%s/end-retro".formatted(teamId.toString()))
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

    private ResponseEntity<List<Board>> createPaginatedBoardListResponse(UUID teamId) {
        var board1 = new Board(1L, teamId.toString(), LocalDate.now(), List.of());
        var board2 = new Board(2L, teamId.toString(), LocalDate.now(), List.of());
        var headers = new HttpHeaders();
        headers.add(
                "Access-Control-Expose-Headers",
                "Sort-Order,Sort-By,Page-Index,Page-Size,Total-Board-Count,Total-Pages,Page-Range"
        );
        headers.set("Sort-Order", "DESC");
        headers.set("Sort-By", "dateCreated");
        headers.set("Page-Index", "0");
        headers.set("Page-Size", "2");
        headers.set("Page-Range", "1-2");
        headers.set("Total-Board-Count", "3");
        headers.set("Total-Pages",  "2");
        return ResponseEntity.ok().headers(headers).body(List.of(board1, board2));
    }
}