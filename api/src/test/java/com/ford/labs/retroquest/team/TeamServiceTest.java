package com.ford.labs.retroquest.team;

import com.ford.labs.retroquest.column.ColumnService;
import com.ford.labs.retroquest.team.exception.InviteExpiredException;
import com.ford.labs.retroquest.team.exception.InviteNotFoundException;
import com.ford.labs.retroquest.team.exception.TeamAlreadyExistsException;
import com.ford.labs.retroquest.team.invite.Invite;
import com.ford.labs.retroquest.team.invite.InviteService;
import com.ford.labs.retroquest.teamusermapping.TeamUserMappingService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TeamServiceTest {
    private final TeamRepository mockTeamRepository = mock(TeamRepository.class);
    private final TeamUserMappingService mockTeamUserMappingService = mock(TeamUserMappingService.class);
    private final InviteService mockInviteService = mock(InviteService.class);
    private final ColumnService mockColumnService = mock(ColumnService.class);
    private final TeamService service = new TeamService(mockTeamRepository, mockTeamUserMappingService, mockInviteService, mockColumnService);

    @Test
    void createTeam_ShouldReturnCreatedTeam() {
        var expected = new Team(UUID.randomUUID(), "expected name", LocalDateTime.now());
        when(mockTeamRepository.save(new Team(null, "expected name", null))).thenReturn(expected);
        var actual = service.createTeam("expected name", "User ID");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createTeam_WhenTeamAlreadyExists_ShouldThrowException() {
        when(mockTeamRepository.save(new Team(null, "team already exists", null))).thenThrow(DataIntegrityViolationException.class);
        assertThatExceptionOfType(TeamAlreadyExistsException.class).isThrownBy(() -> service.createTeam("team already exists", "user ID"));
    }

    @Test
    void createTeam_ShouldAddCreatingUserToTeam() {
        var expected = new Team(UUID.randomUUID(), "expected team name", LocalDateTime.now());
        when(mockTeamRepository.save(new Team(null, "expected team name", null))).thenReturn(expected);
        var actual = service.createTeam("expected team name", "User ID");
        assertThat(actual).isEqualTo(expected);
        verify(mockTeamUserMappingService).addUserToTeam(actual.getId(), "User ID");
    }

    @Test
    void createTeam_GeneratesInitialColumns() {
        var expectedTeam = new Team(UUID.randomUUID(), "expected team name", LocalDateTime.now());
        when(mockTeamRepository.save(new Team(null, "expected team name", null))).thenReturn(expectedTeam);
        service.createTeam("expected team name", "User ID");
        verify(mockColumnService).generateInitialColumnsForTeam(expectedTeam.getId());
    }

    @Test
    void getTeam_ReturnsOptionalFromRepository() {
        var teamId = UUID.randomUUID();
        var expected = Optional.of(new Team(teamId, "Team Name", LocalDateTime.now()));
        when(mockTeamRepository.findById(teamId)).thenReturn(expected);

        var actual = service.getTeam(teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void addUser_WhenInviteDoesNotExistForTeam_ThrowsInviteNotFoundException() {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        when(mockInviteService.getInvite(teamId, inviteId)).thenReturn(Optional.empty());
        assertThrows(InviteNotFoundException.class, () -> service.addUser(teamId, "User ID", inviteId));
    }

    @Test
    void addUser_WhenInviteOlderThanThreeHours_ThrowsInviteExpiredException() {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        when(mockInviteService.getInvite(teamId, inviteId)).thenReturn(Optional.of(new Invite(inviteId, teamId, LocalDateTime.of(1981, 10, 1, 0, 0))));
        assertThrows(InviteExpiredException.class, () -> service.addUser(teamId, "User ID", inviteId));
    }

    @Test
    void addUser_WhenTeamDoesNotExist_ThrowsTeamNotFoundException() {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        when(mockInviteService.getInvite(teamId, inviteId)).thenReturn(Optional.of(new Invite(inviteId, teamId, LocalDateTime.now())));
        service.addUser(teamId, "User ID", inviteId);
    }

    @Test
    void addUser_WithValidInvite_AddsUserTeamMappingForUserAndTeam() {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        when(mockInviteService.getInvite(teamId, inviteId)).thenReturn(Optional.of(new Invite(inviteId, teamId, LocalDateTime.now())));
        service.addUser(teamId, "User ID", inviteId);
        verify(mockTeamUserMappingService).addUserToTeam(teamId, "User ID");
    }

    @Test
    void removeUser_CallsTeamUserMappingService() {
        var teamId = UUID.randomUUID();
        var userId = "User ID";
        service.removeUser(teamId, userId);
        verify(mockTeamUserMappingService).removeUserFromTeam(teamId, userId);
    }
}