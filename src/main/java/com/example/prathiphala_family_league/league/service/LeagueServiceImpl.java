package com.example.prathiphala_family_league.league.service;

import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.league.dto.CreateLeagueRequest;
import com.example.prathiphala_family_league.league.dto.LeagueResponse;
import com.example.prathiphala_family_league.league.entity.League;
import com.example.prathiphala_family_league.league.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;

    @Override
    @Transactional
    public LeagueResponse create(CreateLeagueRequest request) {
        League league = new League();
        league.setName(request.getName());
        league.setDescription(request.getDescription());
        return new LeagueResponse(leagueRepository.save(league));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeagueResponse> getAll(Pageable pageable, String search) {
        String trimmed = StringUtils.hasText(search) ? search.trim() : null;
        return leagueRepository.searchByName(trimmed, pageable).map(LeagueResponse::new);
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueResponse getById(Long id) {
        return new LeagueResponse(findLeague(id));
    }

    League findLeague(Long id) {
        return leagueRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("League", id));
    }
}
