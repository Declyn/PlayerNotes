package dev.declyn.playernotes.user;

import dev.declyn.playernotes.note.Note;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID uniqueId;
    private String username;

    private final Set<Note> notes = new HashSet<>();

    public User(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
    }

    public Note getNote(int id) {
        return notes.stream().filter(note -> note.getId() == id).findAny().orElse(null);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Note> getNotes() {
        return notes;
    }

}