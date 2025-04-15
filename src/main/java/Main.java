import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import models.MongoConnection;
import services.CounterService;
import services.OrderService;
import services.RestaurantService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        MongoClient client = MongoConnection.connect();
        MongoDatabase db = client.getDatabase("food_delivery");
        Scanner scanner = new Scanner(System.in);

        CounterService counterService = new CounterService(db);
        RestaurantService restaurantService = new RestaurantService(db, counterService);
        OrderService orderService = new OrderService(db, counterService, restaurantService);

        while (true) {
            System.out.println("\n--- Food Delivery Backend ---");
            System.out.println("1. Add Restaurant");
            System.out.println("2. List Restaurants");
            System.out.println("3. Add Menu Item");
            System.out.println("4. Place Order");
            System.out.println("5. Track Order");
            System.out.println("6. List Orders");
            System.out.println("7. Most Ordered Items (Aggregation)");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1: restaurantService.addRestaurant(scanner); break;
                case 2: restaurantService.listRestaurants(); break;
                case 3: restaurantService.addMenuItem(scanner); break;
                case 4: orderService.placeOrder(scanner); break;
                case 5: orderService.trackOrder(scanner); break;
                case 6: orderService.listOrders(); break;
                case 7: orderService.mostOrderedItems(); break;
                case 0: client.close(); return;
                default: System.out.println("Invalid choice!");
            }
        }
    }
}