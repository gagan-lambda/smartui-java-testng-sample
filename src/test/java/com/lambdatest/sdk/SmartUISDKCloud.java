package com.lambdatest.sdk;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.github.lambdatest.SmartUISnapshot;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.json.JSONArray;
import org.json.JSONObject;

public class SmartUISDKCloud {

    private RemoteWebDriver driver;
    private String Status = "failed";
    //private String projectToken = ""; // Update your own project token
    private String githubURL = System.getenv("GITHUB_URL");
    private String buildId = System.getenv("SMARTUI_BUILD_ID");
   // private String buildName = System.getenv("SMARTUI_BUILD_NAME");
    private String username = System.getenv("LT_USERNAME") == null ? "Your LT Username" : System.getenv("LT_USERNAME");
    private String authkey = System.getenv("LT_ACCESS_KEY") == null ? "Your LT AccessKey" : System.getenv("LT_ACCESS_KEY");

    @BeforeMethod
    public void setup(Method m, ITestContext ctx) throws MalformedURLException {
        String hub = "@hub.lambdatest.com/wd/hub";

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platform", "Catalina");
        caps.setCapability("browserName", "chrome");
        caps.setCapability("version", "latest");
        caps.setCapability("build", "TestNG With Java");
       // caps.setCapability("smartUI.baseline", true);
        caps.setCapability("name", m.getName() + " - " + this.getClass().getName());
        
        if (githubURL != null) {
            Map<String, String> github = new HashMap<String, String>();
            github.put("url",githubURL);
            caps.setCapability("github", github);
        }
        System.out.println(caps);
        driver = new RemoteWebDriver(new URL("https://" + username + ":" + authkey + hub), caps);

    }

    @Test
    public void basicTest() throws Exception {
        System.out.println("Loading Url");
        driver.get("https://flipkart.com");
        Thread.sleep(1000);
        SmartUISnapshot.smartuiSnapshot(driver, "Flipkart");
        Thread.sleep(30000);
        System.out.println("Build ID is : " + buildId); // Getting this value from Line 30

    }

    @AfterMethod
    public void tearDown() {
        String projectName = "veeva-sdk";
        //String apiUrl = "https://api.lambdatest.com/automation/smart-ui/screenshot/build/status";
        String apiUrl = "https://api.lambdatest.com/smartui/2.0/build/screenshots"; // New Updated API

        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + authkey).getBytes());

        Response response = RestAssured
                .given()
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Accept", "application/json")
                    .queryParam("project_name", projectName)
                    .queryParam("build_id", buildId)
                    .queryParam("baseline", false)
                   // .queryParam("projectToken", projectToken)
    

                .when()
                    .get(apiUrl)
                .then()
                    .statusCode(200)
                    .extract()
                    .response();
                    
       

        JSONObject json = new JSONObject(response.asString());
        System.out.println("Respnse for API is : " + json);
        JSONArray screenshots = json.getJSONArray("screenshots");

        boolean hasRejection = false;

        for (int i = 0; i < screenshots.length(); i++) {
            JSONObject screenshot = screenshots.getJSONObject(i);
            String status = screenshot.getString("status");

            if ("rejected".equalsIgnoreCase(status)) {
                System.out.println("Rejected screenshot found: " + screenshot.getString("screenshot_name"));
                hasRejection = true;
                break; // Optional: stop after first rejection
            }
        }

        // Set Test status
        Status = hasRejection ? "failed" : "passed";
        ((JavascriptExecutor) driver).executeScript("lambda-status=" + Status);
        driver.quit();
    }

}
