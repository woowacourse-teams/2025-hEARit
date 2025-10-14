package com.onair.hearit.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.onair.hearit.core.domain.exception.HearitDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SourceTest {

    @Test
    @DisplayName("정상적인 입력으로 Source 객체를 생성할 수 있다")
    void createValidSource() {
        // given
        String name = "출처명";
        String url = "https://example.com";

        // when
        Source source = new Source(name, url);

        // then
        assertThat(source.getSourceName()).isEqualTo(name);
        assertThat(source.getSourceUrl()).isEqualTo(url);
    }

    @Test
    @DisplayName("sourceName이 null이면 예외가 발생한다")
    void createSourceWithNullName() {
        assertThatThrownBy(() -> new Source(null, "https://example.com"))
                .isInstanceOf(HearitDomainException.class)
                .hasMessageContaining("sourceName");
    }

    @Test
    @DisplayName("sourceName이 250자를 초과하면 예외가 발생한다")
    void createSourceWithTooLongName() {
        String longName = "a".repeat(251);
        assertThatThrownBy(() -> new Source(longName, "https://example.com"))
                .isInstanceOf(HearitDomainException.class)
                .hasMessageContaining("sourceName");
    }

    @Test
    @DisplayName("sourceUrl이 500자를 초과하면 예외가 발생한다")
    void createSourceWithTooLongUrl() {
        String longUrl = "a".repeat(501);
        assertThatThrownBy(() -> new Source("sourceName", longUrl))
                .isInstanceOf(HearitDomainException.class)
                .hasMessageContaining("sourceUrl");
    }
}
