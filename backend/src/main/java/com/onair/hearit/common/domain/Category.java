package com.onair.hearit.common.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    public static final int CATEGORY_NAME_MAX_LENGTH = 15;
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color_code", nullable = false)
    private String colorCode;

    public Category(String name, String colorCode) {
        validate(name, colorCode);
        this.name = name;
        this.colorCode = colorCode;
    }

    private void validate(String name, String colorCode) {
        validateName(name);
        validateColorCode(colorCode);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isBlank() || name.length() > CATEGORY_NAME_MAX_LENGTH) {
            throw new InvalidInputException("카테고리 이름은 15자 이하여야 합니다.");
        }
    }

    private void validateColorCode(String colorCode) {
        if (colorCode == null || !COLOR_CODE_PATTERN.matcher(colorCode).matches()) {
            throw new InvalidInputException("컬러 코드는 '#'으로 시작하고 6자리 16진수여야 합니다.");
        }
    }

    public void update(String name, String colorCode) {
        validate(name, colorCode);
        this.name = name;
        this.colorCode = colorCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Category category)) {
            return false;
        }
        if (this.id == null || category.id == null) {
            return false;
        }
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
