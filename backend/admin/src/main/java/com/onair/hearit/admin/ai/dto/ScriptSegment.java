package com.onair.hearit.admin.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScriptSegment {

    private Integer id;
    private Integer start;
    private Integer end;
    private String text;

    public static ScriptSegment of(int id, double startSeconds, double endSeconds, String text) {
        return new ScriptSegment(
                id,
                (int) (startSeconds * 1000),
                (int) (endSeconds * 1000),
                text.trim()
        );
    }
}
