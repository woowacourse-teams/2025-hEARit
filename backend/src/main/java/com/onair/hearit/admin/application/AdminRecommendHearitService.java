package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.request.AdminPagingRequest;
import com.onair.hearit.admin.dto.request.RecommendHearitCreateRequest;
import com.onair.hearit.admin.dto.request.RecommendHearitUpdateRequest;
import com.onair.hearit.admin.dto.response.AdminPagedResponse;
import com.onair.hearit.admin.dto.response.AdminRecommendHearitResponse;
import com.onair.hearit.admin.dto.response.MonthlyRecommendHearitResponse;
import com.onair.hearit.admin.exception.custom.AdminInvalidInputException;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.RecommendHearit;
import com.onair.hearit.common.infrastructure.jpa.HearitRepository;
import com.onair.hearit.common.infrastructure.jpa.RecommendHearitRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRecommendHearitService {

    private static final int RECOMMEND_HEARIT_COUNT = 5;

    private final HearitRepository hearitRepository;
    private final RecommendHearitRepository recommendHearitRepository;

    public AdminPagedResponse<AdminRecommendHearitResponse> getHearits(AdminPagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(pagingRequest.page(), pagingRequest.size());
        Page<Hearit> hearits = hearitRepository.findAll(pageable);
        Page<AdminRecommendHearitResponse> dtoPage = hearits.map(
                hearit -> AdminRecommendHearitResponse.of(hearit, getLastRecommendDate(hearit)));
        return AdminPagedResponse.from(dtoPage);
    }

    private LocalDate getLastRecommendDate(Hearit hearit) {
        Optional<RecommendHearit> recommendHearit = recommendHearitRepository.findRecentByHearitId(hearit.getId());
        return recommendHearit.map(RecommendHearit::getRecommendDate).orElse(null);
    }

    public List<MonthlyRecommendHearitResponse> getMonthRecommendHearit(Integer year, Integer month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = YearMonth.of(year, month).atEndOfMonth();
        List<RecommendHearit> recommendHearits = recommendHearitRepository.findByRecommendDateIsBetween(from, to);

        List<MonthlyRecommendHearitResponse> responses = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LocalDate target = date;
            List<Hearit> hearits = recommendHearits.stream()
                    .filter(recommendHearit -> recommendHearit.getRecommendDate().isEqual(target))
                    .map(RecommendHearit::getHearit)
                    .toList();
            responses.add(MonthlyRecommendHearitResponse.from(target, hearits));
        }
        return responses;
    }

    @Transactional
    public void addRecommendHearits(RecommendHearitCreateRequest request) {
        validateForCreateRecommendHearit(request.recommendDate(), request.hearitIds());
        List<Hearit> hearits = getHearitsById(request.hearitIds());
        List<RecommendHearit> recommendHearits = hearits.stream()
                .map(h -> new RecommendHearit(h, request.recommendDate()))
                .toList();
        recommendHearitRepository.saveAll(recommendHearits);
    }

    @Transactional
    public void modifyRecommendHearits(RecommendHearitUpdateRequest request) {
        validateForCreateRecommendHearit(request.recommendDate(), request.hearitIds());
        List<Hearit> hearits = getHearitsById(request.hearitIds());
        int deletedRowCount = recommendHearitRepository.deleteAllByRecommendDate(request.recommendDate());
        if (deletedRowCount != RECOMMEND_HEARIT_COUNT) {
            throw new AdminInvalidInputException("추천 히어릿 아이디가 유효하지 않습니다.");
        }
        List<RecommendHearit> recommendHearits = hearits.stream()
                .map(h -> new RecommendHearit(h, request.recommendDate()))
                .toList();
        recommendHearitRepository.saveAll(recommendHearits);
    }

    private void validateForCreateRecommendHearit(LocalDate recommendDate, List<Long> hearitIds) {
        LocalDate today = LocalDate.now();
        if (recommendDate.isBefore(today)) {
            throw new AdminInvalidInputException("과거의 추천히어릿은 생성할 수 없습니다.");
        }
        if (hearitIds.size() != RECOMMEND_HEARIT_COUNT) {
            throw new AdminInvalidInputException("추천 히어릿은 반드시 5개여야합니다.");
        }
    }

    private List<Hearit> getHearitsById(List<Long> hearitIds) {
        List<Hearit> hearits = hearitRepository.findAllByIdIn(hearitIds);
        if (hearits.size() != RECOMMEND_HEARIT_COUNT) {
            throw new AdminInvalidInputException("추천 히어릿은 반드시 5개여야합니다.");
        }
        return hearits;
    }
}
