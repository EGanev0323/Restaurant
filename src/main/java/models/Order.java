package models;

import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

public class Order {
    private int id;
    private int restaurantId;
    private List<MenuItem> items;
    private String status;

    public Order(int id, int restaurantId, List<MenuItem> items, String status) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.items = items;
        this.status = status;
    }

    public int getId() { return id; }
    public int getRestaurantId() { return restaurantId; }
    public List<MenuItem> getItems() { return items; }
    public String getStatus() { return status; }

    public Document toDocument() {
        List<Document> itemDocs = items.stream()
                .map(MenuItem::toDocument)
                .collect(Collectors.toList());
        Document doc = new Document("_id", id)
                .append("restaurantId", restaurantId)
                .append("items", itemDocs)
                .append("status", status);
        return doc;
    }

    public static Order fromDocument(Document doc) {
        int id = doc.getInteger("_id");
        int restaurantId = doc.getInteger("restaurantId");
        List<MenuItem> items = ((List<Document>) doc.get("items"))
                .stream().map(MenuItem::fromDocument).collect(Collectors.toList());
        String status = doc.getString("status");
        return new Order(id, restaurantId, items, status);
    }
}
