package com.onair.hearit.admin.ai.infrastructure.stt;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SttResponse {

    private final double duration;
    private final List<ScriptSegment> segments;
    private final long latencyMs;
    private final String provider;

    public int getDurationSeconds() {
        return (int) Math.ceil(duration);
    }

    public boolean hasSegments() {
        return segments != null && !segments.isEmpty();
    }
}
