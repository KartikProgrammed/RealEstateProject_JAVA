import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class PropertySearcher {
    private static final PropertyRTree propertyRTree = new PropertyRTree();
    private static final MongoCollection<Document> propertyCollection = MongoDBUtilProperty.getPropertyCollection();

    // Load data from CSV into the R-tree
    public static void loadPropertyData() {
        propertyRTree.loadDataFromCSV("C:\\Users\\Kartik Khatri\\Desktop\\RealEstateSearch\\src\\main\\java\\Properties.csv");
    }

    // Function to search properties based on address
    public static void searchProperty(Scanner scanner) {
        System.out.print("Enter the address you want to search for: ");
        String address = scanner.nextLine().trim();

        // Debug: print the address entered by the user
        System.out.println("[DEBUG] User entered address: " + address);

        // Convert address to latitude and longitude
        double[] coordinates = API_init.getCoordinatesFromAddress(address);

        // Debug: print the coordinates obtained from the address
        if (coordinates != null) {
            System.out.println("[DEBUG] Coordinates found: Latitude = " + coordinates[0] + ", Longitude = " + coordinates[1]);
        } else {
            System.out.println("[DEBUG] Coordinates not found for address: " + address);
            return; // Exit if coordinates are not found
        }

        double latitude = coordinates[0];
        double longitude = coordinates[1];

        // Query properties from R-tree
        System.out.println("[DEBUG] Querying R-tree for nearby properties...");
        List<String> nearbyPropertyIds = propertyRTree.queryNearbyProperties(latitude, longitude, 0.1); // Increased distance

        // Debug: print the number of properties found
        System.out.println("[DEBUG] Number of nearby properties found: " + nearbyPropertyIds.size());

        // Fetch property details from MongoDB and display them
        if (!nearbyPropertyIds.isEmpty()) {
            System.out.println("Found properties:");
            for (String propertyId : nearbyPropertyIds) {
                System.out.println("[DEBUG] Fetching property details for Property ID: " + propertyId);
                Property property = Property.getPropertyById(propertyId, propertyCollection);
                if (property != null) {
                    System.out.println(propertyDetailsToString(property));
                } else {
                    System.out.println("[DEBUG] Property not found for Property ID: " + propertyId);
                }
            }
        } else {
            System.out.println("No properties found near the given location.");
        }
    }

    // Helper function to display property details as a string
    private static String propertyDetailsToString(Property property) {
        return "Property ID: " + property.getPropertyId() +
                "\nSquare Feet: " + property.squareFeet +
                "\nAddress: " + property.address +
                "\nBHK: " + property.bhkNumber +
                "\nLatitude: " + property.latitude +
                "\nLongitude: " + property.longitude +
                "\nPrice in Lakhs: " + property.priceInLakhs +
                "\nAgent ID: " + property.agentId;
    }

    public static void main(String[] args) {
        loadPropertyData(); // Load property data before searching
        Scanner scanner = new Scanner(System.in);
        searchProperty(scanner);
        scanner.close();
    }
}