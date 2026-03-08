package com.onair.hearit.app.explore.presentation;

import com.onair.hearit.app.explore.application.ExploreRankingFacade;
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

    private final ExploreRankingFacade exploreRankingFacade;

    @PostMapping("/explore-ranking")
    public ResponseEntity<String> runBatch(HttpServletRequest request) {
        if (!isLoopbackRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean executed = exploreRankingFacade.runWithLock();
        if (!executed) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Batch job is already in progress by another instance or scheduler.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Batch job executed successfully.");
    }

    private boolean isLoopbackRequest(HttpServletRequest request) {
        try {
            InetAddress address = InetAddress.getByName(request.getRemoteAddr());
            return address.isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
