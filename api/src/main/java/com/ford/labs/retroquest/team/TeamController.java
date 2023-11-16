package com.ford.labs.retroquest.team;

import com.ford.labs.retroquest.team.exception.InviteExpiredException;
import com.ford.labs.retroquest.team.exception.InviteNotFoundException;
import com.ford.labs.retroquest.team.exception.TeamAlreadyExistsException;
import com.ford.labs.retroquest.team.exception.TeamNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<Void> createTeam(@RequestBody CreateTeamRequest createTeamRequest, Principal principal) {
        var team = teamService.createTeam(createTeamRequest.name(), principal.getName());
        return ResponseEntity.created(URI.create("/api/team/%s".formatted(team.getId()))).build();
    }

    @GetMapping("{id}")
    @PreAuthorize("@teamUserAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Team> getTeam(@PathVariable("id") UUID teamId) {
        var possibleTeam = teamService.getTeam(teamId);
        return possibleTeam.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<Void> addUser(@PathVariable("id") UUID teamId, @RequestBody AddUserToTeamRequest request, Principal principal) {
        teamService.addUser(teamId, principal.getName(), request.inviteId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/users/{userId}")
    @PreAuthorize("@teamUserAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> removeUser(@PathVariable("id") UUID teamId, @PathVariable("userId") String userId) {
        teamService.removeUser(teamId, userId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(TeamAlreadyExistsException.class)
    public ResponseEntity<Void> handleTeamAlreadyExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<Void> handleTeamNotFoundException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InviteNotFoundException.class)
    public ResponseEntity<Void> handleInviteNotFoundException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InviteExpiredException.class)
    public ResponseEntity<Void> handleInviteExpiredException() {
        return ResponseEntity.badRequest().build();
    }
}
