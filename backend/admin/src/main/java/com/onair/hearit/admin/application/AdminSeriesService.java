package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.SeriesCreateRequest;
import com.onair.hearit.admin.dto.response.UploadUrlResponse;
import com.onair.hearit.admin.infrastructure.s3.FileStorage;
import com.onair.hearit.core.domain.Series;
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
}
