package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.ReactionDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reaction",
        uniqueConstraints = {
                @UniqueConstraint(name = "reaction_unique_constraint", columnNames = {
                        "user_uuid", "hearit_id", "type"
                })
        })
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearit_id", nullable = false)
    private Hearit hearit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ReactionType type;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Reaction(UUID userUuid, Hearit hearit, ReactionType type) {
        validate(userUuid, hearit, type);
        this.userUuid = userUuid;
        this.hearit = hearit;
        this.type = type;
    }

    private void validate(UUID userUuid, Hearit hearit, ReactionType type) {
        if (userUuid == null) {
            throw new ReactionDomainException("userUuid는 null이 될 수 없습니다.");
        }
        if (hearit == null) {
            throw new ReactionDomainException("hearit은 null이 될 수 없습니다.");
        }
        if (type == null) {
            throw new ReactionDomainException("reactionType은 null이 될 수 없습니다.");
        }
    }
}
