package services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;


public class CounterService {
    private final MongoDatabase db;

    public CounterService(MongoDatabase db) {
        this.db = db;
    }

    public int getNextSequence(String name) {
        MongoCollection<Document> counters = db.getCollection("counters");
        Document result = counters.findOneAndUpdate(
                eq("_id", name),
                inc("seq", 1),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );
        return result.getInteger("seq");
    }
}
