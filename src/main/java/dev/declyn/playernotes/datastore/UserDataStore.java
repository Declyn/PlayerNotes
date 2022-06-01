package dev.declyn.playernotes.datastore;

import dev.declyn.playernotes.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserDataStore {

    CompletableFuture<User> getUser(String username);

    CompletableFuture<User> getUser(UUID uuid);

    void save(User user);

}