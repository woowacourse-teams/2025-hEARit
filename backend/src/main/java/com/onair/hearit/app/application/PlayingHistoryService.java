package com.onair.hearit.app.application;

import com.onair.hearit.app.dto.request.PlayingHistoryRequest;
import com.onair.hearit.app.dto.response.PlayingHistoryResponse;
import com.onair.hearit.app.infrastructure.scheduler.PlayingHistoryBuffer;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.PlayingHistory;
import com.onair.hearit.common.domain.UserInfo;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.PlayingHistoryRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayingHistoryService {

    private static final int PLAYING_HISTORY_MAX_COUNT = 10;

    private final HearitRepository hearitRepository;
    private final PlayingHistoryRepository playingHistoryRepository;
    private final PlayingHistoryBuffer playingHistoryBuffer;

    public List<PlayingHistoryResponse> getRecentPlayingHistoryOfMember(UserInfo userInfo) {
        if (userInfo == null || userInfo.isGuest()) {
            return new ArrayList<>();
        }
        return toPlayingHistoryResponseForMember(userInfo.getMemberId());
    }

    private List<PlayingHistoryResponse> toPlayingHistoryResponseForMember(Long memberId) {
        List<PlayingHistory> histories = playingHistoryRepository.findByMemberIdOrderByUpdatedAtDesc(
                memberId, PLAYING_HISTORY_MAX_COUNT);
        Map<Long, Long> lastPlayTimeByHearitId = histories.stream()
                .collect(Collectors.toMap(PlayingHistory::getHearitId, PlayingHistory::getLastPlayTime,
                        (existing, ignored) -> existing, LinkedHashMap::new));
        Map<Long, Hearit> hearitMap = hearitRepository.findAllByIdIn(lastPlayTimeByHearitId.keySet().stream().toList())
                .stream().collect(Collectors.toMap(Hearit::getId, h -> h));
        return lastPlayTimeByHearitId.entrySet().stream()
                .map(e -> PlayingHistoryResponse.from(hearitMap.get(e.getKey()), e.getValue()))
                .toList();
    }

    public void addPlayingHistory(UserInfo userInfo, PlayingHistoryRequest request) {
        if (userInfo == null || userInfo.isGuest()) {
            return;
        }
        Hearit hearit = getHearitById(request.hearitId());
        PlayingHistory history = new PlayingHistory(userInfo.getMemberId(), hearit, request.lastPlayTime());
        playingHistoryBuffer.add(history);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
