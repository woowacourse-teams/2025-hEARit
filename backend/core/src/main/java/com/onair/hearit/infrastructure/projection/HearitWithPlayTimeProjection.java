package com.onair.hearit.infrastructure.projection;

import com.onair.hearit.domain.Hearit;

public interface HearitWithPlayTimeProjection {
    Hearit getHearit();

    Long getLastPlayTime();
}
