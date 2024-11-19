package com.secure.notes.controllers;

import com.secure.notes.entities.NoteEntity;
import com.secure.notes.services.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Slf4j
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public NoteEntity createNote(@RequestBody String content, @AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        log.info("USER DETAILS: {} ", username);
        return noteService.createNoteForUser(username, content);
    }

    @GetMapping
    public List<NoteEntity> getUserNotes(@AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        log.info("Retrieving notes for user: {} ", username);
        List<NoteEntity> notes = noteService.getNoteForUser(username);
        log.info("Notes retrieved: {} ", notes.size());
        return notes;
    }

    @PutMapping("/{noteId}")
    public NoteEntity updateNote(@PathVariable Long noteId, @RequestBody String content,
                                 @AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        return noteService.updateNoteForUser(noteId,content,username);
    }

    @DeleteMapping("/{noteId}")
    public void deleteNote(@PathVariable Long noteId, @AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        noteService.deleteNoteForUser(noteId, username);
    }
}
