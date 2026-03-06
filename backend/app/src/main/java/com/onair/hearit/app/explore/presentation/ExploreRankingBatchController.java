package com.onair.hearit.app.explore.presentation;

import com.onair.hearit.app.cluster.application.HearitClusterBatchService;
import com.onair.hearit.app.explore.application.ExploreRankingBatchService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/batch")
public class ExploreRankingBatchController {

    private final HearitClusterBatchService hearitClusterBatchService;
    private final ExploreRankingBatchService exploreRankingBatchService;

    @PostMapping("/explore-ranking")
    public ResponseEntity<Void> runBatch(HttpServletRequest request) {
        try {
            InetAddress address = InetAddress.getByName(request.getRemoteAddr());
            if (!address.isLoopbackAddress()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (UnknownHostException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("Manual batch triggered. remoteIp={}", request.getRemoteAddr());
        hearitClusterBatchService.runClustering();
        exploreRankingBatchService.runRankingForExplore();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
