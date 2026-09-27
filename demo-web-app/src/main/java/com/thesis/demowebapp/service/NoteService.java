package com.thesis.demowebapp.service;

import com.thesis.demowebapp.entity.Note;
import com.thesis.demowebapp.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public List<Note> getNotesForOwner(Long ownerId) {
        return noteRepository.findByOwnerId(ownerId);
    }

    /**
     * Returns a single note by ID.
     *
     * <p>VULNERABILITY-TP [AUTHZ-003]: Insecure Direct Object Reference
     * <br>Risk: Any authenticated user can view any other user's
     *   note by guessing or enumerating note IDs. There is no
     *   check that the requesting user owns the note.
     * <br>Expected LLM classification: TRUE_POSITIVE
     * <br>Context: No ownership verification against the
     *   currently authenticated principal.
     */
    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Note not found: " + id));
    }
}
