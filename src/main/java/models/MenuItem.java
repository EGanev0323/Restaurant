package models;

import org.bson.Document;

public class MenuItem {
    private String name;
    private double price;
    private int quantity; // quantity се използва само в Order

    public MenuItem(String name, double price) {
        this.name = name;
        this.price = price;
        this.quantity = 1;
    }

    public MenuItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public Document toDocument() {
        Document doc = new Document("name", name)
                .append("price", price);
        if (quantity > 0) doc.append("quantity", quantity);
        return doc;
    }

    public static MenuItem fromDocument(Document doc) {
        String name = doc.getString("name");
        double price = doc.getDouble("price");
        int quantity = doc.containsKey("quantity") ? doc.getInteger("quantity") : 1;
        return new MenuItem(name, price, quantity);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
