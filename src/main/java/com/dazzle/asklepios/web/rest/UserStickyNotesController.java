package com.dazzle.asklepios.web.rest;

import com.dazzle.asklepios.domain.UserStickyNotes;
import com.dazzle.asklepios.service.UserStickyNotesService;
import com.dazzle.asklepios.web.rest.vm.userStickyNotes.UserStickyNotesCreateVM;
import com.dazzle.asklepios.web.rest.vm.userStickyNotes.UserStickyNotesResponseVM;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
public class UserStickyNotesController {

    private static final Logger LOG = LoggerFactory.getLogger(UserStickyNotesController.class);

    private final UserStickyNotesService userStickyNotesService;

    public UserStickyNotesController(UserStickyNotesService userStickyNotesService) {
        this.userStickyNotesService = userStickyNotesService;
    }




    @GetMapping("/user-sticky-notes/{user_id}")
    public ResponseEntity<List<UserStickyNotesResponseVM>> findAllByUserId(
            @PathVariable long user_id
    ) {
        LOG.debug("REST list User Sticky Notes user_id={}", user_id);

        final List<UserStickyNotes> list = userStickyNotesService.findAllByUserId(user_id);

        List<UserStickyNotesResponseVM> dtoList = list.stream()
                .map(UserStickyNotesResponseVM::ofEntity)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/user-sticky-notes")
    public ResponseEntity<UserStickyNotesResponseVM> createUserStickyNotes(
            @Valid @RequestBody UserStickyNotesCreateVM vm
    ) {
        LOG.debug("REST create User sticky Note payload={}", vm);

        UserStickyNotes toCreate = UserStickyNotes.builder()
                .note(vm.note())
                .priority(vm.priority())
                .color(vm.color())
                .userId(vm.userId())
                .priorityOrder(vm.priorityOrder())
                .build();

        UserStickyNotes created = userStickyNotesService.create(toCreate);
        UserStickyNotesResponseVM body = UserStickyNotesResponseVM.ofEntity(created);

        return ResponseEntity
                .created(URI.create("/api/setup/user-sticky-notes/" + created.getId()))
                .body(body);
    }

    @DeleteMapping("/user-sticky-notes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOG.debug("REST request to delete user sticky note id={}", id);
        boolean removed = userStickyNotesService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
