package com.secure.notes.repositories;

import com.secure.notes.entities.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<NoteEntity, Long> {
    List<NoteEntity> findByOwnerUsername(String ownerUsername);
}
