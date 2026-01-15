package com.onair.hearit.admin.ai.dto.request;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ScriptUpdateRequest(
        @NotEmpty List<ScriptSegment> segments
) {
}
