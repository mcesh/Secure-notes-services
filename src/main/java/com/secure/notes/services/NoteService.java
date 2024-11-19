package com.secure.notes.services;

import com.secure.notes.entities.NoteEntity;

import java.util.List;

public interface NoteService {

    NoteEntity createNoteForUser(String username, String content);

    NoteEntity updateNoteForUser(Long noteId, String content, String username);

    void deleteNoteForUser(Long noteId, String username);

    List<NoteEntity> getNoteForUser(String username);
}
