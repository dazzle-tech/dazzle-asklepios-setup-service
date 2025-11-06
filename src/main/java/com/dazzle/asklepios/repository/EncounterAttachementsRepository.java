package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.EncounterAttachments;
import com.dazzle.asklepios.domain.enumeration.EncounterAttachmentSource;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncounterAttachementsRepository extends JpaRepository<EncounterAttachments, Long> {
    List<EncounterAttachments> findByEncounterIdInAndDeletedAtIsNullOrderByCreatedDateDesc(List<Long> encounterId);
    List<EncounterAttachments> findByEncounterIdAndSourceAndDeletedAtIsNullOrderByCreatedDateDesc(Long encounterId, EncounterAttachmentSource source);
    List<EncounterAttachments> findByEncounterIdAndSourceAndSourceIdAndDeletedAtIsNullOrderByCreatedDateDesc(Long encounterId, EncounterAttachmentSource source, Long sourceId);

    Optional<EncounterAttachments> findByIdAndDeletedAtIsNull(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update EncounterAttachments e set e.deletedAt = CURRENT_TIMESTAMP where e.id = :id and e.deletedAt is null")
    int softDelete(Long id);

}
