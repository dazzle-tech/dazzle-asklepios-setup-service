package com.dazzle.asklepios.web.rest.vm.userStickyNotes;

import com.dazzle.asklepios.domain.UserStickyNotes;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record UserStickyNotesResponseVM(
        Long id,
        Long userId,
        String note,
        String priority,
        BigDecimal priorityOrder,
        String color,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) implements Serializable {

    public static UserStickyNotesResponseVM ofEntity(UserStickyNotes userStickyNotes) {
        return new UserStickyNotesResponseVM(
                userStickyNotes.getId(),
                userStickyNotes.getUserId(),
                userStickyNotes.getNote(),
                userStickyNotes.getPriority(),
                userStickyNotes.getPriorityOrder(),
                userStickyNotes.getColor(),
                userStickyNotes.getCreatedBy(),
                userStickyNotes.getCreatedDate(),
                userStickyNotes.getLastModifiedBy(),
                userStickyNotes.getLastModifiedDate()
        );
    }
}
