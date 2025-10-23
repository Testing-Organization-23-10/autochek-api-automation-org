package Listener;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static io.restassured.filter.log.RequestLoggingFilter.logRequestTo;
import static io.restassured.filter.log.ResponseLoggingFilter.logResponseTo;

public class AllureLogs implements ITestListener {

    private final ThreadLocal<ByteArrayOutputStream> requestLog = ThreadLocal.withInitial(ByteArrayOutputStream::new);
    private final ThreadLocal<ByteArrayOutputStream> responseLog = ThreadLocal.withInitial(ByteArrayOutputStream::new);

    @Override
    public void onTestStart(ITestResult result) {
        Object[] params = result.getParameters();
        final String severity;
        final String description;
        final String methodName = result.getMethod().getMethodName();

        if (params != null && params.length >= 6) {
            // Based on your method signature:
            // getBanksList(String S_No, String Type, String Severity, String Test_Description, String Country, String Expected_Status)
            severity = String.valueOf(params[1]).trim();        // Severity parameter
            description = String.valueOf(params[2]).trim();     // Test_Description parameter
        } else {
            severity = "";
            description = "";
        }

        // Create combined display name: Method Name - Description
        String displayName = methodName + " - " + description;

        // Log clean description and severity
        System.out.println("--------------- " + displayName + " [Severity: " + severity + "] ---------------");

        // Immediately update the test name to prevent TestNG from formatting it with parameters
        if (!description.isEmpty()) {
            Allure.getLifecycle().updateTestCase(testResult -> {
                testResult.setName(displayName);
                testResult.setFullName(displayName);
                testResult.setHistoryId(displayName);
                // Clear parameters to remove them from detailed view as well
                testResult.getParameters().clear();
            });
        } else {
            // If no description, just use method name
            Allure.getLifecycle().updateTestCase(testResult -> {
                testResult.setName(methodName);
                testResult.setFullName(methodName);
                testResult.setHistoryId(methodName);
                // Clear parameters to remove them from detailed view as well
                testResult.getParameters().clear();
            });
        }

        // Add severity label if available
        if (!severity.isEmpty()) {
            Allure.getLifecycle().updateTestCase(testResult -> {
                testResult.getLabels().add(new io.qameta.allure.model.Label().setName("severity").setValue(severity.toLowerCase()));
            });
        }

        resetLogs();

        RestAssured.replaceFiltersWith(
                logRequestTo(new PrintStream(requestLog.get())),
                logResponseTo(new PrintStream(responseLog.get()))
        );
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        attachRequestLog(getAndReset(requestLog.get()));
        attachResponseLog(getAndReset(responseLog.get()));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        attachRequestLog(getAndReset(requestLog.get()));
        attachResponseLog(getAndReset(responseLog.get()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        attachRequestLog(getAndReset(requestLog.get()));
        attachResponseLog(getAndReset(responseLog.get()));
    }



    // Capture request log as an attachment
    @Attachment(value = "Request Log", type = "application/json")
    public String attachRequestLog(String request) {
        return request;
    }

    // Capture response log as an attachment
    @Attachment(value = "Response Log", type = "application/json")
    public String attachResponseLog(String response)  {
        return response;
    }

    public void logFunctionName(String FName) {
        System.out.println("---------------" + FName + "---------------");
    }

    private void resetLogs() {
        requestLog.get().reset();
        responseLog.get().reset();
    }

    private String getAndReset(ByteArrayOutputStream stream) {
        String content = stream.toString();
        stream.reset();
        return content;
    }
    public static void step(String message) {
        Allure.step(message);
        System.out.println("[STEP] " + message); // Optional: Also log to console
    }
    @Step("Assertion: Verify response status code is {0}")
    public static void assertStatusCode(int actualStatusCode, int expectedStatusCode) {
        Assert.assertEquals(actualStatusCode, expectedStatusCode, "Response status code does not match!");
    }

    // Allure step for assertion to check if the array size in response body is greater than 0
    @Step("Assertion: Verify that the response body array size is greater than 0")
    public static void assertArraySizeGreaterThanZero(Object array) {
        Assert.assertNotNull(array, "Array is null");
        Assert.assertTrue(((java.util.List<?>) array).size() > 0, "Array size is not greater than 0");
    }

    @Step("Assertion: Verify that the response body is not empty")
    public static void assertNotEmptyJson(String responseBody) {
        // Print the response body (for debugging)
        System.out.println("Response Body: " + responseBody);

        // Convert response body to JSON object
        JSONObject jsonResponse = new JSONObject(responseBody);

        // Assert the response body is not empty (i.e., it should have keys)
        if (jsonResponse.isEmpty()) {
            Allure.addAttachment("Response Body", "Expected JSON response body to have content but got an empty JSON.");
            throw new AssertionError("Response body is empty JSON.");
        }
    }
    // SoftAssert step methods
    @Step("SoftAssert: Verify {fieldName} - Expected: '{expected}', Actual: '{actual}'")
    public static void softAssertEquals(org.testng.asserts.SoftAssert softAssert, Object actual, Object expected, String fieldName) {
        softAssert.assertEquals(actual, expected, "Mismatch in " + fieldName);
    }

    @Step("SoftAssert: Verify {fieldName} is not null")
    public static void softAssertNotNull(org.testng.asserts.SoftAssert softAssert, Object actual, String fieldName) {
        softAssert.assertNotNull(actual, fieldName + " should not be null");
    }

    @Step("SoftAssert: Verify {fieldName} is true - Condition: {condition}")
    public static void softAssertTrue(org.testng.asserts.SoftAssert softAssert, boolean condition, String fieldName) {
        softAssert.assertTrue(condition, "Assertion failed for " + fieldName);
    }

    @Step("SoftAssert: Verify {fieldName} contains expected value")
    public static void softAssertContains(org.testng.asserts.SoftAssert softAssert, java.util.List<String> actualList, String expectedValue, String fieldName) {
        boolean contains = actualList != null && actualList.contains(expectedValue);
        softAssert.assertTrue(contains, "Mismatch in " + fieldName + ". Expected list to contain: " + expectedValue);
    }

    @Step("Execute all soft assertions")
    public static void executeSoftAssertAll(org.testng.asserts.SoftAssert softAssert) {
        try {
            softAssert.assertAll();
            Allure.addAttachment("Soft Assertions", "All soft assertions passed successfully");
        } catch (AssertionError e) {
            Allure.addAttachment("Soft Assertion Failures", e.getMessage());
            throw e;
        }
    }
}