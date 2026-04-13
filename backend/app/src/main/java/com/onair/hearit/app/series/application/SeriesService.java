package com.onair.hearit.app.series.application;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.exception.custom.NotFoundException;
import com.onair.hearit.app.series.dto.SeriesDetailResponse;
import com.onair.hearit.app.series.dto.SeriesOverviewResponse;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Series;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import com.onair.hearit.core.infrastructure.jpa.SeriesRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final HearitRepository hearitRepository;

    @Transactional(readOnly = true)
    public PagedResponse<SeriesOverviewResponse> getSeries(PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size(),
                Sort.by(Sort.Order.desc("id")));
        Page<Series> seriesPage = seriesRepository.findAll(pageable);
        Page<SeriesOverviewResponse> dtoPage = seriesPage.map(SeriesOverviewResponse::from);
        return PagedResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public SeriesDetailResponse getSeriesDetail(Long seriesId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("seriesId", seriesId.toString()));
        List<Hearit> hearits = hearitRepository.findBySeriesOrderByCreatedAtDesc(seriesId);
        return SeriesDetailResponse.from(series, hearits);
    }
}
