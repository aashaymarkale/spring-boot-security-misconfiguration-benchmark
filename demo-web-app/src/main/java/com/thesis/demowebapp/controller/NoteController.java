package com.thesis.demowebapp.controller;

import com.thesis.demowebapp.entity.Note;
import com.thesis.demowebapp.entity.User;
import com.thesis.demowebapp.service.NoteService;
import com.thesis.demowebapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    /**
     * GET /notes — lists the current user's own notes.
     * Correctly scoped to the authenticated user — no IDOR risk here.
     */
    @GetMapping
    public String listNotes(Authentication auth, Model model) {
        User user = userService.findByUsername(auth.getName());
        model.addAttribute("notes",
            noteService.getNotesForOwner(user.getId()));
        model.addAttribute("username", user.getDisplayName());
        return "notes";
    }

    /**
     * GET /notes/{id} — view a single note by ID.
     *
     * VULNERABILITY-TP [AUTHZ-003] (see NoteService.getNoteById):
     * No check that the note belongs to the authenticated user.
     * Any logged-in user (alice or bob) can view the other's
     * note simply by changing the ID in the URL, e.g.
     * navigating from /notes/1 to /notes/2.
     */
    @GetMapping("/{id}")
    public String viewNote(@PathVariable Long id, Model model) {
        Note note = noteService.getNoteById(id);
        model.addAttribute("note", note);
        return "note-detail";
    }
}
