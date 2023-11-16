/*
 * Copyright (c) 2021 Ford Motor Company
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ford.labs.retroquest.board;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping("/team/{teamId}/board")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public ResponseEntity<Void> createBoard(@PathVariable("teamId") UUID teamId) throws URISyntaxException {
        var board = this.boardService.createBoard(teamId.toString());
        var uri = new URI(String.format("/api/team/%s/board/%s", board.getTeamId(), board.getId()));
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/team/{teamId}/boards")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public ResponseEntity<List<Board>> getBoards(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(value = "pageIndex", defaultValue = "0") Integer pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "30") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "dateCreated") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "DESC") String sortOrder
    ) {
        return this.boardService.getPaginatedBoardListWithHeaders(teamId.toString(), pageIndex, pageSize, sortBy, sortOrder);
    }

    @GetMapping("/team/{teamId}/boards/{boardId}")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public Retro getBoard(@PathVariable("teamId") UUID teamId, @PathVariable("boardId") Long boardId) {
        return this.boardService.getArchivedRetroForTeam(teamId.toString(), boardId);
    }

    @DeleteMapping("/team/{teamId}/board/{boardId}")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void deleteBoard(@PathVariable("teamId") UUID teamId, @PathVariable("boardId") Long boardId) {
        this.boardService.deleteBoard(teamId.toString(), boardId);
    }

    @DeleteMapping("/team/{teamId}/boards")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void deleteBoards(@PathVariable("teamId") UUID teamId, @RequestBody @Valid DeleteBoardsRequest request) {
        this.boardService.deleteBoards(teamId.toString(), request.boardIds());
    }

    @PutMapping("/team/{teamId}/end-retro")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void endRetro(@PathVariable("teamId") UUID teamId) {
        this.boardService.endRetro(teamId.toString());
    }
}
