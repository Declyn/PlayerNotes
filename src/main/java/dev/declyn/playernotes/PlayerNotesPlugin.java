package dev.declyn.playernotes;

import dev.declyn.playernotes.commands.NoteCommands;
import dev.declyn.playernotes.datastore.UserDataStore;
import dev.declyn.playernotes.datastore.impl.MongoDataStore;
import dev.declyn.playernotes.datastore.impl.SQLDataStore;
import dev.declyn.playernotes.user.UserListener;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.Objects;

@Plugin(name = "PlayerNotes", version = "1.1", authors = "Declyn")
public class PlayerNotesPlugin extends ExtendedJavaPlugin {

    private UserDataStore userDataStore;

    @Override
    protected void enable() {
        saveDefaultConfig();

        userDataStore = switch (Objects.requireNonNull(getConfig().getString("datastore")).toLowerCase()) {
            case "sqlite" -> new SQLDataStore(this, true);
            case "mysql" -> new SQLDataStore(this, false);
            case "mongodb" -> new MongoDataStore(this);
            default -> throw new IllegalStateException("Data store not recognized, implemented data stores: 'flatfile', 'sql' or 'mongo'");
        };

        new UserListener(this);

        var commandHandler = BukkitCommandHandler.create(this);

        commandHandler.registerDependency(PlayerNotesPlugin.class, this);
        commandHandler.registerDependency(UserDataStore.class, userDataStore);

        commandHandler.register(new NoteCommands());
    }

    public UserDataStore getUserDataStore() {
        return userDataStore;
    }

}