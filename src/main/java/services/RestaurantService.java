package services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import models.MenuItem;
import models.Restaurant;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RestaurantService {
    private final MongoDatabase db;
    private final CounterService counterService;

    public RestaurantService(MongoDatabase db, CounterService counterService) {
        this.db = db;
        this.counterService = counterService;
    }

    public void addRestaurant(Scanner scanner) {
        try {
            System.out.print("Restaurant name: ");
            String name = scanner.nextLine();
            int nextId = counterService.getNextSequence("restaurantid");
            Restaurant restaurant = new Restaurant(nextId, name, new ArrayList<>());
            db.getCollection("restaurants").insertOne(restaurant.toDocument());
            System.out.println("Restaurant added with id: " + nextId);
        } catch (Exception e) {
            System.out.println("Error while adding restaurant." + e.getMessage());
        }
    }

    public List<Restaurant> getAllRestaurants() {
        MongoCollection<Document> restaurants = db.getCollection("restaurants");
        List<Restaurant> result = new ArrayList<>();
        for (Document doc : restaurants.find()) {
            result.add(Restaurant.fromDocument(doc));
        }
        return result;
    }


//не е тестван дали и как точно работи
    public void deleteRestaurantById(Scanner scanner) {
        try {
            System.out.print("Enter restaurant ID to delete: ");
            int restId;
            try {
                restId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid restaurant ID!");
                return;
            }

            long relatedOrders = db.getCollection("orders").countDocuments(new Document("restaurantId", restId));
            if (relatedOrders > 0) {
                System.out.println("There are " + relatedOrders + " orders related with this restaurant, do you want to delete? (yes/no)");
                String answer = scanner.nextLine().toLowerCase();
                if (!answer.equals("yes")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }
            }

            long deleted = db.getCollection("restaurants").deleteOne(new Document("_id", restId)).getDeletedCount();
            if (deleted > 0) {
                System.out.println("Restaurant deleted successfully.");
                long ordersDeleted = db.getCollection("orders").deleteMany(new Document("restaurantId", restId)).getDeletedCount();
                if (ordersDeleted > 0) {
                    System.out.println("Also deleted " + ordersDeleted + " related orders.");
                }
            } else {
                System.out.println("Restaurant not found.");
            }
        } catch (Exception e) {
            System.out.println("Error deleting restaurant: " + e.getMessage());
        }
    }


    public Restaurant getRestaurantById(int id) {
        Document doc=db.getCollection("restaurants").find(new Document("id", id)).first();
        if (doc==null) {
            System.out.println("Restaurant with id: " + id + " not found");
            return null;
        }
        return Restaurant.fromDocument(doc);
    }

    public void listRestaurants() {
        List<Restaurant> restaurants = getAllRestaurants();
        System.out.println("\n--- Restaurants ---");
        for (Restaurant r : restaurants) {
            System.out.println(r.getId() + ": " + r.getName());
        }
    }

    public void addMenuItem(Scanner scanner) {
        List<Restaurant> restaurants = getAllRestaurants();
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants!");
            return;
        }
        listRestaurants();
        System.out.print("Enter restaurant ID: ");
        int restId = Integer.parseInt(scanner.nextLine());
        Restaurant restaurant = restaurants.stream()
                .filter(r -> r.getId() == restId)
                .findFirst().orElse(null);
        if (restaurant == null) {
            System.out.println("Restaurant not found!");
            return;
        }
        System.out.print("Menu item name: ");
        String itemName = scanner.nextLine();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine());

        MenuItem menuItem = new MenuItem(itemName, price);
        restaurant.getMenu().add(menuItem);

        db.getCollection("restaurants").updateOne(
                new Document("_id", restId),
                new Document("$set", new Document("menu",
                        restaurant.getMenu().stream().map(MenuItem::toDocument).collect(Collectors.toList())))
        );
        System.out.println("Menu item added!");

    }
}
