package com.onair.hearit.domain;

import com.onair.hearit.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

    public static final int KEYWORD_NAME_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public Keyword(String name) {
        validate(name);
        this.name = name;
    }

    private void validate(String name) {
        if (name == null || name.trim().isBlank() || name.length() > KEYWORD_NAME_MAX_LENGTH) {
            throw new InvalidInputException("키워드 이름은 20자 이하여야 합니다.");
        }
    }

    public void updateName(String newName) {
        validate(newName);
        this.name = newName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Keyword keyword)) {
            return false;
        }
        if (this.id == null || keyword.id == null) {
            return false;
        }
        return Objects.equals(id, keyword.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
