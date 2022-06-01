package dev.declyn.playernotes;

import dev.declyn.playernotes.commands.NoteCommands;
import dev.declyn.playernotes.datastore.UserDataStore;
import dev.declyn.playernotes.datastore.impl.MongoDataStore;
import dev.declyn.playernotes.datastore.impl.SQLDataStore;
import dev.declyn.playernotes.user.User;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.plugin.ap.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.Objects;

@Plugin(name = "PlayerNotes", version = "1.0", authors = "Declyn")
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

        Events.subscribe(PlayerLoginEvent.class).handler(event -> {
            var player = event.getPlayer();

            userDataStore.getUser(player.getUniqueId()).thenAccept(user -> {
                if (user != null) {
                    if (!user.getUsername().equals(player.getName())) {
                        user.setUsername(player.getName());
                        userDataStore.save(user);
                    }
                    return;
                }

                user = new User(player.getUniqueId(), player.getName());
                userDataStore.save(user);
            });
        });

        var commandHandler = BukkitCommandHandler.create(this);

        commandHandler.registerDependency(PlayerNotesPlugin.class, this);
        commandHandler.registerDependency(UserDataStore.class, userDataStore);

        commandHandler.getAutoCompleter().registerSuggestion("username", (list, commandActor, executableCommand) -> getServer().getOnlinePlayers().stream().map(Player::getName).toList());

        commandHandler.register(new NoteCommands());
    }

    public UserDataStore getUserDataStore() {
        return userDataStore;
    }

}