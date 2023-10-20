package com.ford.labs.retroquest.team2;

import com.ford.labs.retroquest.team2.exception.InviteExpiredException;
import com.ford.labs.retroquest.team2.exception.InviteNotFoundException;
import com.ford.labs.retroquest.team2.exception.TeamAlreadyExistsException;
import com.ford.labs.retroquest.team2.exception.TeamNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/team2")
public class TeamController2 {

    private final TeamService2 teamService;

    public TeamController2(TeamService2 teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<Void> createTeam(@RequestBody CreateTeamRequest createTeamRequest, Principal principal) {
        var team = teamService.createTeam(createTeamRequest.name(), principal.getName());
        return ResponseEntity.created(URI.create("/api/team/%s".formatted(team.getId()))).build();
    }

    @GetMapping
    public ResponseEntity getTeams() {
        throw new UnsupportedOperationException();
    }

    @GetMapping("/{id}")
    public ResponseEntity getTeam(@PathVariable("id") UUID teamId){
        throw new UnsupportedOperationException();
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<Void> addUser(@PathVariable("id") UUID teamId, @RequestBody AddUserToTeamRequest request, Principal principal) {
        teamService.addUser(teamId, principal.getName(), request.inviteId());
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
