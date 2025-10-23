package autochek_api_positive.UserService;

import Base.Initialization;
import Listener.AllureLogs;
import Utils.Postive_Data_Extractor;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.*;

import io.restassured.path.json.JsonPath;
import static io.restassured.RestAssured.given;

@Epic("API Automation")
@Feature("Postive UserService")
public class UserService {

    Initialization initialization = new Initialization();

    // Global variables used across tests
    private static String franchiseId;
    private static String user_id;
    private static String token;
    private static String Consoletoken;
    private static String random_email;
    private static String random_phoneNumber;
    private static String random_accountNumber;
    private static String random_accountBvn;
    private static String status = "pending";
    private static String country = "NG";

    // Stores created franchise data for validation across tests
    private static final Map<String, Map<String, String>> franchiseResponseMap = new LinkedHashMap<>();

    @BeforeClass
    public void setUp() {
        initialization.initializeEnvironment(); // Sets base URI and tokens
        user_id = Initialization.franchiseAdminToken.get("userId");
        token = Initialization.franchiseAdminToken.get("token");
        Consoletoken = Initialization.consoleAdminToken.get("token");
    }

    @Test(dataProvider = "Create Franchise")
    @Description("Create a new franchise with valid data")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create Franchise")
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

        SoftAssert softAssert = new SoftAssert();
        int expectedStatusCode = Integer.parseInt(expectedStatusCodeStr.trim());

        // Generate random values to avoid duplicate conflicts
        int randomNum = 10000 + new Random().nextInt(90000);
        random_email = email + randomNum + "@mail.com";
        random_phoneNumber = phonenumber + randomNum;
        random_accountNumber = account_number + randomNum;
        random_accountBvn = account_bvn + randomNum;

        // Prepare request body for POST call
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("owner", owner);
        requestBody.put("email", random_email);
        requestBody.put("phonenumber", random_phoneNumber);
        requestBody.put("accountName", account_name);
        requestBody.put("bankName", bank_name);
        requestBody.put("accountNumber", random_accountNumber);
        requestBody.put("accountBvn", random_accountBvn);
        requestBody.put("type", type);
        requestBody.put("displayAddress", display_address);
        requestBody.put("address", address);
        requestBody.put("isAnchor", is_anchor);
        requestBody.put("latitude", latitude);
        requestBody.put("longitude", longitude);
        requestBody.put("onboarding_source", onboarding_source);
        requestBody.put("mapAddress", map_address);
        requestBody.put("houseNo", house_no);
        requestBody.put("zipCode", zip_code);
        requestBody.put("streetName", street_name);
        requestBody.put("nearestLandmark", nearest_landmark);

        // Send POST request to create franchise
        Response response = given()
                .header("Authorization", "Bearer " + Consoletoken)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/v1/franchise")
                .then()
                .log().all()
                .extract().response();
        System.out.println(city_id);
        System.out.println(state);
        System.out.println(country);
        System.out.println(city);
        // Validate status code
        AllureLogs.softAssertEquals(softAssert, response.getStatusCode(), expectedStatusCode, "Expected Status Code");
        JsonPath json = response.jsonPath();
        franchiseId = json.getString("id");
        // On success, extract and store franchise details for validation
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {


            Map<String, String> responseData = new HashMap<>();
            responseData.put("id", franchiseId);
            responseData.put("name", json.getString("name"));
            responseData.put("email", json.getString("email"));
            responseData.put("phonenumber", json.getString("phonenumber"));
            responseData.put("address", json.getString("address"));
            responseData.put("zipCode", json.getString("zipCode"));
            responseData.put("mapAddress", json.getString("mapAddress"));
            responseData.put("nearestLandmark", json.getString("nearestLandmark"));
            responseData.put("houseNo", json.getString("houseNo"));
            responseData.put("streetName", json.getString("streetName"));
            responseData.put("cityId", json.getString("cityId"));
            responseData.put("state", json.getString("state"));
            responseData.put("country", json.getString("country"));

            // Store each created franchise's details using its ID as key
            franchiseResponseMap.put(franchiseId, responseData);
        }
        AllureLogs.softAssertNotNull(softAssert, json.getString("id"), "id should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("isAnchor"), "isAnchor should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("mapAddress"), "mapAddress should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("houseNo"), "houseNo should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("zipCode"), "zipCode should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("streetName"), "streetName should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("nearestLandmark"), "nearestLandmark should not be null");

        AllureLogs.softAssertNotNull(softAssert, json.getString("name"), "name should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("email"), "email should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("phonenumber"), "phonenumber should not be null");
        // AllureLogs.softAssertNotNull(softAssert, json.getString("cityId"), "cityId should not be null");

        AllureLogs.softAssertNotNull(softAssert, json.getString("accountName"), "accountName should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("bankName"), "bankName should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("accountNumber"), "accountNumber should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("accountBvn"), "accountBvn should not be null");

        AllureLogs.softAssertNotNull(softAssert, json.getString("type"), "type should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("displayAddress"), "displayAddress should not be null");
        AllureLogs.softAssertNotNull(softAssert, json.getString("address"), "address should not be null");


        AllureLogs.executeSoftAssertAll(softAssert);


    }

    @DataProvider(name = "Create Franchise")
    public Object[][] CreateFranchise() {
        // Reads test data from Excel sheet named "Create Franchise"
        return Postive_Data_Extractor.ExcelData("Create Franchise");
    }

    @Test(dependsOnMethods = "CreateFranchise")
    @Description("Get franchise by ID to verify creation")
    @Severity(SeverityLevel.NORMAL)
    @Story("Get Franchise By ID")
    public void GetFranchiseByID() {
        SoftAssert softAssert = new SoftAssert();
        int counter = 1;
        System.out.println("Franchises stored in map: " + franchiseResponseMap.size());


        // Loop through all created franchise records
        for (Map.Entry<String, Map<String, String>> entry : franchiseResponseMap.entrySet()) {
            String franchiseId = entry.getKey();
            Map<String, String> expectedFranchiseDetails = entry.getValue();

            // Send GET request to fetch franchise by ID
            Response getResponse = given()
                    .header("Authorization", "Bearer " + Consoletoken)
                    .when()
                    .get("/v1/franchise/" + franchiseId)
                    .then()
                    .log().all()
                    .extract().response();

            JsonPath json = getResponse.jsonPath();

            // Validate all expected fields
            AllureLogs.step(" [" + counter + "] Validating Franchise ID: `" + franchiseId + "`");
            AllureLogs.softAssertEquals(softAssert, getResponse.getStatusCode(), 200, "Status Code ");
            AllureLogs.softAssertEquals(softAssert, json.getString("id"), expectedFranchiseDetails.get("id"), "Franchise ID");
            AllureLogs.softAssertEquals(softAssert, json.getString("name"), expectedFranchiseDetails.get("name"), "Name");
            AllureLogs.softAssertEquals(softAssert, json.getString("email"), expectedFranchiseDetails.get("email"), "Email");
            AllureLogs.softAssertEquals(softAssert, json.getString("phonenumber"), expectedFranchiseDetails.get("phonenumber"), "Phone ");
            AllureLogs.softAssertEquals(softAssert, json.getString("address"), expectedFranchiseDetails.get("address"), "Address ");
            AllureLogs.softAssertEquals(softAssert, json.getString("zipCode"), expectedFranchiseDetails.get("zipCode"), "Zip Code ");
            AllureLogs.softAssertEquals(softAssert, json.getString("mapAddress"), expectedFranchiseDetails.get("mapAddress"), "Map Address");
            AllureLogs.softAssertEquals(softAssert, json.getString("nearestLandmark"), expectedFranchiseDetails.get("nearestLandmark"), "Landmark");
            AllureLogs.softAssertEquals(softAssert, json.getString("streetName"), expectedFranchiseDetails.get("streetName"), "Street Name");
            AllureLogs.softAssertEquals(softAssert, json.getString("houseNo"), expectedFranchiseDetails.get("houseNo"), "House No");
            counter++;
        }

        AllureLogs.executeSoftAssertAll(softAssert);
    }


    @Test(dataProvider = "Get Franchise", dependsOnMethods = "CreateFranchise")
    @Description("Validates GET /v1/franchise with dynamic parameters - Positive Test")
    @Severity(SeverityLevel.CRITICAL)
    @Parameters({"type", "severity", "testDescription", "page_size", "country", "status", "expectedStatusCode"})
    @Story("Get Franchise List")
    public void GetFranchiseList(String type, String severity, String testDescription,

                                 String page_size,String expectedStatusCode) {

        SoftAssert softAssert = new SoftAssert();

        // Send GET request with query params
        Response response = given()
                .header("Authorization", "Bearer " + Consoletoken)
                .queryParam("page_size", page_size)
                .queryParam("country", country)
                .queryParam("status", status)
                .when()
                .get("/v1/franchise")
                .then()
                .log().all()
                .extract().response();

        // Validate response status code
        AllureLogs.softAssertEquals(softAssert, response.getStatusCode(), Integer.parseInt(expectedStatusCode), "Status code should match");

        // Re-validate each franchise using GET by ID to ensure they're returned correctly
        int counter = 1;
        for (Map.Entry<String, Map<String, String>> entry : franchiseResponseMap.entrySet()) {
            String franchiseId = entry.getKey();
            Map<String, String> expectedFranchiseDetails = entry.getValue();

            Response getResponse = given()
                    .header("Authorization", "Bearer " + Consoletoken)
                    .when()
                    .get("/v1/franchise/" + franchiseId)
                    .then()
                    .log().all()
                    .extract().response();

            JsonPath json1 = getResponse.jsonPath();

            AllureLogs.step("Count [" + counter + "] Validating Franchise ID: `" + franchiseId + "`");
            AllureLogs.softAssertEquals(softAssert, getResponse.getStatusCode(), 200, "Status Code ");
            AllureLogs.softAssertEquals(softAssert, json1.getString("id"), expectedFranchiseDetails.get("id"), "Franchise ID");
            AllureLogs.softAssertEquals(softAssert, json1.getString("name"), expectedFranchiseDetails.get("name"), "Name");
            AllureLogs.softAssertEquals(softAssert, json1.getString("email"), expectedFranchiseDetails.get("email"), "Email");
            AllureLogs.softAssertEquals(softAssert, json1.getString("phonenumber"), expectedFranchiseDetails.get("phonenumber"), "Phone ");
            AllureLogs.softAssertEquals(softAssert, json1.getString("address"), expectedFranchiseDetails.get("address"), "Address ");
            AllureLogs.softAssertEquals(softAssert, json1.getString("zipCode"), expectedFranchiseDetails.get("zipCode"), "Zip Code ");
            AllureLogs.softAssertEquals(softAssert, json1.getString("mapAddress"), expectedFranchiseDetails.get("mapAddress"), "Map Address");
            AllureLogs.softAssertEquals(softAssert, json1.getString("nearestLandmark"), expectedFranchiseDetails.get("nearestLandmark"), "Landmark");
            AllureLogs.softAssertEquals(softAssert, json1.getString("streetName"), expectedFranchiseDetails.get("streetName"), "Street Name");
            AllureLogs.softAssertEquals(softAssert, json1.getString("houseNo"), expectedFranchiseDetails.get("houseNo"), "House No");

            counter++;
            int total = response.path("pagination.total");
            int pageSize = response.path("pagination.pageSize");
            int currentPage = response.path("pagination.currentPage");
            List<String> ids = response.path("result.id");

            // Assert: IDs list is not empty
            AllureLogs.softAssertTrue(softAssert, ids.size() > 0, "Result IDs list should not be empty");




            // Calculate total pages
            int totalPages = (int) Math.ceil((double) total / pageSize);
            if (total <= pageSize) {
                AllureLogs.softAssertTrue(
                        softAssert,
                        ids.size() == total,
                        "When total < pageSize, current page should be 1 and totalPages should be 1"
                );
            }

//// Case 2: If total >= pageSize → validate totalPages calculation
            else {
                AllureLogs.softAssertEquals(
                        softAssert,
                        ids.size(),
                        pageSize,
                        "Total pages calculation should match expected formula"
                );
            }

        }

        AllureLogs.executeSoftAssertAll(softAssert);


    }

    @DataProvider(name = "Get Franchise")
    public Object[][] GetFranchise() {
        // Reads test data from Excel sheet named "Get Franchise"
        return Postive_Data_Extractor.ExcelData("Get Franchise");
    }



    @Test(dataProvider = "Update Franchise")
    @Description("Update franchise fields one-by-one using PUT and validate the response")
    @Severity(SeverityLevel.NORMAL)
    @Parameters({"test_type", "severity", "testDescription", "name", "email", "phonenumber",
            "city_id", "account_name", "bank_name", "account_number", "account_bvn",
            "type", "display_address", "address", "is_anchor", "map_address",
            "house_no", "zip_code", "street_name", "nearest_landmark", "city",
            "state", "country", "expectedStatusCodeStr"})
    @Story("Update Franchise")

    public void UpdateFranchise(String test_type, String severity, String testDescription,
                                String name, String email, String phonenumber,
                                String city_id, String account_name, String bank_name, String account_number,
                                String account_bvn, String type, String display_address,
                                String address, String is_anchor, String map_address,
                                String house_no, String zip_code, String street_name,
                                String nearest_landmark, String city, String state, String country, String expectedStatusCode) throws InterruptedException {

        SoftAssert softAssert = new SoftAssert();

        Map<String, String> existingData = franchiseResponseMap.get("id");
        String uuid = UUID.randomUUID().toString().substring(0, 6);
        int randomNum = 10000 + new Random().nextInt(90000);
        random_email = email + randomNum + "@mail.com";
        random_phoneNumber = phonenumber + randomNum;
        random_accountNumber = account_number + randomNum;
        random_accountBvn = account_bvn + randomNum;
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("email", random_email);
        requestBody.put("phonenumber", random_phoneNumber);
        requestBody.put("cityId", city_id);
        requestBody.put("accountName", account_name);
        requestBody.put("bankName", bank_name);
        requestBody.put("accountNumber", random_accountNumber);
        requestBody.put("accountBvn", random_accountBvn);
        requestBody.put("type", type);
        requestBody.put("displayAddress", display_address);
        requestBody.put("address", address);
        requestBody.put("isAnchor", is_anchor);
        requestBody.put("mapAddress", map_address);
        requestBody.put("houseNo", house_no);
        requestBody.put("zipCode", zip_code);
        requestBody.put("streetName", street_name);
        requestBody.put("nearestLandmark", nearest_landmark);
        requestBody.put("state", state);
        requestBody.put("country", country);

        // Send PUT request
        Response response = given()
                .header("Authorization", "Bearer " + Consoletoken)
                .contentType("application/json")
                .body(requestBody)
                .when()
                .put("/v1/franchise/"+franchiseId)
                .then()
                .log().all()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        System.out.println(jsonPath.getString("name"));
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("name"), name, "Name mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("email"), random_email, "Email mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("phonenumber"), random_phoneNumber, "Phone Number mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("accountName"), account_name, "Account Name mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("bankName"), bank_name, "Bank Name mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("mapAddress"), map_address, "Map Address mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("houseNo"), house_no, "House No mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("zipCode"), zip_code, "Zip Code mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("streetName"), street_name, "Street Name mismatch");
        AllureLogs.softAssertEquals(softAssert, jsonPath.getString("nearestLandmark"), nearest_landmark, "Nearest Landmark mismatch");
        AllureLogs.softAssertEquals(softAssert, response.getStatusCode(), Integer.parseInt(expectedStatusCode), "Status Code Validation");
        int counter = 1;

            Response getResponse = given()
                    .header("Authorization", "Bearer " + Consoletoken)
                    .when()
                    .get("/v1/franchise/"+franchiseId)
                    .then()
                    .log().all()
                    .extract().response();
            JsonPath json = getResponse.jsonPath();
            AllureLogs.softAssertEquals(softAssert, getResponse.getStatusCode(), 200, "Status Code ");
        AllureLogs.softAssertEquals(softAssert, json.getString("name"), name, "Name mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("email"), random_email, "Email mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("phonenumber"), random_phoneNumber, "Phone Number mismatch");

        AllureLogs.softAssertEquals(softAssert, json.getString("accountName"), account_name, "Account Name mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("bankName"), bank_name, "Bank Name mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("isAnchor"), is_anchor, "Is Anchor mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("mapAddress"), map_address, "Map Address mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("houseNo"), house_no, "House No mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("zipCode"), zip_code, "Zip Code mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("streetName"), street_name, "Street Name mismatch");
        AllureLogs.softAssertEquals(softAssert, json.getString("nearestLandmark"), nearest_landmark, "Nearest Landmark mismatch");
        AllureLogs.softAssertEquals(softAssert, response.getStatusCode(), Integer.parseInt(expectedStatusCode), "Status Code Validation");

        AllureLogs.executeSoftAssertAll(softAssert);
    }
    @DataProvider(name = "Update Franchise")
    public Object[][] GetFranchiseUpdate() {
        return Postive_Data_Extractor.ExcelData("Update Franchise");
    }



    @Test(dependsOnMethods = "CreateFranchise")
    @Description("Rejects all franchises and validates rejection using GET /v1/franchise/{id}")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Reject Franchise")
    public void RejectFranchise() {

        SoftAssert softAssert = new SoftAssert();
        System.out.println("Franchises stored in map: " + franchiseResponseMap.size());
        int counter = 1;
        for (Map.Entry<String, Map<String, String>> entry : franchiseResponseMap.entrySet()) {
            String franchise_id = entry.getKey();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("franchise_id", franchise_id);
        requestBody.put("status", "rejected");
        // Step 1: PUT to reject
        Response putResponse = given()
                .header("Authorization", "Bearer " + Consoletoken)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .put("/v1/franchise/"+franchise_id+"/status")
                .then()
                .log().all()
                .extract().response();
        int putStatusCode = putResponse.getStatusCode();
        System.out.println("GET Response after reject: " + putResponse.asString());
        String actualStatus = putResponse.jsonPath().getString("status");
        AllureLogs.step("[" + counter + "] Rejecting franchise ID: `" + franchise_id + "`");
        AllureLogs.softAssertEquals(softAssert, putStatusCode, 200, "PUT Reject Status Code");
        AllureLogs.softAssertEquals(softAssert, actualStatus, "rejected", "Franchise Status");

            // Step 2: GET after rejection
            Response getResponse = given()
                    .header("Authorization", "Bearer " + Consoletoken)
                    .when()
                    .get("/v1/franchise/"+franchise_id)
                    .then()
                   .log().all()
                    .extract().response();

            String getIdActualStatus = getResponse.jsonPath().getString("status");
            // Step 3: Validate status = rejected
            AllureLogs.step("[" + counter + "] Validating franchise status for ID: `" + franchise_id + "`");
            AllureLogs.softAssertEquals(softAssert, getIdActualStatus, "rejected", "Franchise Status");

            counter++;
    }
        AllureLogs.executeSoftAssertAll(softAssert);
    }


}
