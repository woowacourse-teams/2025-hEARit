package com.onair.hearit.core.infrastructure.projection;

import com.onair.hearit.core.domain.Hearit;

public interface ExploredHearitProjection {

    Hearit getHearit();

    Long getCursorId();
}
