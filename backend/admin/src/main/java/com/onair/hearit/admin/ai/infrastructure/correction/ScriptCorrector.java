package com.onair.hearit.admin.ai.infrastructure.correction;

import com.onair.hearit.admin.ai.dto.ScriptSegment;
import java.util.List;


public interface ScriptCorrector {

    List<ScriptSegment> correct(List<ScriptSegment> rawSegments);
}
