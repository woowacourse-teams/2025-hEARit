package com.onair.hearit.infrastructure.projection;

import com.onair.hearit.domain.Hearit;

public interface ExploredHearitProjection {

    Hearit getHearit();

    Long getCursorId();
}
