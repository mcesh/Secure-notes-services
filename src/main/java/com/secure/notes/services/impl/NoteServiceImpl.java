package com.secure.notes.services.impl;

import com.secure.notes.entities.NoteEntity;
import com.secure.notes.repositories.NoteRepository;
import com.secure.notes.services.AuditLogService;
import com.secure.notes.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final AuditLogService auditLogService;

    @Override
    public NoteEntity createNoteForUser(String username, String content) {
        NoteEntity noteEntity =  new NoteEntity();
        noteEntity.setContent(content);
        noteEntity.setOwnerUsername(username);
        auditLogService.logNoteCreation(username, noteEntity);
        return noteRepository.save(noteEntity);
    }

    @Override
    public NoteEntity updateNoteForUser(Long noteId, String content, String username) {
        NoteEntity note = noteRepository.findById(noteId).orElseThrow(() -> new RuntimeException("Note not found"));
        note.setContent(content);
        NoteEntity updatedNote = noteRepository.save(note);
        auditLogService.logNoteUpdate(username, updatedNote);
        return updatedNote;
    }

    @Override
    public void deleteNoteForUser(Long noteId, String username) {
        NoteEntity note = noteRepository.findById(noteId).orElseThrow(
                () -> new RuntimeException("Note not found")
        );
        auditLogService.logNoteDeletion(username, noteId);
        noteRepository.delete(note);
    }

    @Override
    @Transactional
    public List<NoteEntity> getNoteForUser(String username) {
        List<NoteEntity> noteEntities = noteRepository.findByOwnerUsername(username);
        return noteEntities;
    }
}
