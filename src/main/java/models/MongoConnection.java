package models;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoConnection {
    public static MongoClient connect() {
        return MongoClients.create("mongodb://localhost:27017");
    }
}
