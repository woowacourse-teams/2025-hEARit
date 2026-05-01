package com.onair.hearit.app.series.presentation;

import com.onair.hearit.app.common.dto.request.PagingRequest;
import com.onair.hearit.app.common.dto.response.PagedResponse;
import com.onair.hearit.app.series.application.SeriesService;
import com.onair.hearit.app.series.dto.SeriesDetailResponse;
import com.onair.hearit.app.series.dto.SeriesOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/series")
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    public ResponseEntity<PagedResponse<SeriesOverviewResponse>> readSeriesList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PagingRequest pagingRequest = new PagingRequest(page, size);
        PagedResponse<SeriesOverviewResponse> responses = seriesService.getSeries(pagingRequest);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{seriesId}")
    public ResponseEntity<SeriesDetailResponse> readSeriesDetail(@PathVariable Long seriesId) {
        SeriesDetailResponse response = seriesService.getSeriesDetail(seriesId);
        return ResponseEntity.ok(response);
    }
}
