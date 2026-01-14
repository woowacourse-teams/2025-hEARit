package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.BookmarkDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmark",
        uniqueConstraints = {
                @UniqueConstraint(name = "bookmark_unique_constraint", columnNames = {
                        "member_uuid", "hearit_id"
                })
        })
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_uuid", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID memberUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearit_id", nullable = false)
    private Hearit hearit;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Bookmark(UUID memberUuid, Hearit hearit) {
        validate(memberUuid, hearit);
        this.memberUuid = memberUuid;
        this.hearit = hearit;
    }

    private void validate(UUID memberUuid, Hearit hearit) {
        validateMemberUuid(memberUuid);
        validateHearit(hearit);
    }

    private void validateMemberUuid(UUID memberUuid) {
        if (memberUuid == null) {
            throw new BookmarkDomainException("memberUuid는 null이 될 수 없습니다.");
        }
    }

    private void validateHearit(Hearit hearit) {
        if (hearit == null) {
            throw new BookmarkDomainException("히어릿은 null이 될 수 없습니다.");
        }
    }

    public boolean isCreatedBy(UUID memberUuid) {
        return this.memberUuid.equals(memberUuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bookmark bookmark)) {
            return false;
        }
        if (this.id == null || bookmark.id == null) {
            return false;
        }
        return Objects.equals(id, bookmark.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
