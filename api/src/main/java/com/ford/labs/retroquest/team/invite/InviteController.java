package com.ford.labs.retroquest.team.invite;

import com.ford.labs.retroquest.team.exception.TeamNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/team/{id}/invites")
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

    @DeleteMapping("/{inviteId}")
    @PreAuthorize("@teamUserAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> deleteInvite(@PathVariable("id") UUID teamId, @PathVariable("inviteId") UUID inviteId) {
        inviteService.deleteInvite(inviteId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<Void> handleTeamNotFoundException() {
        return ResponseEntity.notFound().build();
    }
}
