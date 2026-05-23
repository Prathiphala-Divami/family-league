package com.example.prathiphala_family_league.team.service;

import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.team.dto.CreateTeamRequest;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public TeamResponse create(CreateTeamRequest request) {
        Team team = new Team();
        team.setTeamName(request.getTeamName());
        team.setShortName(request.getShortName());
        team.setLogo(request.getLogo());
        return new TeamResponse(teamRepository.save(team));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponse> getAll(Pageable pageable, String search) {
        String trimmed = StringUtils.hasText(search) ? search.trim() : null;
        return teamRepository.searchByTeamName(trimmed, pageable).map(TeamResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(Long id) {
        return new TeamResponse(findTeam(id));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Team team = findTeam(id);
        team.setDeleted(true);
        team.setDeletedAt(Instant.now());
        teamRepository.save(team);
    }

    @Override
    public Team findTeam(Long id) {
        return teamRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
}
