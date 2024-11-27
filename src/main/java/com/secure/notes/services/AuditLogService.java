package com.secure.notes.services;

import com.secure.notes.entities.AuditLog;
import com.secure.notes.entities.NoteEntity;

import java.util.List;

public interface AuditLogService {

    void logNoteCreation(String username, NoteEntity note);

    void logNoteUpdate(String username, NoteEntity note);

    void logNoteDeletion(String username, Long noteId);

    List<AuditLog> getAllAuditLogs();

    List<AuditLog> getAuditLogsForNoteId(Long id);
}
