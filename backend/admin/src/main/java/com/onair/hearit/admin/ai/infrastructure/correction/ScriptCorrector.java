package com.onair.hearit.admin.ai.infrastructure.correction;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import java.util.List;

/**
 * 대본을 교정하는 행위를 정의하는 인터페이스
 *
 * 책임:
 * - STT로 생성된 원본 대본의 오타/전문용어 교정
 * - 타임스탬프(start, end)는 유지
 */
public interface ScriptCorrector {

    /**
     * 대본 교정
     *
     * @param rawSegments STT로 생성된 원본 세그먼트 목록
     * @return 교정된 세그먼트 목록 (id, start, end는 유지, text만 교정)
     */
    List<ScriptSegment> correct(List<ScriptSegment> rawSegments);
}
