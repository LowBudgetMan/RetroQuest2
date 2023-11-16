/*
 * Copyright (c) 2022 Ford Motor Company
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

package com.ford.labs.retroquest.column;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class ColumnController {

    private final ColumnService columnService;

    public ColumnController(ColumnService columnService) {
        this.columnService = columnService;
    }

    @GetMapping("/api/team/{teamId}/columns")
    @PreAuthorize("@authorizationService.requestIsAuthorized(authentication, #teamId)")
    public List<Column> getColumns(@PathVariable UUID teamId) {
        return columnService.getColumns(teamId.toString());
    }

    @Transactional
    @PutMapping("/api/team/{teamId}/column/{columnId}/title")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK") })
    public void updateTitleOfColumn(
        @PathVariable("teamId") UUID teamId,
        @RequestBody UpdateColumnTitleRequest request,
        @PathVariable("columnId") Long columnId
    ) {
        columnService.editTitle(columnId, request.title(), teamId.toString());
    }
}
