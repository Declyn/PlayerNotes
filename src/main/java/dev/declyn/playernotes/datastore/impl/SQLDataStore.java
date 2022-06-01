package dev.declyn.playernotes.datastore.impl;

import co.aikar.idb.DB;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.DbRow;
import co.aikar.idb.PooledDatabaseOptions;
import dev.declyn.playernotes.PlayerNotesPlugin;
import dev.declyn.playernotes.datastore.UserDataStore;
import dev.declyn.playernotes.note.Note;
import dev.declyn.playernotes.user.User;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLDataStore implements UserDataStore {

    private static final JsonWriterSettings JSON_WRITER_SETTINGS = JsonWriterSettings.builder()
            .outputMode(JsonMode.EXTENDED)
            .build();

    private final String table;

    public SQLDataStore(PlayerNotesPlugin plugin, boolean sqlite) {
        var config = plugin.getConfig().getConfigurationSection("mysql");

        this.table = config.getString("data.table");

        DatabaseOptions options;

        if (sqlite) {
            options = DatabaseOptions.builder().sqlite("playerNotes.db").logger(plugin.getLogger()).build();
        } else {
            options = DatabaseOptions.builder().mysql(
                            config.getString("username"),
                            config.getString("password"),
                            config.getString("database"),
                            config.getString("host") + ':' + config.getInt("port")
                    )
                    .logger(plugin.getLogger())
                    .build();
        }

        DB.setGlobalDatabase(PooledDatabaseOptions.builder().options(options).createHikariDatabase());

        try {
            DB.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (uuid VARCHAR(64), username VARCHAR(64), notes LONGTEXT, PRIMARY KEY (uuid))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<User> getUser(String username) {
        var future = new CompletableFuture<User>();
        DB.getFirstRowAsync("SELECT * FROM " + table + " WHERE UPPER(username)=UPPER(?)", username).thenAccept(row -> future.complete(row != null ? load(row) : null));
        return future;
    }

    @Override
    public CompletableFuture<User> getUser(UUID uuid) {
        var future = new CompletableFuture<User>();
        DB.getFirstRowAsync("SELECT * FROM " + table + " WHERE uuid=?", uuid.toString()).thenAccept(row -> future.complete(row != null ? load(row) : null));
        return future;
    }

    @Override
    public void save(User user) {
        var notesData = new Document();
        user.getNotes().forEach(note -> notesData.put(String.valueOf(note.getId()), note.asDocument()));

        DB.executeUpdateAsync("REPLACE INTO " + table + " (uuid, username, notes) VALUES (?, ?, ?)", user.getUniqueId().toString(), user.getUsername(), notesData.toJson(JSON_WRITER_SETTINGS));
    }

    private User load(DbRow row) {
        var user = new User(UUID.fromString(row.getString("uuid")), row.getString("username"));

        var notesData = Document.parse(row.getString("notes"));
        notesData.keySet().forEach(key -> user.getNotes().add(new Note(notesData.get(key, Document.class))));

        return user;
    }

}