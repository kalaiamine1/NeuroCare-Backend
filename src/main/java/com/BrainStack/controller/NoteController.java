package com.brainstack.controller;


import com.brainstack.entity.Note;
import com.brainstack.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "http://localhost:4200") // permet la communication Angular <-> Spring
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    // 📥 Ajouter une note
    @PostMapping
    public Note createNote(@RequestBody Note note) {
        return noteRepository.save(note);
    }

    // 📤 Récupérer toutes les notes
    @GetMapping
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    // ❌ Supprimer une note
    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable Long id) {
        noteRepository.deleteById(id);
    }
}
