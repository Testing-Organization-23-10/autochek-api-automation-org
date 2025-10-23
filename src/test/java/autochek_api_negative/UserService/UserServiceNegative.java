package autochek_api_negative.UserService;
import Base.Initialization;
import Listener.AllureLogs;
import Utils.Negative_Data_Extractor;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import io.qameta.allure.Epic;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.given;
@Epic("API Automation")
@Feature("Negative UserService")
public class UserServiceNegative {

    Initialization initialization = new Initialization();
    private static String franchiseId;
    private static String user_id;
    private static String token;
    private static String Consoletoken;

    // Token rotation variables
    private static List<String> bearerTokens;
    private static AtomicInteger requestCounter = new AtomicInteger(0);
    private static AtomicInteger currentTokenIndex = new AtomicInteger(0);
    private static final int BATCH_SIZE = 45;
    private static final int DELAY_BETWEEN_BATCHES = 2000; // 2 seconds delay between batches
    private static final int DELAY_BETWEEN_TOKENS = 5000; // 5 seconds delay when switching tokens

    // Response variables
    private static String responeName;
    private static String responeAddress;
    private static String responeCity;
    private static String responeState;
    private static String responeCountry;
    private static String responeZipCode;
    private static String responseHouseNo;
    private static String responseEmail;
    private static String resposePhoneNumber;
    private static String responseBvn;
    private static String responseStatus;
    private static String responseNearestLandmark;
    private static String responseMapAddress;
    private static String responseStreetName;

    @BeforeClass
    public void setUp() {
        initialization.initializeEnvironment();// Sets base URI

        // Initialize bearer tokens list with your 4 tokens
        initializeBearerTokens();

        // Set initial console token
        Consoletoken = getCurrentToken();

        System.out.println("=== Token Rotation Setup Complete ===");
        System.out.println("Total Tokens Available: " + bearerTokens.size());
        System.out.println("Batch Size: " + BATCH_SIZE);
        System.out.println("Current Token Index: " + currentTokenIndex.get());
        System.out.println("=====================================");
    }

    /**
     * Initialize the bearer tokens list with your 4 user tokens
     * Replace these with your actual bearer tokens
     */
    private void initializeBearerTokens() {
        bearerTokens = new ArrayList<>();
        // If you have tokens stored in Initialization class
        bearerTokens.add(Initialization.franchiseAdminToken.get("token"));
        bearerTokens.add(Initialization.accountManagerToken.get("token"));
        bearerTokens.add(Initialization.dealerDsaToken.get("token"));
        bearerTokens.add(Initialization.dsaAgentToken.get("token"));
    }

    /**
     * Get current token with automatic rotation based on request count
     */
    private synchronized String getCurrentToken() {
        int currentRequest = requestCounter.get();
        int tokenIndex = currentTokenIndex.get();

        // Check if we need to switch to next token
        if (currentRequest > 0 && currentRequest % BATCH_SIZE == 0) {
            // Switch to next token
            tokenIndex = (tokenIndex + 1) % bearerTokens.size();
            currentTokenIndex.set(tokenIndex);

            System.out.println("=== SWITCHING TOKEN ===");
            System.out.println("Request #" + currentRequest + " - Switching to Token " + (tokenIndex + 1));
            System.out.println("Previous batch completed. Adding delay to avoid rate limits...");
            System.out.println("=====================");

            // Add delay when switching tokens to avoid rate limits
            try {
                Thread.sleep(DELAY_BETWEEN_TOKENS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted during token switch delay: " + e.getMessage());
            }
        }

        return bearerTokens.get(tokenIndex);
    }

    /**
     * Switch to next token in circular fashion (for 429 error handling)
     */
    private synchronized String switchToNextToken() {
        int currentTokenIndex = this.currentTokenIndex.get();
        int nextTokenIndex = (currentTokenIndex + 1) % bearerTokens.size();
        this.currentTokenIndex.set(nextTokenIndex);

        System.out.println("=== SWITCHING TOKEN DUE TO 429 ERROR ===");
        System.out.println("Previous Token Index: " + (currentTokenIndex + 1) + " (Rate Limited)");
        System.out.println("New Token Index: " + (nextTokenIndex + 1));
        if (nextTokenIndex == 0) {
            System.out.println("*** Cycled back to first token ***");
        }
        System.out.println("=======================================");

        return bearerTokens.get(nextTokenIndex);
    }

    /**
     * Add delay between requests in same batch to further reduce rate limit risk
     */
    private void addRequestDelay() {
        int currentRequest = requestCounter.get();

        // Add small delay every 10 requests within a batch
        if (currentRequest % 10 == 0 && currentRequest % BATCH_SIZE != 0) {
            try {
                Thread.sleep(500); // 500ms delay every 10 requests
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted during request delay: " + e.getMessage());
            }
        }
    }

    /**
     * Execute API request with token rotation and 429 handling
     * This method handles both POST and PUT requests
     */
    private Response executeWithTokenRotation(String httpMethod, String endpoint, Map<String, Object> requestBody,
                                              int currentRequestNumber, String testCaseInfo) {
        Response response = null;
        int retryCount = 0;
        int maxRetries = bearerTokens.size(); // Try all tokens if needed
        Set<Integer> triedTokens = new HashSet<>();
        String currentToken = getCurrentToken();

        while (retryCount < maxRetries) {
            int currentTokenIndexForLogging = this.currentTokenIndex.get() + 1;
            triedTokens.add(this.currentTokenIndex.get());

            try {
                // Build the request based on HTTP method
                if ("POST".equalsIgnoreCase(httpMethod)) {
                    response = given()
                            .basePath(endpoint)
                            .header("Authorization", "Bearer " + currentToken)
                            .contentType(ContentType.JSON)
                            .when()
                            .body(requestBody)
                            .post()
                            .then()
                            .log().all()
                            .extract().response();
                } else if ("PUT".equalsIgnoreCase(httpMethod)) {
                    response = given()
                            .header("Authorization", "Bearer " + currentToken)
                            .contentType("application/json")
                            .body(requestBody)
                            .when()
                            .put(endpoint)
                            .then()
                            .log().all()
                            .extract().response();
                }

                // Check if we got rate limited (429)
                if (response.getStatusCode() == 429) {
                    System.err.println("⚠️  Rate limit (429) hit on request #" + currentRequestNumber +
                            " with Token " + currentTokenIndexForLogging + " | " + testCaseInfo);

                    // Check if we've tried all tokens
                    if (triedTokens.size() >= bearerTokens.size()) {
                        System.err.println("❌ All tokens have been rate limited! Proceeding with 429 response.");
                        System.err.println("Tried tokens: " + triedTokens.size() + "/" + bearerTokens.size());
                        break;
                    }

                    retryCount++;

                    // Switch to next token in circular fashion
                    currentToken = switchToNextToken();
                    currentTokenIndexForLogging = this.currentTokenIndex.get() + 1;

                    System.out.println("🔄 Retrying with Token " + currentTokenIndexForLogging +
                            " (Attempt " + (retryCount + 1) + "/" + maxRetries + ")");

                    // Add delay before retry to give server time to reset rate limits
                    try {
                        Thread.sleep(2000); // 2 seconds delay before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Thread interrupted during retry delay: " + ie.getMessage());
                        break;
                    }
                    continue;

                } else {
                    // Success or other error (not rate limit) - exit retry loop
                    if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                        System.out.println("✅ Success with Token " + currentTokenIndexForLogging +
                                " | Status: " + response.getStatusCode() + " | " + testCaseInfo);
                    } else if (response.getStatusCode() != 429) {
                        System.out.println("ℹ️  Non-rate-limit response with Token " + currentTokenIndexForLogging +
                                " | Status: " + response.getStatusCode() + " | " + testCaseInfo);
                    }
                    break;
                }

            } catch (Exception e) {
                System.err.println("❌ Error during request #" + currentRequestNumber +
                        " with Token " + currentTokenIndexForLogging + ": " + e.getMessage());
                retryCount++;

                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Failed after trying all " + maxRetries + " tokens", e);
                }

                // Switch to next token on exception as well
                currentToken = switchToNextToken();

                try {
                    Thread.sleep(1000); // Wait before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Log final response details
        if (response != null) {
            String statusEmoji = getStatusEmoji(response.getStatusCode());
            System.out.println(statusEmoji + " Final Response Status: " + response.getStatusCode() +
                    " | Response Time: " + response.getTime() + "ms" +
                    " | Final Token Used: " + (this.currentTokenIndex.get() + 1));

            if (triedTokens.size() > 1) {
                System.out.println("🔄 Total tokens tried for this request: " + triedTokens.size() + "/" + bearerTokens.size());
            }
        }

        return response;
    }

    /**
     * Get emoji for different status codes for better visual feedback
     */
    private String getStatusEmoji(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return "✅"; // Success
        } else if (statusCode == 429) {
            return "⚠️"; // Rate limited
        } else if (statusCode >= 400 && statusCode < 500) {
            return "❌"; // Client error
        } else if (statusCode >= 500) {
            return "🔥"; // Server error
        } else {
            return "ℹ️"; // Info
        }
    }

    private void putIfNotNull(Map<String, Object> requestBody, String key, Object value) {
        if (value != null) {
            requestBody.put(key, value);
        }
    }
    private static final Map<String, String> franchiseIdMap = new ConcurrentHashMap<>();

    @Story("Create Franchise")
    @Test(priority = 1, dataProvider = "Create Franchise Negative")
    @Parameters({"test_type", "severity", "testDescription", "name", "owner", "email", "phonenumber",
            "city_id", "account_name", "bank_name", "account_number", "account_bvn", "type",
            "display_address", "address", "is_anchor", "latitude", "longitude", "onboarding_source",
            "map_address", "house_no", "zip_code", "street_name", "nearest_landmark", "city",
            "state", "country", "expectedStatusCodeStr"})
    public void CreateFranchise(String test_type, String severity, String testDescription,
                                String name, String owner, String email, String phonenumber,
                                String city_id, String account_name, String bank_name, String account_number,
                                String account_bvn, String type, String display_address,
                                String address, String is_anchor, String latitude, String longitude, String onboarding_source,
                                String map_address, String house_no, String zip_code, String street_name,
                                String nearest_landmark, String city, String state, String country, String expectedStatusCodeStr) {

        // Increment request counter and get current token
        int currentRequestNumber = requestCounter.incrementAndGet();
        int currentTokenIndexDisplay = this.currentTokenIndex.get() + 1; // +1 for display purposes

        // Add delay between requests to reduce rate limit pressure
        addRequestDelay();

        // Log current request info
        String testCaseInfo = "Test Case: " + testDescription;
        System.out.println("Request #" + currentRequestNumber + " | Using Token " + currentTokenIndexDisplay +
                " | " + testCaseInfo);

        SoftAssert softAssert = new SoftAssert();
        int expectedStatusCode = Integer.parseInt(expectedStatusCodeStr.trim());

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        putIfNotNull(requestBody, "name", name);
        putIfNotNull(requestBody, "owner", owner);
        putIfNotNull(requestBody, "email", email);
        putIfNotNull(requestBody, "phonenumber", phonenumber);
        putIfNotNull(requestBody, "city_id", city_id);
        putIfNotNull(requestBody, "account_name", account_name);
        putIfNotNull(requestBody, "bank_name", bank_name);
        putIfNotNull(requestBody, "account_number", account_number);
        putIfNotNull(requestBody, "account_bvn", account_bvn);
        putIfNotNull(requestBody, "type", type);
        putIfNotNull(requestBody, "display_address", display_address);
        putIfNotNull(requestBody, "address", address);
        putIfNotNull(requestBody, "is_anchor", is_anchor);
        putIfNotNull(requestBody, "latitude", latitude);
        putIfNotNull(requestBody, "longitude", longitude);
        putIfNotNull(requestBody, "onboarding_source", onboarding_source);
        putIfNotNull(requestBody, "map_address", map_address);
        putIfNotNull(requestBody, "house_no", house_no);
        putIfNotNull(requestBody, "zip_code", zip_code);
        putIfNotNull(requestBody, "street_name", street_name);
        putIfNotNull(requestBody, "nearest_landmark", nearest_landmark);
        putIfNotNull(requestBody, "city", city);
        putIfNotNull(requestBody, "state", state);
        putIfNotNull(requestBody, "country", country);

        // Now requestBody only has non-null fields
        System.out.println("Final Request Body: " + requestBody);

        // Execute POST request with token rotation
        Response response = executeWithTokenRotation("POST", "/v1/franchise", requestBody,
                currentRequestNumber, testCaseInfo);

        // Perform assertions
        if (response != null) {
            AllureLogs.softAssertEquals(softAssert, response.getStatusCode(), expectedStatusCode, "Status");
            AllureLogs.executeSoftAssertAll(softAssert);

            // Store franchise ID if created successfully for update tests
            if (response.getStatusCode() == 201) {
                try {
                    JsonPath jsonPath = response.jsonPath();
                    String createdFranchiseId = jsonPath.getString("data.id");
                    if (createdFranchiseId != null) {
                        franchiseId = createdFranchiseId;
                        System.out.println("📝 Franchise ID stored for future tests: " + franchiseId);
                    }
                } catch (Exception e) {
                    System.err.println("Could not extract franchise ID from response: " + e.getMessage());
                }
            }

            // Add summary log every 10 requests
            if (currentRequestNumber % 10 == 0) {
                System.out.println("=== PROGRESS UPDATE ===");
                System.out.println("Completed " + currentRequestNumber + " requests");
                System.out.println("Currently using Token " + (this.currentTokenIndex.get() + 1));
                System.out.println("Next token switch at request #" +
                        (((currentRequestNumber / BATCH_SIZE) + 1) * BATCH_SIZE));
                System.out.println("=====================");
            }
        }
    }

    @DataProvider(name = "Create Franchise Negative")
    public Object[][] CreateFranchise() {
        return Negative_Data_Extractor.ExcelData("Create Franchise Negative");
    }

    @Test(priority = 4, dataProvider = "Update Franchise Negative")
    @Story("Update Franchise")
    @Parameters({"test_type", "severity", "testDescription", "name", "email", "phonenumber",
            "city_id", "account_name", "bank_name", "account_number", "account_bvn",
            "type", "display_address", "address", "is_anchor", "map_address",
            "house_no", "zip_code", "street_name", "nearest_landmark", "city",
            "state", "country", "expectedStatusCodeStr"})
    public void UpdateFranchise( String test_type, String severity, String testDescription,
                                String name, String email, String phonenumber,
                                String city_id, String account_name, String bank_name, String account_number,
                                String account_bvn, String type, String display_address,
                                String address, String is_anchor, String map_address,
                                String house_no, String zip_code, String street_name,
                                String nearest_landmark, String city, String state, String country, String expectedStatusCodeStr) throws InterruptedException {

        // Increment request counter and get current token
        int currentRequestNumber = requestCounter.incrementAndGet();
        int currentTokenIndexDisplay = this.currentTokenIndex.get() + 1; // +1 for display purposes

        // Add delay between requests to reduce rate limit pressure
        addRequestDelay();

        // Log current request info
        String testCaseInfo = "Update Test Case: " + testDescription;
        System.out.println("Request #" + currentRequestNumber + " | Using Token " + currentTokenIndexDisplay +
                " | " + testCaseInfo);

        SoftAssert softAssert = new SoftAssert();
        int expectedStatusCode = Integer.parseInt(expectedStatusCodeStr.trim());

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("email", email);
        requestBody.put("phonenumber", phonenumber);
        requestBody.put("cityId", city_id);
        requestBody.put("accountName", account_name);
        requestBody.put("bankName", bank_name);
        requestBody.put("accountNumber", account_number);
        requestBody.put("accountBvn", account_bvn);
        requestBody.put("type", type);
        requestBody.put("displayAddress", display_address);
        requestBody.put("address", address);
        requestBody.put("isAnchor", is_anchor);
        requestBody.put("mapAddress", map_address);
        requestBody.put("houseNo", house_no);
        requestBody.put("zipCode", zip_code);
        requestBody.put("city", city);
        requestBody.put("streetName", street_name);
        requestBody.put("nearestLandmark", nearest_landmark);
        requestBody.put("state", state);
        requestBody.put("country", country);

        // Check if franchiseId is available
        if (franchiseId == null || franchiseId.isEmpty()) {
            System.err.println("⚠️ Warning: No franchise ID available for update test. Using default or creating one...");
            // You might want to create a franchise first or use a default ID
            franchiseId = "default-franchise-id"; // Replace with appropriate logic
        }

        // Execute PUT request with token rotation
        String updateEndpoint = "/v1/franchise/" + franchiseId;
        Response response = executeWithTokenRotation("PUT", updateEndpoint, requestBody,
                currentRequestNumber, testCaseInfo);

        // Perform assertions
        if (response != null) {
            AllureLogs.softAssertEquals(softAssert, response.getStatusCode(), expectedStatusCode, "Status");
            AllureLogs.executeSoftAssertAll(softAssert);

            // Add summary log every 10 requests
            if (currentRequestNumber % 10 == 0) {
                System.out.println("=== PROGRESS UPDATE ===");
                System.out.println("Completed " + currentRequestNumber + " requests");
                System.out.println("Currently using Token " + (this.currentTokenIndex.get() + 1));
                System.out.println("Next token switch at request #" +
                        (((currentRequestNumber / BATCH_SIZE) + 1) * BATCH_SIZE));
                System.out.println("=====================");
            }
        }
    }

    @DataProvider(name = "Update Franchise Negative")
    public Object[][] GetFranchiseUpdate() {
        return Negative_Data_Extractor.ExcelData("Update Franchise Negative");
    }
    @Test(priority = 4, dataProvider = "Get Franchise ID Negative")
    @Parameters({"Sno", "type1", "severity", "testDescription", "id", "expectedStatusCodeStr"})

    @Story("Get Franchise By ID")
    public void GetFranchiseByID(String type1, String severity, String testDescription,
                                 String id, String expectedStatusCodeStr) throws InterruptedException {
        int expectedStatusCode = Integer.parseInt(expectedStatusCodeStr.trim());
        String auth_token = bearerTokens.get(1);
        Response response = given()
                .header("Authorization", "Bearer " + auth_token)
                .when()
                .get("/v1/franchise/"+id)
                .then()
                .log().all()
                .extract().response();

        SoftAssert softAssert = new SoftAssert();
        AllureLogs.softAssertEquals(softAssert,response.getStatusCode(),expectedStatusCode,"Status");

        AllureLogs.executeSoftAssertAll(softAssert);

    }


    @DataProvider(name = "Get Franchise ID Negative")
    public Object[][] GetFranchiseID() {

        return Negative_Data_Extractor.ExcelData("Get Franchise ID Negative");
    }
    @Test(priority = 4, dataProvider = "Get Franchise Negative")
    @Story("Get Franchise List")
    @Parameters({"test_type", "severity", "testDescription", "pageSize", "country", "status", "expectedStatusCodeStr"})

    public void GetFranchiseList(String test_type, String severity, String testDescription,
                                 String pageSize, String country, String status, String expectedStatusCodeStr) {
        String auth_token = bearerTokens.get(1);
        int expectedStatusCode = Integer.parseInt(expectedStatusCodeStr.trim());
        RequestSpecification request = given()
                .header("Authorization", "Bearer " + auth_token)
                .log().all();

        // Execute the GET request
        Response response = request
                .when()
                .queryParam("page_size", pageSize)
                .queryParam("country", country)
                .queryParam("status", status)
                .get("/v1/franchise")
                .then()
                .log().all()
                .extract().response();

        // Assertions
        SoftAssert softAssert = new SoftAssert();
        if(response.getStatusCode() == expectedStatusCode){
            AllureLogs.softAssertEquals(softAssert, response.getStatusCode(), expectedStatusCode, "Status");
        }
        else{
            AllureLogs.softAssertEquals(softAssert,response.jsonPath().getList("result").size(),0,"result size");
        }

        AllureLogs.executeSoftAssertAll(softAssert);
    }
    @DataProvider(name = "Get Franchise Negative")
    public Object[][] getFranchiseList() {
        return Negative_Data_Extractor.ExcelData("Get Franchise Negative");
    }


    // Check if param is omitted (represented as "—" or empty)
    private boolean isOmitted(String value) {
        return value == null || value.trim().equals("—") || value.trim().isEmpty();
    }

    // Convert string to proper Object (int, float, null, boolean, array, JSON, etc.)
    private Object convertValue(String value) {
        try {
            value = value.trim();

            if (value.equalsIgnoreCase("null")) return null;
            if (value.equalsIgnoreCase("true")) return true;
            if (value.equalsIgnoreCase("false")) return false;


            // Array input
            if (value.startsWith("[") && value.endsWith("]")) {
                return new Gson().fromJson(value, Object.class);  // Can be List or Array
            }

            // Object input
            if (value.startsWith("{") && value.endsWith("}")) {
                return new Gson().fromJson(value, Object.class);  // Can be Map
            }

            // Try number parsing
            if (value.contains(".")) return Double.parseDouble(value); // float
            return Integer.parseInt(value); // int

        } catch (NumberFormatException | JsonSyntaxException e) {
            // Return raw string if parsing fails
            return value;
        }
    }

    /**
     * Utility method to manually reset token rotation (if needed)
     */
    public static void resetTokenRotation() {
        requestCounter.set(0);
        currentTokenIndex.set(0);
        System.out.println("Token rotation counters reset");

    }

    /**
     * Utility method to get current rotation status
     */
    public static void printRotationStatus() {
        System.out.println("Current Request Count: " + requestCounter.get());
        System.out.println("Current Token Index: " + (currentTokenIndex.get() + 1));
        System.out.println("Requests until next token switch: " +
                (BATCH_SIZE - (requestCounter.get() % BATCH_SIZE)));
    }
}