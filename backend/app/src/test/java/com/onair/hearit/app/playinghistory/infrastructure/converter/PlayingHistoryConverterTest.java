package com.onair.hearit.app.playinghistory.infrastructure.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import com.onair.hearit.app.playinghistory.infrastructure.buffer.PlayHistoryValue;
import com.onair.hearit.core.domain.Category;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.PlayingHistory;
import com.onair.hearit.core.infrastructure.jpa.HearitRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayingHistoryConverterTest {

    @Mock
    HearitRepository hearitRepository;

    PlayingHistoryConverter converter;

    @BeforeEach
    void setup() {
        converter = new PlayingHistoryConverter(hearitRepository);
    }

    @Test
    @DisplayName("빈 컬렉션을 변환하면 빈 리스트를 반환한다")
    void toPlayingHistories_emptyCollection() {
        // given
        List<PlayHistoryValue> emptyList = List.of();

        // when
        List<PlayingHistory> result = converter.toPlayingHistories(emptyList);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("PlayValue 컬렉션을 PlayingHistory 리스트로 변환한다")
    void toPlayingHistories_success() throws Exception {
        // given
        Category category = new Category("test", "#000000");
        Hearit hearit1 = createHearitWithId(1L, category, "title1");
        Hearit hearit2 = createHearitWithId(2L, category, "title2");

        when(hearitRepository.findAllById(anySet())).thenReturn(List.of(hearit1, hearit2));

        PlayHistoryValue value1 = new PlayHistoryValue("user-uuid-1", 1L, 5000L, 1000L);
        PlayHistoryValue value2 = new PlayHistoryValue("user-uuid-2", 2L, 10000L, 2000L);

        // when
        List<PlayingHistory> result = converter.toPlayingHistories(List.of(value1, value2));

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).getUserUuid()).isEqualTo("user-uuid-1"),
                () -> assertThat(result.get(0).getHearitId()).isEqualTo(1L),
                () -> assertThat(result.get(0).getLastPlayTime()).isEqualTo(5000L),
                () -> assertThat(result.get(1).getUserUuid()).isEqualTo("user-uuid-2"),
                () -> assertThat(result.get(1).getHearitId()).isEqualTo(2L),
                () -> assertThat(result.get(1).getLastPlayTime()).isEqualTo(10000L)
        );
    }

    @Test
    @DisplayName("PlayValue.from()으로 PlayingHistory에서 PlayValue를 생성한다")
    void playValue_from() throws Exception {
        // given
        Category category = new Category("test", "#000000");
        Hearit hearit = createHearitWithId(1L, category, "title");
        PlayingHistory history = new PlayingHistory("user-uuid", hearit, 5000L);
        long clientEventTime = 1000L;

        // when
        PlayHistoryValue playHistoryValue = PlayHistoryValue.from(history, clientEventTime);

        // then
        assertAll(
                () -> assertThat(playHistoryValue.userUuid()).isEqualTo("user-uuid"),
                () -> assertThat(playHistoryValue.hearitId()).isEqualTo(1L),
                () -> assertThat(playHistoryValue.lastPlayTime()).isEqualTo(5000L),
                () -> assertThat(playHistoryValue.clientEventTime()).isEqualTo(1000L)
        );
    }

    @Test
    @DisplayName("isMoreRecentThan()으로 최신 데이터를 판별한다")
    void playValue_isMoreRecentThan() {
        // given
        PlayHistoryValue older = new PlayHistoryValue("user", 1L, 5000L, 1000L);
        PlayHistoryValue newer = new PlayHistoryValue("user", 1L, 3000L, 2000L);

        // when & then
        assertAll(
            () -> assertThat(newer.isMoreRecentThan(older)).isTrue(),
            () -> assertThat(older.isMoreRecentThan(newer)).isFalse(),
            () -> assertThat(older.isMoreRecentThan(null)).isFalse()
        );
    }

    private Hearit createHearitWithId(Long id, Category category, String title) throws Exception {
        String uuid = "test-uuid-123";
        Hearit hearit = new Hearit(
                title,
                "summary",
                100,
                "/hearit/audio/original/ORG_" + uuid + ".mp3",
                "/hearit/audio/short/SHR_" + uuid + ".mp3",
                "/hearit/script/SCR_" + uuid + ".json",
                List.of(),
                category
        );
        Field idField = Hearit.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(hearit, id);
        return hearit;
    }
}
