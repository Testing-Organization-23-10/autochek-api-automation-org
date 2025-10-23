# API Automation Test Suite

This repository contains a comprehensive **API Automation Test Suite** for validating all core APIs of the project. It includes both **positive** and **negative** test cases to ensure APIs behave correctly under different scenarios and edge cases.

## Project Structure

- **`Base`**: Handles authentication and token generation for API calls.
- **`Listener`**: Contains custom listeners like `AllureLogs` for capturing detailed test execution logs.
- **`TestData`**: Stores input data for test cases, including positive and negative API scenarios and sample files.
- **`Utils`**: Helper classes for extracting test data and managing inputs dynamically.
- **`autochek_api_positive`**: Positive test cases validating successful API responses.
- **`autochek_api_negative`**: Negative test cases validating failure scenarios and error handling.

## CI/CD Integration

The suite is integrated with **GitHub Actions** to run automatically on pull requests or manual triggers. The workflow:
- Sets up Java (JDK 22) and Maven.
- Executes all API tests using TestNG.
- Generates **Allure Reports** for test results.
- Archives historical reports in `allure-reports` branch.
- Blocks PRs on severe failures (blocker/critical/SEV1/P0).

This setup ensures continuous validation of APIs, providing quick feedback on code changes and maintaining consistent API quality.

## Reporting

All test results are captured in **Allure Reports**, which include:
- Test execution status.
- Severity labeling.
- Historical trends for regression tracking.
