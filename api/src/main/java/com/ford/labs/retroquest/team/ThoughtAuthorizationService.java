package com.ford.labs.retroquest.team;

import com.ford.labs.retroquest.security.AuthorizationService;
import com.ford.labs.retroquest.thought.ThoughtService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ThoughtAuthorizationService {

    private final AuthorizationService authorizationService;
    private final ThoughtService thoughtService;

    public ThoughtAuthorizationService(AuthorizationService authorizationService, ThoughtService thoughtService) {
        this.authorizationService = authorizationService;
        this.thoughtService = thoughtService;
    }

    public boolean requestIsAuthorized(Authentication authentication, UUID teamId, Long thoughtId) {
        return authorizationService.requestIsAuthorized(authentication, teamId) && isThoughtPartOfTeam(teamId, thoughtId);
    }

    private boolean isThoughtPartOfTeam(UUID teamId, Long thoughtId) {
        var maybeThought = thoughtService.getThought(thoughtId);
        return maybeThought.isPresent() && maybeThought.get().getTeamId().equals(teamId.toString());
    }
}
