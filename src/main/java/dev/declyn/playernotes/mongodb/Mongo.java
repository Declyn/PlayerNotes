package dev.declyn.playernotes.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import java.util.Collections;

public class Mongo {

    public static final ReplaceOptions REPLACE_OPTIONS = new ReplaceOptions().upsert(true);

    private MongoClient client;

    public Mongo(String host, int port, String username, String password, String database) {
        var settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))));

        if (!password.isEmpty()) {
            settings.credential(MongoCredential.createCredential(username, database, password.toCharArray()));
        }

        this.client = MongoClients.create(settings.build());
    }

    public MongoClient getClient() {
        if (client == null) {
            throw new IllegalStateException("Mongo must be initialized before calling the client");
        }

        return client;
    }


}