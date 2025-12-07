package com.onair.hearit.app.playinghistory.application;

import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.playinghistory.dto.PlayingHistoryRequest;
import com.onair.hearit.app.playinghistory.dto.RecentlyPlayedHearitResponse;
import com.onair.hearit.app.playinghistory.infrastructure.scheduler.PlayingHistoryBuffer;
import com.onair.hearit.app.userinfo.application.UserInfoService;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.PlayingHistoryRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayingHistoryService {

    private static final int PLAYING_HISTORY_MAX_COUNT = 10;

    private final HearitRepository hearitRepository;
    private final PlayingHistoryRepository playingHistoryRepository;
    private final PlayingHistoryBuffer playingHistoryBuffer;
    private final UserInfoService userInfoService;

    @Transactional(readOnly = true)
    public List<RecentlyPlayedHearitResponse> getRecentPlayingHistory(UserInfo userInfo) {
        String userUuid = userInfoService.getUuid(userInfo);
        return toPlayingHistoryResponse(userUuid);
    }

    private List<RecentlyPlayedHearitResponse> toPlayingHistoryResponse(String userUuid) {
        List<PlayingHistory> histories = playingHistoryRepository.findByUserUuidOrderByUpdatedAtDesc(
                userUuid, PLAYING_HISTORY_MAX_COUNT);
        Map<Long, Long> lastPlayTimeByHearitId = mapHearitIdToLastPlayTime(histories);
        Map<Long, Hearit> hearitMap = mapHearitIdToHearit(lastPlayTimeByHearitId.keySet());
        return lastPlayTimeByHearitId.entrySet().stream()
                .map(entry -> RecentlyPlayedHearitResponse.from(hearitMap.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    private Map<Long, Long> mapHearitIdToLastPlayTime(List<PlayingHistory> histories) {
        return histories.stream()
                .collect(Collectors.toMap(
                        PlayingHistory::getHearitId,      // Key: hearitId
                        PlayingHistory::getLastPlayTime,  // Value: lastPlayTime
                        (existing, ignored) -> existing,  // 중복 key 처리
                        LinkedHashMap::new // DB에서 가져온 순서(최신순) 유지
                ));
    }

    private Map<Long, Hearit> mapHearitIdToHearit(Set<Long> hearitIds) {
        return hearitRepository.findAllByIdIn(hearitIds.stream().toList()).stream()
                .collect(Collectors.toMap(
                        Hearit::getId, // Key: hearitId
                        hearit -> hearit // Value: Hearit 객체
                ));
    }

    public void addPlayingHistory(UserInfo userInfo, PlayingHistoryRequest request) {
        Hearit hearit = getHearitById(request.hearitId());
        String userUuid = userInfoService.getUuid(userInfo);
        PlayingHistory history = new PlayingHistory(userUuid, hearit, request.lastPlayTime());
        playingHistoryBuffer.add(history);
    }

    private Hearit getHearitById(Long hearitId) {
        return hearitRepository.findById(hearitId)
                .orElseThrow(() -> new NotFoundException("hearitId", hearitId.toString()));
    }
}
