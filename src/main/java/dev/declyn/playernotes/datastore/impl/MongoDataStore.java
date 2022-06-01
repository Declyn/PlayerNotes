package dev.declyn.playernotes.datastore.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.MongoCollection;
import dev.declyn.playernotes.PlayerNotesPlugin;
import dev.declyn.playernotes.datastore.UserDataStore;
import dev.declyn.playernotes.mongodb.Mongo;
import dev.declyn.playernotes.mongodb.subscribers.CommandSubscriber;
import dev.declyn.playernotes.mongodb.subscribers.OnAnySubscriber;
import dev.declyn.playernotes.note.Note;
import dev.declyn.playernotes.user.User;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDataStore implements UserDataStore {

    private final MongoCollection<Document> collection;

    public MongoDataStore(PlayerNotesPlugin plugin) {
        var config = plugin.getConfig().getConfigurationSection("mongodb");

        var mongo = new Mongo(
                config.getString("host"),
                config.getInt("port"),
                config.getString("authentication.username"),
                config.getString("authentication.password"),
                config.getString("authentication.database")
        );

        this.collection = mongo.getClient().getDatabase(config.getString("data.database")).getCollection(config.getString("data.collection"));
        collection.createIndex(Indexes.text("username"), new IndexOptions().name("username")).subscribe(new CommandSubscriber());
    }

    @Override
    public CompletableFuture<User> getUser(String username) {
        var future = new CompletableFuture<User>();

        collection.find(Filters.text(username)).subscribe(new OnAnySubscriber() {

            @Override
            public void onAny(Document document) {
                future.complete(document != null ? fromDocument(document) : null);
            }

        });

        return future;
    }

    @Override
    public CompletableFuture<User> getUser(UUID uuid) {
        var future = new CompletableFuture<User>();

        collection.find(Filters.eq("_id", uuid.toString())).subscribe(new OnAnySubscriber() {

            @Override
            public void onAny(Document document) {
                future.complete(document != null ? fromDocument(document) : null);
            }

        });

        return future;
    }

    @Override
    public void save(User user) {
        var document = new Document("_id", user.getUniqueId().toString())
                .append("username", user.getUsername())
                .append("notes", user.getNotes().stream()
                        .map(Note::asDocument)
                        .toList());

        collection.replaceOne(Filters.eq("_id", user.getUniqueId().toString()), document, Mongo.REPLACE_OPTIONS).subscribe(new CommandSubscriber());
    }

    private User fromDocument(Document document) {
        var user = new User(UUID.fromString(document.getString("_id")), document.getString("username"));

        document.getList("notes", Document.class).stream()
                .map(Note::new)
                .forEach(note -> user.getNotes().add(note));

        return user;
    }

}