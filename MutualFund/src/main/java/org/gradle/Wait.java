package org.gradle;


import static org.testng.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Provides blocking wait operations. This is better than sleeping a thread to enforce a wait.
 * 
 * @author vol5646
 * 
 */
public class Wait {

	private Wait() {
	}

	/**
	 * Returns a new instance of Wait that is initialized with a default timeout of 10 seconds
	 * 
	 * @param driver
	 *            being used to drive the browser
	 * @return a new Wait instance
	 */
	public static WebDriverWait getWait(WebDriver driver) {
		return getWait(driver, 10);
	}

	/**
	 * Returns a new instance of Wait that is initialized with the specified timeout length
	 * 
	 * @param driver
	 *            being used to drive the browser
	 * @param timeOutInSeconds
	 * @return a new WebDriverWait instance
	 */
	public static WebDriverWait getWait(WebDriver driver, long timeOutInSeconds) {
		return new WebDriverWait(driver, timeOutInSeconds);
	}

	/**
	 * Provides a way of doing dynamic waits
	 * 
	 * @param webElement
	 *            The WebElement being waited on
	 * @return
	 */
	public static ExpectedCondition<WebElement> isDisplayed(final WebElement webElement) {
		return new ExpectedCondition<WebElement>() {

			public WebElement apply(WebDriver arg0) {
				if (webElement.isDisplayed())
					return webElement;
				return null;
			}
		};
	}

	/**
	 * Waits until numNanoSeconds have expired
	 * 
	 * @author ber5840
	 * @param numNanoSeconds
	 */
	public static void sleep(int numNanoSeconds) {
		try {
			Thread.sleep(numNanoSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Waits until either the object in question exists on the screen or the timeout is reached. If
	 * the timeout is reached then the test will fail and a screenshot will be taken of the driver.
	 * 
	 * @author ber5840
	 * @param driver
	 *            - Driver
	 * @param timeOutInSeconds
	 *            - Time to wait
	 * @param webElement
	 *            - Object to wait for
	 * @param objectName
	 *            - Description of object
	 * 
	 * @throws IOException
	 */
	public static void WaitUntilObjectExists(WebDriver driver, long timeOutInSeconds, WebElement webElement, String objectName) {
		try {
			Wait.getWait(driver, timeOutInSeconds).until(ExpectedConditions.visibilityOf(webElement));
		} catch(NoSuchElementException e) {
			String path = takeScreenshot(driver);
			if (objectName == null) {
				fail("Navigation failed to web element.\nScreenshot =" + path + "\nEnding this test");
			} else {
				fail("Navigation failed to web element.\nName = (" + objectName + ")\nScreenshot=" + path
						+ "\nEnding this test");
			}
		}
		catch(TimeoutException e){
			String path = takeScreenshot(driver);
			if (objectName == null) {
				fail("Navigation failed to web element.\nScreenshot =" + path + "\nEnding this test");
			} else {
				fail("Navigation failed to web element.\nName = (" + objectName + ")\nScreenshot=" + path
						+ "\nEnding this test");
			}
		}
	}
	
	public static void WaitUntilObjectExists(WebDriver driver, long timeOutInSeconds, WebElement webElement) {
		WaitUntilObjectExists(driver, timeOutInSeconds, webElement, null);
	}

	/**
	 * 
	 * @param driver
	 *            - Driver
	 * @param timeOutInSeconds
	 *            - Time to wait
	 * @param webElement
	 *            - Object to wait for
	 * @return - True if object exists and False if it doesn't
	 */
	public static boolean exist(WebDriver driver, long timeOutInSeconds, WebElement webElement) {
		try {
			Wait.getWait(driver, timeOutInSeconds).until(ExpectedConditions.visibilityOf(webElement));
		} catch (NoSuchElementException e) {
			return false;
		}
		catch(TimeoutException e){
			return false;
		}
		return true;
	}
	
	/**
	 * Waits for the page title to equal "title" or for the timeOutInSeconds to pass.  Returns true if title matches and false if timeout reached.  
	 * @param driver - The driver object
	 * @param title - Name of title on the page.  Inside the <title> tag.  
	 * @param timeOutInSeconds - timeout in seconds
	 * @return - true if title was achieved and false if timeout was reached.  
	 * @throws ParseException
	 */
	public static boolean waitForTitle(WebDriver driver, String title, long timeOutInSeconds) throws ParseException {
		java.util.Date start = new java.util.Date();
		java.util.Date current = new java.util.Date();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		do{
			current = new java.util.Date();
			Date d1 = format.parse(format.format(start));
			Date d2 = format.parse(format.format(current));
			
			if(d2.getTime() - d1.getTime() > (timeOutInSeconds*1000)){
				break;
			}
			Wait.sleep(1000);
		}while(!driver.getTitle().equalsIgnoreCase(title));
		
		return driver.getTitle().equalsIgnoreCase(title);
	}

	/**
	 * Takes a screenshot of the current driver
	 * 
	 * @param driver
	 *            - Driver
	 * @return - path of screenshot
	 */
	private static String takeScreenshot(WebDriver driver) {

		String path = "";
		// Take the screenshot
		File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

		SimpleDateFormat currentDateFormatter = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat currentTimeFormatter = new SimpleDateFormat("kkmmss");

		path = "C:\\SeleniumScreenshots\\" + currentDateFormatter.format(new Date()) + "\\" + currentTimeFormatter.format(new Date())
				+ ".png";
		// Move the screenshot to where the logs are
		File destination = new File(path);

		FileUtils.deleteQuietly(destination);
		try {
			FileUtils.moveFile(screenshot, destination);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	/**
	 *Waits for an element to exist on the screen.  Can also wait for multiple parent elements to exist.  
	 *Sync only handles findElement by NOT findElements.  
	 *Sync only handles By.className, By.tagName, and By.id
	 *<pre>
	 *	//Example:
	 *	boolean found = sync(new String[] {"className", "tagName", "tagName"}, new String[] {"gr_colm_a2b", "table", "tbody"}, 10);
	 *	//if found contains true the following line will not timeout.  
	 *	WebElement tableBody = mDriver.findElement(By.className("gr_colm_a2b")).findElement(By.tagName("table")).findElement(By.tagName("tbody"));
	 *</pre>
	 * 
	 * @param propertyType - array of properties to look for
	 * @param propertyValue - array of values to look for
	 * @param timeout - Max # of seconds to wait
	 * @return - true if child WebElement found successfully, false if not found 
	 * 
	 */
	public static boolean sync(WebDriver driver, String[] propertyType, String[] propertyValue, int timeout)
	{		
		WebElement parent = null;		
		for(int i=0; i<propertyType.length; i++){
			int j;
			for(j=0; j<timeout; j++){
				try{
					//First time through, look off the WebDriver
					if(parent==null){
						if(propertyType[i].equalsIgnoreCase("className")){
							parent = driver.findElement(By.className(propertyValue[i]));
						}
						else if(propertyType[i].equalsIgnoreCase("tagName")){
							parent = driver.findElement(By.tagName(propertyValue[i]));
						}
						else if(propertyType[i].equalsIgnoreCase("id")){
							parent = driver.findElement(By.id(propertyValue[i]));
						}
						else{
							System.out.println(propertyType[i] + " is not a valid property type in sync method");
							return false;
						}
					}else
						//second or more time through, look off the parent WebElement
						if(propertyType[i].equalsIgnoreCase("className")){
							parent = parent.findElement(By.className(propertyValue[i]));
						}
						else if(propertyType[i].equalsIgnoreCase("tagName")){
							parent = parent.findElement(By.tagName(propertyValue[i]));
						}
						else if(propertyType[i].equalsIgnoreCase("id")){
							parent = parent.findElement(By.id(propertyValue[i]));
						}
						else{
							System.out.println(propertyType[i] + " is not a valid property type in sync method");
							return false;
						}
					break;
				}
				catch (NoSuchElementException e1){}
				catch (TimeoutException e2){}
				sleep(1000);
			}
			if(j==timeout){
				return false;
			}
		}
		if(timeout > 15){
			sleep(500);
		}
		else{
			sleep(100);
		}
		return true;
	}
}
