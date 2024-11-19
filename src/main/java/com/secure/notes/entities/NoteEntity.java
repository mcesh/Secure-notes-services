package com.secure.notes.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String content;

    private String ownerUsername;
}
