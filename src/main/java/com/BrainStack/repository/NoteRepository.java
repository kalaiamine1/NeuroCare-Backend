package com.BrainStack.Repository;


import com.BrainStack.Entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository< Note, Long> {
}
