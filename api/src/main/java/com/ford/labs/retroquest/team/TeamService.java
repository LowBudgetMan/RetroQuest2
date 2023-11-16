package com.ford.labs.retroquest.team;

import com.ford.labs.retroquest.column.ColumnService;
import com.ford.labs.retroquest.team.exception.InviteExpiredException;
import com.ford.labs.retroquest.team.exception.InviteNotFoundException;
import com.ford.labs.retroquest.team.exception.TeamAlreadyExistsException;
import com.ford.labs.retroquest.team.invite.InviteService;
import com.ford.labs.retroquest.teamusermapping.TeamUserMappingService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository repository;
    private final TeamUserMappingService teamUserMappingService;
    private final InviteService inviteService;
    private final ColumnService columnService;

    public TeamService(TeamRepository repository, TeamUserMappingService teamUserMappingService, InviteService inviteService, ColumnService columnService) {
        this.repository = repository;
        this.teamUserMappingService = teamUserMappingService;
        this.inviteService = inviteService;
        this.columnService = columnService;
    }
    public Team createTeam(String teamName, String userId) throws TeamAlreadyExistsException {
        try {
            var savedTeam = repository.save(new Team(teamName));
            teamUserMappingService.addUserToTeam(savedTeam.getId(), userId);
            columnService.generateInitialColumnsForTeam(savedTeam.getId());
            return savedTeam;
        } catch (DataIntegrityViolationException exception) {
            throw new TeamAlreadyExistsException();
        }
    }

    public Optional<Team> getTeam(UUID teamId) {
        return repository.findById(teamId);
    }

    public void addUser(UUID teamId, String userId, UUID inviteId) {
        var invite = inviteService.getInvite(teamId, inviteId);
        if(invite.isEmpty()) {
            throw new InviteNotFoundException();
        }
        if(invite.get().getCreatedAt().plusHours(3).isBefore(LocalDateTime.now())) {
            throw new InviteExpiredException();
        }
        teamUserMappingService.addUserToTeam(teamId, userId);
    }

    public void removeUser(UUID teamId, String userId) {
        teamUserMappingService.removeUserFromTeam(teamId, userId);
    }
}
