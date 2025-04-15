package models;

import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

public class Restaurant {
    private int id;
    private String name;
    private List<MenuItem> menu;

    public Restaurant(int id, String name, List<MenuItem> menu) {
        this.id = id;
        this.name = name;
        this.menu = menu;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<MenuItem> getMenu() { return menu; }

    public Document toDocument() {
        List<Document> menuDocs = menu.stream()
                .map(MenuItem::toDocument)
                .collect(Collectors.toList());
        Document doc = new Document("_id", id)
                .append("name", name)
                .append("menu", menuDocs);
        return doc;
    }

    public static Restaurant fromDocument(Document doc) {
        int id = doc.getInteger("_id");
        String name = doc.getString("name");
        List<MenuItem> menu = ((List<Document>) doc.get("menu"))
                .stream().map(MenuItem::fromDocument).collect(Collectors.toList());
        return new Restaurant(id, name, menu);
    }
}
