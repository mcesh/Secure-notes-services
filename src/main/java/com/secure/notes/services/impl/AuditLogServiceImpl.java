package com.secure.notes.services.impl;

import com.secure.notes.entities.AuditLog;
import com.secure.notes.entities.NoteEntity;
import com.secure.notes.repositories.AuditLogRepository;
import com.secure.notes.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    @Override
    public void logNoteCreation(String username, NoteEntity note) {
        AuditLog log = new AuditLog();
        log.setAction("CREATE");
        log.setUsername(username);
        log.setNoteId(note.getId());
        log.setNoteContent(note.getContent());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Transactional
    @Override
    public void logNoteUpdate(String username, NoteEntity note) {

        AuditLog log = new AuditLog();
        log.setAction("UPDATE");
        log.setUsername(username);
        log.setNoteId(note.getId());
        log.setNoteContent(note.getContent());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Transactional
    @Override
    public void logNoteDeletion(String username, Long noteId) {

        AuditLog log = new AuditLog();
        log.setAction("DELETE");
        log.setUsername(username);
        log.setNoteId(noteId);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Transactional
    @Override
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    @Transactional
    @Override
    public List<AuditLog> getAuditLogsForNoteId(Long id) {
        return auditLogRepository.findByNoteId(id);
    }
}
