package com.onair.hearit.app.playinghistory.infrastructure.buffer;

import java.util.UUID;

public record PlayHistoryKey(UUID userUuid, long hearitId) {
}
