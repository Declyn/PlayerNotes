package dev.declyn.playernotes.commands;

import dev.declyn.playernotes.PlayerNotesPlugin;
import dev.declyn.playernotes.datastore.UserDataStore;
import dev.declyn.playernotes.note.Note;
import dev.declyn.playernotes.utilities.Text;
import dev.declyn.playernotes.utilities.TimeUtil;
import me.lucko.helper.Schedulers;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.UUID;

@Command({"notes", "note"})
public class NoteCommands {

    @Dependency
    private PlayerNotesPlugin plugin;

    @Dependency
    private UserDataStore userDataStore;

    @Default
    public void help(BukkitCommandActor actor) {
        var player = actor.requirePlayer();

        if (!player.hasPermission("notes.use")) {
            player.sendMessage(Text.translate("&6This server is running PlayerNotes version " + plugin.getDescription().getVersion() + " by Declyn."));
            return;
        }

        Text.translate(plugin.getConfig().getStringList("command.help")).forEach(player::sendMessage);
    }

    @Subcommand("view")
    @CommandPermission("notes.view")
    @Usage("<player> [id]")
    @AutoComplete("@players")
    public void view(BukkitCommandActor actor, String username, @Optional Integer id) {
        userDataStore.getUser(username).thenAccept(user -> {
            if (user == null) {
                actor.reply(Text.translate(plugin.getConfig().getString("command.userNotFound")).replace("%player%", username));
                return;
            }

            if (user.getNotes().isEmpty()) {
                actor.reply(Text.translate(plugin.getConfig().getString("command.empty")).replace("%player%", username));
                return;
            }

            if (id != null) {
                var note = user.getNote(id);

                if (note == null) {
                    actor.reply(Text.translate(plugin.getConfig().getString("command.noteNotFound")).replace("%id%", String.valueOf(id)));
                    return;
                }

                Schedulers.async().run(() -> {
                    var author = userDataStore.getUser(note.getAuthor()).join();
                    displayNote(actor, note, author.getUsername());
                });
                return;
            }

            Schedulers.async().run(() -> {
                var authorNames = new HashMap<UUID, String>();

                for (Note note : user.getNotes()) {
                    if (authorNames.containsKey(note.getAuthor())) {
                        continue;
                    }

                    authorNames.put(note.getAuthor(), userDataStore.getUser(note.getAuthor()).join().getUsername());
                }

                user.getNotes().forEach(note -> displayNote(actor, note, authorNames.get(note.getAuthor())));
            });
        });
    }

    @Subcommand("add")
    @CommandPermission("notes.add")
    @Usage("<player> <content...>")
    @AutoComplete("@players")
    public void add(BukkitCommandActor actor, String username, String content) {
        var player = actor.requirePlayer();

        userDataStore.getUser(username).thenAccept(user -> {
            if (user == null) {
                actor.reply(Text.translate(plugin.getConfig().getString("command.userNotFound")).replace("%player%", username));
                return;
            }

            var note = new Note(user.getNotes().size() + 1, System.currentTimeMillis(), player.getUniqueId(), content);
            user.getNotes().add(note);
            userDataStore.save(user);

            actor.reply(Text.translate(plugin.getConfig().getString("command.add")).replace("%id%", String.valueOf(note.getId())).replace("%player%", user.getUsername()));
        });
    }

    @Subcommand("remove")
    @CommandPermission("notes.remove")
    @Usage("<player> <id>")
    @AutoComplete("@players")
    public void remove(BukkitCommandActor actor, String username, int id) {
        userDataStore.getUser(username).thenAccept(user -> {
            if (user == null) {
                actor.reply(Text.translate(plugin.getConfig().getString("command.userNotFound")).replace("%player%", username));
                return;
            }

            if (user.getNotes().isEmpty()) {
                actor.reply(Text.translate(plugin.getConfig().getString("command.empty")).replace("%player%", username));
                return;
            }

            var note = user.getNote(id);
            if (note == null) {
                actor.reply(Text.translate(plugin.getConfig().getString("command.noteNotFound")).replace("%id%", String.valueOf(id)));
                return;
            }

            user.getNotes().remove(note);
            userDataStore.save(user);

            actor.reply(Text.translate(plugin.getConfig().getString("command.remove")).replace("%id%", String.valueOf(note.getId())).replace("%player%", user.getUsername()));
        });
    }

    private void displayNote(BukkitCommandActor actor, Note note, String author) {
        Text.translate(plugin.getConfig().getStringList("command.preview")).forEach(string -> actor.reply(string
                .replace("%id%", String.valueOf(note.getId()))
                .replace("%content%", note.getContent())
                .replace("%timestamp%", TimeUtil.formatDate(note.getTimestamp()))
                .replace("%author%", author)
        ));
    }

}