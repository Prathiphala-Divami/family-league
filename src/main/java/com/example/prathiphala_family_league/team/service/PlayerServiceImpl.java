package com.example.prathiphala_family_league.team.service;

import com.example.prathiphala_family_league.common.exception.ResourceNotFoundException;
import com.example.prathiphala_family_league.team.dto.CreatePlayerRequest;
import com.example.prathiphala_family_league.team.dto.PlayerResponse;
import com.example.prathiphala_family_league.team.dto.UpdatePlayerRequest;
import com.example.prathiphala_family_league.team.entity.Player;
import com.example.prathiphala_family_league.team.entity.Team;
import com.example.prathiphala_family_league.team.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamService teamService;

    @Override
    @Transactional
    public PlayerResponse addPlayer(Long teamId, CreatePlayerRequest request) {
        Team team = teamService.findTeam(teamId);

        Player player = new Player();
        player.setTeam(team);
        player.setPlayerName(request.getPlayerName());
        player.setPlayerRole(request.getPlayerRole());
        player.setJerseyNumber(request.getJerseyNumber());
        player.setCountry(request.getCountry());

        return new PlayerResponse(playerRepository.save(player));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> getPlayersByTeam(Long teamId) {
        teamService.findTeam(teamId);
        return playerRepository.findByTeamIdAndDeletedFalse(teamId).stream()
                .map(PlayerResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public PlayerResponse updatePlayer(Long teamId, Long playerId, UpdatePlayerRequest request) {
        teamService.findTeam(teamId);
        Player player = findPlayerInTeam(teamId, playerId);

        if (StringUtils.hasText(request.getPlayerName())) {
            player.setPlayerName(request.getPlayerName());
        }
        if (request.getPlayerRole() != null) {
            player.setPlayerRole(request.getPlayerRole());
        }
        if (request.getJerseyNumber() != null) {
            player.setJerseyNumber(request.getJerseyNumber());
        }
        if (request.getCountry() != null) {
            player.setCountry(request.getCountry());
        }

        return new PlayerResponse(playerRepository.save(player));
    }

    @Override
    @Transactional
    public void removePlayer(Long teamId, Long playerId) {
        teamService.findTeam(teamId);
        Player player = findPlayerInTeam(teamId, playerId);
        player.setDeleted(true);
        player.setDeletedAt(Instant.now());
        playerRepository.save(player);
    }

    @Override
    public Player findPlayer(Long playerId) {
        return playerRepository.findByIdAndDeletedFalse(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player", playerId));
    }

    private Player findPlayerInTeam(Long teamId, Long playerId) {
        Player player = findPlayer(playerId);
        if (!player.getTeam().getId().equals(teamId)) {
            throw new ResourceNotFoundException("Player", playerId);
        }
        return player;
    }
}
