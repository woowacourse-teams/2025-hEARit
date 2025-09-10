package com.onair.hearit.common.infrastructure.jpa;

import com.onair.hearit.common.domain.Hearit;

public interface HearitWithPlayTime {
    Hearit getHearit();

    Long getLastPlayTime();
}
