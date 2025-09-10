package com.onair.hearit.common.infrastructure.dto;

import com.onair.hearit.common.domain.Hearit;

public interface HearitWithPlayTimeProjection {
    Hearit getHearit();

    Long getLastPlayTime();
}
