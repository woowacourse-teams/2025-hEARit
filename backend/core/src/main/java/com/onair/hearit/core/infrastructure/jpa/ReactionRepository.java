package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.Reaction;
import com.onair.hearit.core.domain.ReactionType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByUserUuidAndHearitAndType(UUID uuid, Hearit hearit, ReactionType type);

    long countByHearitAndType(Hearit hearit, ReactionType type);

    boolean existsByHearitAndUserUuidAndType(Hearit hearit, UUID userUuid, ReactionType type);
}
