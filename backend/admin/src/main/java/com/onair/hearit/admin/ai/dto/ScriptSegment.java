package com.onair.hearit.admin.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScriptSegment {

    private Integer id;     // 세그먼트 순번
    private Integer start;  // 시작 시간 (밀리초)
    private Integer end;    // 종료 시간 (밀리초)
    private String text;    // 대본 텍스트

    public static ScriptSegment of(int id, double startSeconds, double endSeconds, String text) {
        return new ScriptSegment(
                id,
                (int) (startSeconds * 1000),
                (int) (endSeconds * 1000),
                text.trim()
        );
    }
}
