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
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private final UUID teamId = UUID.randomUUID();
    private final Authentication authentication = mock(Authentication.class);
    private final TeamUserAuthorizationService teamUserAuthorizationService = mock(TeamUserAuthorizationService.class);
    private final AuthorizationService authorizationService = new AuthorizationService(teamUserAuthorizationService);

    @Test
    void requestIsAuthorized_WhenUserIsMemberOfTeam_ReturnsTrue() {
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(true);
        assertThat(authorizationService.requestIsAuthorized(authentication, teamId)).isTrue();
    }

    @Test
    void requestIsAuthorized_WhenUserIsNotMemberOfTeam_ReturnsFalse() {
        when(teamUserAuthorizationService.isUserMemberOfTeam(authentication, teamId)).thenReturn(false);
        assertThat(authorizationService.requestIsAuthorized(authentication, teamId)).isFalse();
    }
}
