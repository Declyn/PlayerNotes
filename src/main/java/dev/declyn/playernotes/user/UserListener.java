package dev.declyn.playernotes.user;

import dev.declyn.playernotes.PlayerNotesPlugin;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class UserListener {

    private final PlayerNotesPlugin plugin;

    public UserListener(PlayerNotesPlugin plugin) {
        this.plugin = plugin;

        Events.subscribe(PlayerLoginEvent.class).handler(event -> {
            var player = event.getPlayer();

            plugin.getUserDataStore().getUser(player.getUniqueId()).thenAccept(user -> {
                if (user != null) {
                    if (!user.getUsername().equals(player.getName())) {
                        user.setUsername(player.getName());
                        plugin.getUserDataStore().save(user);
                    }
                    return;
                }

                user = new User(player.getUniqueId(), player.getName());
                plugin.getUserDataStore().save(user);
            });
        });

        if (plugin.getConfig().getBoolean("announceNotesOnJoin")) {
            Events.subscribe(PlayerJoinEvent.class).handler(event -> plugin.getUserDataStore().getUser(event.getPlayer().getUniqueId()).thenAccept(user -> {
                if (user == null || user.getNotes().isEmpty()) {
                    return;
                }

                Players.forEach(player -> {
                    if (!player.hasPermission("notes.view")) {
                        return;
                    }

                    Schedulers.sync().run(() -> player.chat("/notes view " + user.getUsername()));
                });
            }));
        }
    }

}