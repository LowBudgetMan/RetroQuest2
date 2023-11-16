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

package com.ford.labs.retroquest.thought;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
public class ThoughtController {

    private final ThoughtService thoughtService;

    public ThoughtController(ThoughtService thoughtService) {
        this.thoughtService = thoughtService;
    }

    @PostMapping("/api/team/{teamId}/thought")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public ResponseEntity<Void> createThoughtForTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestBody CreateThoughtRequest request
    ) throws URISyntaxException {
        var thought = thoughtService.createThought(teamId.toString(), request);
        var uri = new URI(String.format("/api/team/%s/thought/%s", thought.getTeamId(), thought.getId()));
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/api/team/{teamId}/thoughts")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public List<Thought> getThoughtsForTeam(@PathVariable("teamId") UUID teamId) {
        return thoughtService.fetchAllActiveThoughts(teamId.toString());
    }

    @Transactional
    @PutMapping("/api/team/{teamId}/thought/{thoughtId}/heart")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void likeThought(@PathVariable("thoughtId") Long thoughtId, @PathVariable("teamId") UUID teamId) {
        thoughtService.likeThought(teamId.toString(), thoughtId);
    }

    @PutMapping("/api/team/{teamId}/thought/{thoughtId}/discuss")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void discussThought(
        @PathVariable("thoughtId") Long thoughtId,
        @PathVariable("teamId") UUID teamId,
        @RequestBody UpdateThoughtDiscussedRequest request
    ) {
        thoughtService.discussThought(teamId.toString(), thoughtId, request.discussed());
    }

    @Transactional
    @PutMapping("/api/team/{teamId}/thought/{thoughtId}/column-id")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void moveThought(
        @PathVariable UUID teamId,
        @PathVariable Long thoughtId,
        @RequestBody MoveThoughtRequest request
    ) {
        thoughtService.updateColumn(teamId.toString(), thoughtId, request.columnId());
    }

    @Transactional
    @PutMapping("/api/team/{teamId}/thought/{id}/message")
    public void updateThoughtMessage(@PathVariable("id") Long id, @RequestBody UpdateThoughtMessageRequest request,
                                     @PathVariable("teamId") UUID teamId) {
        thoughtService.updateThoughtMessage(teamId.toString(), id, request.message());
    }

    @Transactional
    @DeleteMapping("/api/team/{teamId}/thought/{thoughtId}")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void clearIndividualThoughtForTeam(@PathVariable("teamId") UUID teamId, @PathVariable("thoughtId") Long id) {
        thoughtService.deleteThought(teamId.toString(), id);
    }
}
