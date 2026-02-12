package com.onair.hearit.app.cluster.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HearitClusterFeatureLoader {

    private final FeatureProcessor featureProcessor;

    /**
     * 히어릿 통계 데이터를 페이지 단위로 로드하여 Feature 테이블에 적재합니다.
     * 1. 메모리 효율성: 페이지 단위로 트랜잭션을 분리하여 영속성 컨텍스트 부하 방지
     * 2. 데이터 정합성:
     * - 실패 시 해당 페이지 이후 데이터는 적재되지 않음(Missing Row)
     * - 쓰레기 값(Garbage Data)이 남는 것보다 누락된 상태가 장애 인지 및 분석 제외에 유리
     * 3. 멱등성 확보: Upsert(On Duplicate Key Update) 구조이므로 실패 시 처음부터 재실행 가능
     */
    public void loadStatisticsFeature(int pageSize) {
        int pageNumber = 0;
        boolean hasNext;

        do {
            hasNext = featureProcessor.processPage(pageNumber, pageSize);
            pageNumber++;
        } while (hasNext);
    }
}
