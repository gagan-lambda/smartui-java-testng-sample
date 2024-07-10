package com.lambdatest.sdk;

import io.github.lambdatest.SmartUISnapshot;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartUIIgnoreSelectSDKCould {


  private RemoteWebDriver driver;
  private String Status = "failed";
  private String githubURL = System.getenv("GITHUB_URL");

  @BeforeMethod
  public void setup(Method m, ITestContext ctx) throws MalformedURLException {
    String username = System.getenv("LT_USERNAME") == null ? "Your LT Username" : System.getenv("LT_USERNAME");
    String authkey = System.getenv("LT_ACCESS_KEY") == null ? "Your LT AccessKey" : System.getenv("LT_ACCESS_KEY");
    String hub = "@hub.lambdatest.com/wd/hub";

    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("platform", "Catalina");
    caps.setCapability("browserName", "chrome");
    caps.setCapability("version", "latest");
    caps.setCapability("build", "TestNG With Java");
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
    Map<String, Object> ignoreOptions = new HashMap<>();
    List<String> ignoreID = Arrays.asList("api-requests");
    List<String> ignoreCSSSelectors = Arrays.asList(".overflow-hidden section:first-of-type",
      ".overflow-hidden section:nth-of-type(2)", "section:nth-of-type(6) .swiper", "section:nth-of-type(7) .swiper");
    Map<String, List<String>> ignoreDOM = new HashMap<>();
    ignoreDOM.put("id", ignoreID);
    ignoreDOM.put("cssSelector", ignoreCSSSelectors);
    ignoreOptions.put("ignoreDOM", ignoreDOM);

    Map<String, Object> selectOptions = new HashMap<>();
    List<String> selectID = Arrays.asList("");
    List<String> selectCSSSelectors = Arrays.asList("h1.heading-h1");
    Map<String, List<String>> selectDOM = new HashMap<>();
    selectDOM.put("cssSelector", selectCSSSelectors);
    selectOptions.put("selectDOM", selectDOM);

    System.out.println("Loading Url");
    driver.get("https://ipinfo.io/");
    SmartUISnapshot.smartuiSnapshot(driver, "ignoreOptionsScreenshot", ignoreOptions);
    Thread.sleep(2000);
    SmartUISnapshot.smartuiSnapshot(driver, "selectOptionsScreenshot", selectOptions);
    Thread.sleep(1000);
    System.out.println("Test Finished");
  }

  @AfterMethod
  public void tearDown() {
    driver.executeScript("lambda-status=" + Status);
    driver.quit();
  }
}
