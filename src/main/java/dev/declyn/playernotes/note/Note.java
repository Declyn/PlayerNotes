package dev.declyn.playernotes.note;

import org.bson.Document;

import java.util.UUID;

public class Note {

    private final int id;
    private final long timestamp;
    private final UUID author;
    private final String content;

    public Note(int id, long timestamp, UUID author, String content) {
        this.id = id;
        this.timestamp = timestamp;
        this.content = content;
        this.author = author;
    }

    public Note(Document document) {
        this.id = document.getInteger("_id");
        this.timestamp = document.getLong("timestamp");
        this.author = UUID.fromString(document.getString("author"));
        this.content = document.getString("content");
    }

    public Document asDocument() {
        return new Document("_id", id)
                .append("timestamp", timestamp)
                .append("author", author.toString())
                .append("content", content);
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

}