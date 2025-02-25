package com.ford.labs.retroquest.team.invite;

import com.ford.labs.retroquest.team.exception.TeamNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InviteServiceTest {
    private final InviteRepository mockInviteRepository = mock(InviteRepository.class);
    private final InviteService inviteService = new InviteService(mockInviteRepository);

    @Test
    void createInvite_ReturnsCreatedInvite() {
        var teamId = UUID.randomUUID();
        var expected = new Invite(UUID.randomUUID(), teamId, LocalDateTime.now());
        when(mockInviteRepository.save(new Invite(null, teamId, null))).thenReturn(expected);

        var actual = inviteService.createInvite(teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createInvite_WhenTeamDoesNotExist_ThrowsTeamNotFoundException() {
        var teamId = UUID.randomUUID();
        when(mockInviteRepository.save(new Invite(null, teamId, null))).thenThrow(new DataIntegrityViolationException(
                "could not execute statement",
                new ConstraintViolationException(
                        "could not execute statement",
                        null,
                        "FK_INVITE_TEAM")));
        assertThrows(TeamNotFoundException.class, () -> inviteService.createInvite(teamId));
    }

    @Test
    void getInvite_ReturnsEntityFromRepository() {
        var teamId = UUID.randomUUID();
        var inviteId = UUID.randomUUID();
        var expectedInvite = Optional.of(new Invite(inviteId, teamId, LocalDateTime.now()));
        when(mockInviteRepository.findByIdAndTeamId(inviteId, teamId)).thenReturn(expectedInvite);
        assertThat(inviteService.getInvite(teamId, inviteId)).isEqualTo(expectedInvite);
    }

    @Test
    void deleteInvite_DeletesInviteFromRepo() {
        var inviteId = UUID.randomUUID();
        inviteService.deleteInvite(inviteId);
        verify(mockInviteRepository).deleteById(inviteId);
    }
}