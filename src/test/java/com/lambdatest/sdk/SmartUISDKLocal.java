package com.lambdatest.sdk;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.github.lambdatest.SmartUISnapshot;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class SmartUISDKLocal {

    private RemoteWebDriver driver;

    @BeforeMethod
    public void setup(Method m, ITestContext ctx) throws MalformedURLException {
        WebDriverManager.chromedriver().setup();
        // Create a new instance of the Chrome browser
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        System.out.println("Chrome Driver initiated");
    }

    @Test
    public void basicTest() throws Exception {
        System.out.println("Loading Url");
        driver.get("https://www.lambdatest.com/support/docs/");
        Thread.sleep(1000);
        SmartUISnapshot.smartuiSnapshot(driver, "docs");
        Thread.sleep(5000);
        driver.get("https://www.lambdatest.com");
        Thread.sleep(1000);
        SmartUISnapshot.smartuiSnapshot(driver, "homepage");
        Thread.sleep(1000);
        System.out.println("Test Finished");
    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

}
