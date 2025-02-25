package com.ford.labs.retroquest.team.invite;

import com.ford.labs.retroquest.team.Team;
import com.ford.labs.retroquest.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class InviteRepositoryTest {

    @Autowired
    private InviteRepository subject;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void save_AutoGeneratesIdAndCreationTimestamp() {
        var savedTeam = teamRepository.saveAndFlush(new Team(null, "team name", null));

        var actual = subject.saveAndFlush(new Invite(null, savedTeam.getId(), null));

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTeamId()).isEqualTo(savedTeam.getId());
        assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    void save_whenTeamDoesNotExist_ThrowsDataIntegrityViolationException() {
        assertThrows(DataIntegrityViolationException.class, () -> subject.saveAndFlush(new Invite(null, UUID.randomUUID(), null)));
    }

    @Test
    void findByIdAndTeamId_WhenTeamIdNotOnInviteWithId_ReturnsEmptyOptional() {
        var team = teamRepository.saveAndFlush(new Team(null, "Team 1", null));
        var invite = subject.saveAndFlush(new Invite(null, team.getId(), null));
        assertThat(subject.findByIdAndTeamId(invite.getId(), UUID.randomUUID())).isNotPresent();
    }

    @Test
    void findByIdAndTeamId_WhenInviteDoesNotExist_ReturnsEmptyOptional() {
        var team = teamRepository.saveAndFlush(new Team(null, "Team 1", null));
        assertThat(subject.findByIdAndTeamId(UUID.randomUUID(), team.getId())).isNotPresent();
    }

    @Test
    void findByIdAndTeamId_WhenInviteHasTeamId_ReturnsInvite() {
        var team = teamRepository.saveAndFlush(new Team(null, "Team 1", null));
        var invite = subject.saveAndFlush(new Invite(null, team.getId(), null));
        assertThat(subject.findByIdAndTeamId(invite.getId(), team.getId())).isPresent();
    }
}