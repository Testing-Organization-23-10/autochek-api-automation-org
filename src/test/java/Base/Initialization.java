package Base;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeSuite;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Initialization {

    public static Map<String,String> franchiseAdminToken,accountManagerToken,dealerDsaToken,dsaAgentToken,consoleAdminToken,InspectorAppToken;
    @BeforeSuite

    public void cleanAllureResults() {
        File allureResultsDir = new File("allure-results");
        if (allureResultsDir.exists() && allureResultsDir.isDirectory()) {
            for (File file : allureResultsDir.listFiles()) {
                file.delete();
            }
            System.out.println("✅ Allure results folder cleaned before suite execution.");
        }
    }
    public void initializeEnvironment() {
        urlSetUp();
        franchiseAdminToken = generateAuthToken("suhail+1@frugaltestingin.com", "Autochek@123");
        accountManagerToken = generateAuthToken("sumasri@frugaltesting.com", "AutochekF123");
        dealerDsaToken = generateAuthToken("eshareddy+100@frugaltestingin.com", "Autochek@123");
        dsaAgentToken = generateAuthToken("suhail@frugaltestingin.com", "Autochek@123");
        consoleAdminToken = generateAuthToken("bharti@frugaltesting.com", "AutochekF123");
    }

    public void urlSetUp() {
        RestAssured.baseURI = "https://api.staging.myautochek.com";
    }

    public Map<String, String> generateAuthToken(String email, String password) {
        String requestBody = "{\n" +
                "  \"email\": \"" + email + "\",\n" +
                "  \"password\": \"" + password + "\"\n" +
                "}";

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/v1/auth/login");

        String token = response.jsonPath().getString("token");
        String userId = response.jsonPath().getString("user.id");
        Map<String,String> auth_details = new HashMap<>();
        auth_details.put("token",token);
        auth_details.put("userId",userId);
        System.out.println("Token generated for " + email + ": " + token + " User id : "+userId);
        return auth_details;
    }
}













