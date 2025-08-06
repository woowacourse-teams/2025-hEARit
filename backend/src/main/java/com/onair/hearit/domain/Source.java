package com.onair.hearit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class Source {

    @Column(name = "sourceName")
    private String sourceName;

    @Column(name = "sourceUrl")
    private String sourceUrl;

    public Source(String sourceName, String sourceUrl) {
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
    }
}
