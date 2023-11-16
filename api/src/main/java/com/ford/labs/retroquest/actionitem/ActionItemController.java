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

package com.ford.labs.retroquest.actionitem;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;

@RestController
public class ActionItemController {

    private final ActionItemService actionItemService;

    public ActionItemController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @PostMapping("/api/team/{teamId}/action-item")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public ResponseEntity<URI> createActionItemForTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestBody CreateActionItemRequest request
    ) throws URISyntaxException {
        var actionItem = actionItemService.createActionItem(teamId.toString(), request);
        var actionItemUri = new URI(format("/api/team/%s/action-item/%d", teamId, actionItem.getId()));
        return ResponseEntity.created(actionItemUri).build();
    }

    @GetMapping("/api/team/{teamId}/action-item")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public List<ActionItem> getActionItemsForTeam(@PathVariable("teamId") UUID teamId, @RequestParam(required = false) Boolean archived) {
        return actionItemService.getActionItems(teamId.toString(), Optional.ofNullable(archived));
    }

    @PutMapping("/api/team/{teamId}/action-item/{actionItemId}/completed")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void completeActionItem(
            @PathVariable("teamId") UUID teamId,
            @PathVariable("actionItemId") Long actionItemId,
            @RequestBody UpdateActionItemCompletedRequest request
    ) {
        actionItemService.updateCompletedStatus(teamId.toString(), actionItemId, request);
    }

    @PutMapping("/api/team/{teamId}/action-item/{actionItemId}/task")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void updateActionItemTask(
        @PathVariable("teamId") UUID teamId,
        @PathVariable("actionItemId") Long actionItemId,
        @RequestBody UpdateActionItemTaskRequest request
    ) {
        actionItemService.updateTask(teamId.toString(), actionItemId, request);
    }

    @PutMapping("/api/team/{teamId}/action-item/{actionItemId}/assignee")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void updateActionItemAssignee(
        @PathVariable("teamId") UUID teamId,
        @PathVariable("actionItemId") Long actionItemId,
        @RequestBody UpdateActionItemAssigneeRequest request
    ) {
        actionItemService.updateAssignee(teamId.toString(), actionItemId, request);
    }

    @PutMapping("/api/team/{teamId}/action-item/{actionItemId}/archived")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void updateActionItemArchivedStatus(
            @PathVariable("teamId") UUID teamId,
            @PathVariable("actionItemId") Long actionItemId,
            @RequestBody UpdateActionItemArchivedRequest request
    ) {
        actionItemService.updateArchivedStatus(teamId.toString(), actionItemId, request);
    }

    @Transactional
    @DeleteMapping("/api/team/{teamId}/action-item/{actionItemId}")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void deleteActionItemByTeamIdAndId(@PathVariable("teamId") UUID teamId, @PathVariable("actionItemId") Long actionItemId) {
        actionItemService.deleteOneActionItem(teamId.toString(), actionItemId);
    }

    @Transactional
    @DeleteMapping("/api/team/{teamId}/action-item")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public void deleteActionItemsByTeamIdAndIds(@PathVariable("teamId") UUID teamId, @RequestBody() DeleteActionItemsRequest request) {
        actionItemService.deleteMultipleActionItems(teamId.toString(), request.actionItemIds());
    }
}
