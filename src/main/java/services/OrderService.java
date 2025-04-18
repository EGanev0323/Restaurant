package services;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import models.MenuItem;
import models.Order;
import models.Restaurant;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class OrderService {
    private final MongoDatabase db;
    private final CounterService counterService;
    private final RestaurantService restaurantService;

    public OrderService(MongoDatabase db, CounterService counterService, RestaurantService restaurantService) {
        this.db = db;
        this.counterService = counterService;
        this.restaurantService = restaurantService;
    }

    public void placeOrder(Scanner scanner) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants!");
            return;
        }
        restaurantService.listRestaurants();
        System.out.print("Enter restaurant ID: ");
        int restId = Integer.parseInt(scanner.nextLine());

        Restaurant restaurant = getRestaurantById(restId);

        List<MenuItem> menu = restaurant.getMenu();
        if (menu.isEmpty()) {
            System.out.println("No menu items!");
            return;
        }
        System.out.println("Menu:");
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - " + item.getPrice());
        }
        List<MenuItem> orderItems = new ArrayList<>();
        while (true) {
            System.out.print("Select item number (0 to finish): ");
            int itemNum = Integer.parseInt(scanner.nextLine());
            if (itemNum == 0) break;
            if (itemNum < 1 || itemNum > menu.size()) {
                System.out.println("Invalid item!");
                continue;
            }
            MenuItem menuItem = menu.get(itemNum - 1);
            System.out.print("Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine());
            orderItems.add(new MenuItem(menuItem.getName(), menuItem.getPrice(), qty));
        }
        if (orderItems.isEmpty()) {
            System.out.println("No items selected!");
            return;
        }
        int nextOrderId = counterService.getNextSequence("orderid");
        Order order = new Order(nextOrderId, restId, orderItems, "pending");
        db.getCollection("orders").insertOne(order.toDocument());
        System.out.println("Order placed with id: " + nextOrderId);
    }

    public void updateOrderStatus(Scanner scanner) {
        try {
            System.out.print("Enter order ID: ");
            int orderId;
            try {
                orderId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid order ID!");
                return;
            }
            Document doc = db.getCollection("orders")
                    .find(new Document("_id", orderId)).first();
            if (doc == null) {
                System.out.println("Order not found!");
                return;
            }
            Order order = Order.fromDocument(doc);
            System.out.println("Current status: " + order.getStatus());
            System.out.print("Enter new status (pending/completed/cancelled): ");
            String newStatus = scanner.nextLine().trim().toLowerCase();
            if (!newStatus.equals("pending") && !newStatus.equals("completed") && !newStatus.equals("cancelled")) {
                System.out.println("Invalid status!");
                return;
            }
            db.getCollection("orders").updateOne(
                    new Document("_id", orderId),
                    new Document("$set", new Document("status", newStatus))
            );
            System.out.println("Order status updated to: " + newStatus);
        } catch (Exception e) {
            System.out.println("An error occurred while updating order status: " + e.getMessage());
        }
    }


    public Restaurant getRestaurantById(int id) {
        try {
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();
            Restaurant restaurant = restaurants.stream()
                    .filter(r -> r.getId() == id)
                    .findFirst().orElse(null);
            if (restaurant == null) {
                System.out.println("Restaurant not found!");
                return null;
            }
            return restaurant;
        } catch (Exception e) {
            System.out.println("Error occurred while fetching the restaurant!" + e.getMessage());
            return null;
        }
    }

    public void trackOrder(Scanner scanner) {
        try {
            System.out.print("Enter order ID: ");
            int orderId = Integer.parseInt(scanner.nextLine());
            Document doc = db.getCollection("orders")
                    .find(new Document("_id", orderId)).first();
            if (doc == null) {
                System.out.println("Order not found!");
                return;
            }
            Order order = Order.fromDocument(doc);
            System.out.println("Order status: " + order.getStatus());
        } catch (NumberFormatException e) {
            System.out.println("Error occurred while tracking order." + e.getMessage());
        }
    }

    public void listOrders() {
        try {
            MongoCollection<Document> orders = db.getCollection("orders");
            System.out.println("\n--- Orders ---");
            for (Document doc : orders.find()) {
                Order order = Order.fromDocument(doc);
                double totalPrice=order.getItems().stream().mapToDouble(item->item.getPrice()*item.getQuantity()).sum();
                System.out.println(order.getId() + " | Restaurant: " +
                        getRestaurantById(order.getRestaurantId()).getName() + " | Status: " + order.getStatus() +
                        " | Total price: " + String.format("%.2f",totalPrice) + "лв");
            }
        } catch (Exception e) {
            System.out.println("Error while listing orders." + e.getMessage());
        }
    }

    public void mostOrderedItems() {
        try {
            MongoCollection<Document> orders = db.getCollection("orders");
            List<Document> pipeline = Arrays.asList(
                    new Document("$unwind", "$items"),
                    new Document("$group", new Document("_id", "$items.name")
                            .append("totalOrdered", new Document("$sum", "$items.quantity"))),
                    new Document("$sort", new Document("totalOrdered", -1)),
                    new Document("$limit", 5)
            );
            AggregateIterable<Document> result = orders.aggregate(pipeline);
            System.out.println("\n--- Most Ordered Items ---");
            for (Document doc : result) {
                System.out.println(doc.getString("_id") + ": " + doc.getInteger("totalOrdered"));
            }
        } catch (Exception e) {
            System.out.println("Error while aggregate function." + e.getMessage());
        }
    }

    public void mostOrderedItemsByRestaurant() {
        MongoCollection<Document> orders = db.getCollection("orders");
        List<Document> pipeline = Arrays.asList(
                new Document("$unwind", "$items"),
                new Document("$group", new Document("_id", new Document("restaurantId", "$restaurantId").append("item", "$items.name"))
                        .append("totalOrdered", new Document("$sum", "$items.quantity"))),
                new Document("$sort", new Document("_id.restaurantId", 1).append("totalOrdered", -1))
        );
        AggregateIterable<Document> result = orders.aggregate(pipeline);
        System.out.println("\n--- Most Ordered Items By Restaurant ---");
        for (Document doc : result) {
            Document idDoc = (Document) doc.get("_id");
            int restaurantId = idDoc.getInteger("restaurantId");
            String item = idDoc.getString("item");
            int total = doc.getInteger("totalOrdered");
            System.out.println("Restaurant " + getRestaurantById(restaurantId).getName() + " | " + item + ": " + total);
        }
    }
}
