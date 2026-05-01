package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.SeriesCreateRequest;
import com.onair.hearit.admin.dto.response.UploadUrlResponse;
import com.onair.hearit.admin.exception.custom.AdminInvalidInputException;
import com.onair.hearit.admin.exception.custom.AdminNotFoundException;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitSeries;
import com.onair.hearit.core.domain.Series;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitSeriesRepository;
import com.onair.hearit.core.infrastructure.jpa.SeriesRepository;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSeriesService {

    private static final String SERIES_IMAGE_PATH = "/series/image/";

    private final SeriesRepository seriesRepository;
    private final HearitRepository hearitRepository;
    private final HearitSeriesRepository hearitSeriesRepository;
    private final FileStorage fileStorage;

    public UploadUrlResponse getSeriesImageUploadUrl() {
        String key = SERIES_IMAGE_PATH + UUID.randomUUID() + ".jpg";
        URL uploadUrl = fileStorage.createPutUrl(key);
        return new UploadUrlResponse(key, uploadUrl);
    }

    @Transactional
    public void addSeries(SeriesCreateRequest request) {
        Series series = new Series(request.title(), request.description(), request.imageKey());
        seriesRepository.save(series);
    }

    @Transactional
    public void addHearitToSeries(Long seriesId, Long hearitId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new AdminNotFoundException("seriesId", seriesId.toString()));
        Hearit hearit = hearitRepository.findById(hearitId)
                .orElseThrow(() -> new AdminNotFoundException("hearitId", hearitId.toString()));
        if (hearitSeriesRepository.existsByHearitIdAndSeriesId(hearitId, seriesId)) {
            throw new AdminInvalidInputException("이미 해당 시리즈에 포함된 히어릿입니다. hearitId: " + hearitId);
        }
        hearitSeriesRepository.save(HearitSeries.of(hearit, series));
    }

    @Transactional
    public void removeHearitFromSeries(Long seriesId, Long hearitId) {
        if (!seriesRepository.existsById(seriesId)) {
            throw new AdminNotFoundException("seriesId", seriesId.toString());
        }
        if (!hearitRepository.existsById(hearitId)) {
            throw new AdminNotFoundException("hearitId", hearitId.toString());
        }
        if (!hearitSeriesRepository.existsByHearitIdAndSeriesId(hearitId, seriesId)) {
            throw new AdminNotFoundException("hearitId + seriesId", hearitId + " + " + seriesId);
        }
        hearitSeriesRepository.deleteByHearitIdAndSeriesId(hearitId, seriesId);
    }
}
