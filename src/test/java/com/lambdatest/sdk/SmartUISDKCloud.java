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
import org.json.JSONObject;

public class SmartUISDKCloud {

    private RemoteWebDriver driver;
    private String Status = "failed";
    private String projectToken = "2838#cad0c854-kdjd-4c08-80d7-3146d83e0cf0#veeva-sdk"; // Update your own project token
    private String githubURL = System.getenv("GITHUB_URL");
    private String buildId = System.getenv("SMARTUI_BUILD_ID");
    private String buildName = System.getProperty("SMARTUI_BUILD_NAME");
    private String username = System.getenv("LT_USERNAME") == null ? "Your LT Username" : System.getenv("LT_USERNAME");
    private String authkey = System.getenv("LT_ACCESS_KEY") == null ? "Your LT AccessKey" : System.getenv("LT_ACCESS_KEY");

    @BeforeMethod
    public void setup(Method m, ITestContext ctx) throws MalformedURLException {
      //  String username = System.getenv("LT_USERNAME") == null ? "Your LT Username" : System.getenv("LT_USERNAME");
        //String authkey = System.getenv("LT_ACCESS_KEY") == null ? "Your LT AccessKey" : System.getenv("LT_ACCESS_KEY");
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
        System.out.println("Build ID is : " + buildId);

    }

    @AfterMethod
    public void tearDown() {
String apiUrl = "https://api.lambdatest.com/automation/smart-ui/screenshot/build/status";

        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + authkey).getBytes());

        Response response = RestAssured
                .given()
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Accept", "application/json")
                    .queryParam("projectToken", projectToken)
                    .queryParam("buildId", buildId)

                .when()
                    .get(apiUrl)
                .then()
                    .statusCode(200)
                    .extract()
                    .response();

        JSONObject json = new JSONObject(response.asString());
        String buildStatus = json.getJSONObject("data").getString("buildStatus");

        System.out.println("Build status is: " + buildStatus);

         Status = buildStatus.equalsIgnoreCase("Rejected") ? "failed" : "passed";

        // Send status back to LambdaTest
        ((JavascriptExecutor) driver).executeScript("lambda-status=" + Status);
        driver.quit();
    }

}
