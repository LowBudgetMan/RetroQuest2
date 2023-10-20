package com.ford.labs.retroquest.team2.invite;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/team2/{id}/invites")
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping
    @PreAuthorize("@teamUserAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> createInvite(@PathVariable("id") UUID teamId) {
        var invite = inviteService.createInvite(teamId);
        return ResponseEntity.created(URI.create("/api/team/%s/invites/%s".formatted(teamId, invite.getId()))).build();
    }
}
