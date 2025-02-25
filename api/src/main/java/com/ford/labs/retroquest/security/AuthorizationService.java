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

package com.ford.labs.retroquest.security;

import com.ford.labs.retroquest.teamusermapping.TeamUserAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {

    private final TeamUserAuthorizationService authorizationService;

    public AuthorizationService(TeamUserAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public boolean requestIsAuthorized(Authentication authentication, UUID teamId) {
        return authorizationService.isUserMemberOfTeam(authentication, teamId);
    }
}
