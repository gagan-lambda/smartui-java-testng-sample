package com.lambdatest;

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
import org.json.JSONObject;
public class TestNGSmartUI {

  private RemoteWebDriver driver;
  private String Status = "Passed";
  private String githubURL = System.getenv("GITHUB_URL");

  @BeforeMethod
  public void setup(Method m, ITestContext ctx) throws MalformedURLException {
    String username = System.getenv("LT_USERNAME") == null
      ? "Your LT Username"
      : System.getenv("LT_USERNAME");
    String authkey = System.getenv("LT_ACCESS_KEY") == null
      ? "Your LT AccessKey"
      : System.getenv("LT_ACCESS_KEY");
    String hub = "@hub.lambdatest.com/wd/hub";

    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("platform", "Windows 10");
    caps.setCapability("browserName", "chrome");
    caps.setCapability("version", "latest");
    caps.setCapability("build", "Veeva-webhook");
    caps.setCapability("name", m.getName() + " - " + this.getClass().getName());
    caps.setCapability("plugin", "git-testng");
    caps.setCapability("smartUI.project", "Veeva-webhook");
   // caps.setCapability("smartUI.baseline", true);
    caps.setCapability("selenium_version", "4.8.0");


    if (githubURL != null) {
      Map<String, String> github = new HashMap<String, String>();
      github.put("url", githubURL);
      caps.setCapability("github", github);
    }
    System.out.println(caps);
    driver =
      new RemoteWebDriver(
        new URL("https://" + username + ":" + authkey + hub),
        caps
      );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void basicTest() throws InterruptedException {
    String spanText;
    System.out.println("Loading URL");
    // opening URL
    driver.get("https://www.flipkart.com");
    Thread.sleep(5000);
    System.out.println("Taking FullPage Screenshot");
    // Capturing Screenshot
    driver.executeScript("smartui.takeFullPageScreenshot=home-page");
    Thread.sleep(10000);
    System.out.println("Response is : ");
    // Fetching Status of Screenshot
    Map<String, Object> response = (Map<String, Object>) (((JavascriptExecutor)driver).executeScript("smartui.fetchScreenshotStatus=home-page"));
    System.out.println("Response is : "+response);
// Check if screenshot  Status is rejected
List<Map<String, Object>> screenshots = (List<Map<String, Object>>) response.get("screenshotsData");
for (Map<String, Object> shot : screenshots) {
    if ("rejected".equalsIgnoreCase((String) shot.get("screenshotStatus"))) {
        Status = "failed";
        break;
    }
}
  }
  @AfterMethod
  public void tearDown() {
    //Updating status of Functional Test
    driver.executeScript("lambda-status=" + Status);
    driver.quit();
  }
}