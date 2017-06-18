package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.gradle.CustomTable;
import org.gradle.Wait;
import org.gradle.WebElements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class Everything {

	WebDriver driver;

	public static void main(String[] args) throws IOException {

		GatherFunds f = new GatherFunds();

		String[] funds = f.readFile();
		if (funds == null) {
			System.out.println("No funds found to read.  Therefore exiting program.  ");
			f.closeDown();
			return;
		}

		ArrayList<MutualFund> data = new ArrayList<MutualFund>();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date startDate = new Date();

		System.out.println("About to analyze " + funds.length + " fund(s)");
		System.out.println("Start time = " + dateFormat.format(startDate));

		f.launchMorningstar();		
		// Search for the required fund
		for (int i = 0; i < funds.length; i++) {

			MutualFund fund = new MutualFund();

			try {
				// search for the fund
				if(funds[i].length() != 5){
					f.searchFund(funds[i]);
				}
				else{
					f.launchFundByTickerSymbol(funds[i]);
				}
				
				// capture the required information
				f.gatherInformation(funds[i], i + 1, fund);
			} catch (NoSuchElementException e) {
				System.out.println(funds[i] + ", error searching or gathering information for fund: " + e.toString());
				System.out.println(e.getStackTrace());
				System.out.println();
			} catch (WebDriverException e) {
				System.out.println(funds[i] + ", error searching or gathering informationfor fund: " + e.toString());
				System.out.println(e.getStackTrace());
				System.out.println();
			}
			data.add(fund);

			// Write the data to a file
			f.writeInformationToFile(data);
		}

		f.closeDown();

		Date endDate = new Date();
		long seconds = (endDate.getTime() - startDate.getTime()) / 1000;
		System.out.println("End time = " + dateFormat.format(endDate));
		System.out.println("Finished running in " + seconds / 60 + " minutes and " + seconds % 60 + " seconds");

	}
}

class GatherFunds {
	protected WebDriver mDriver;
	private String mInputFileLocation;

	final private String INPUT_FILE_NAME = "input.txt";
	final private String OUTPUT_FILE_NAME = "output.txt";

	/**
	 * Startup morningstar website and initialize driver
	 */
	protected GatherFunds() {

		System.setProperty("webdriver.chrome.driver","C:\\Users\\" + System.getProperty("user.name") + "\\chromedriver.exe");
		
		
		
		/*
		 * String sChromeDriverPath = "C:\\Users\\" + System.getProperty("user.name") + "\\chromedriver.exe";
		Proxy proxy=startProxy();
		proxy.setProxyType(ProxyType.MANUAL); 
		proxy.setNoProxy("");
		ChromeOptions options = new ChromeOptions();
		DesiredCapabilities dc = DesiredCapabilities.chrome();
		dc.setCapability(CapabilityType.PROXY, proxy);
		System.setProperty("webdriver.chrome.driver",sChromeDriverPath);
		dc.setCapability(ChromeOptions.CAPABILITY, options);
		mDriver = new ChromeDriver(dc);
		*/
		
		
		
		
		
		
		
		mDriver = new ChromeDriver(); 		
		//mDriver = new FirefoxDriver();

		mInputFileLocation = "/users/" + System.getProperty("user.name") + "/";
		if (!new File(mInputFileLocation + INPUT_FILE_NAME).exists()) {
			mInputFileLocation = "./";
		}

	}

	protected void launchMorningstar() {
		//String baseUrl = "http://www.morningstar.com/";
		//String baseUrl = "http://www.morningstar.com/funds.html";
		String baseUrl = "http://www.morningstar.com/funds/xnas/vtsax/quote.html";
		
		mDriver.get(baseUrl);
		mDriver.manage().window().maximize();
		//mDriver.get(baseUrl);		
	}

	/**
	 * Reads the input information from input.txt
	 * 
	 * @return - Array of strings containing all the required funds to look up.
	 * @throws IOException
	 */
	protected String[] readFile() throws IOException {
		String sCurrentLine = "";
		BufferedReader br = null;
		String allText = "";

		try {
			br = new BufferedReader(new FileReader(mInputFileLocation + INPUT_FILE_NAME));
		} catch (FileNotFoundException e) {
			mInputFileLocation = null;
			return null;
		}

		do {
			sCurrentLine = br.readLine();
			if (sCurrentLine != null) {
				allText += sCurrentLine.trim().replace("\n", " ").replace("\t", " ");
			}
		} while (sCurrentLine != null);

		br.close();
		return allText.split(";");
	}

	/**
	 * Write the information retrieved to a text file.
	 * 
	 * @param keys
	 *            - keys of information to write
	 * @param information
	 *            - Information to write
	 * @throws IOException
	 */
	protected void writeInformationToFile(ArrayList<MutualFund> fundInformation) throws IOException {

		File file = new File(mInputFileLocation + OUTPUT_FILE_NAME);

		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		// Write the headers
		bw.write(fundInformation.get(0).getHeaders());
		bw.newLine();

		// Write the data
		for (MutualFund singleFund : fundInformation) {
			bw.write(singleFund.getFundTextExcel());
			bw.newLine();
		}

		bw.close();
	}

	/**
	 * Get all the required information for each fund. Return it in a HashTable
	 * using keys as keys
	 * 
	 * @param keys
	 *            - keys to lookup in morningstar
	 * @param fundTickerSymbolOrFundName
	 *            - Used to search fund
	 * @param fundNum
	 *            - Fund # in list
	 * @return
	 */
	protected MutualFund gatherInformation(String fundTickerSymbolOrFundName, int fundNum, MutualFund currentFund) {

		// Make sure page loads
		if (!Wait.sync(mDriver, new String[] { "className" }, new String[] { "gry" }, 10)) {
			System.out.println("Fund (" + fundTickerSymbolOrFundName + ") was not found.  ");
			currentFund.setName(fundTickerSymbolOrFundName);
			currentFund.setTickerSymbol(fundTickerSymbolOrFundName);
			return currentFund;
		}

		// Fund 0 - ticker symbol
		String tickerSymbol = mDriver.findElement(By.className("gry")).getText().trim();
		if (tickerSymbol.length() < 3 && fundTickerSymbolOrFundName.length() < 10) {
			tickerSymbol = fundTickerSymbolOrFundName;
		}
		currentFund.setTickerSymbol(tickerSymbol);

		// Fund 1 - Name
		String name = mDriver.findElement(By.className("r_title")).getText();
		name = name.replace(tickerSymbol, "");
		currentFund.setName(name);

		// Get main table at the top
		
		Wait.sync(mDriver, new String[] { "className", "tagName", "tagName" },
				new String[] { "gr_colm_a2b", "table", "tbody" }, 20);
		
		
		WebElement tableBody = mDriver.findElement(By.className("gr_colm_a2b")).findElement(By.tagName("table"))
				.findElement(By.tagName("tbody"));
		String tableText = tableBody.getText().replace("\n", " ");

		// Fund 2 - Category
		String category = parseString(tableText, "Category", "Investment style");
		if (category.equalsIgnoreCase("")) {
			category = parseString(tableText, "Category", "Credit");
		}
		currentFund.setCategoryAndAbreviatedCategory(category);

		// Fund 4 - Expenses
		String expenses = parseString(tableText, "Expenses", "Fee Level");
		currentFund.setExpenses(expenses);

		// Fund 5 - min investment
		String tableFirstRow = tableBody.findElement(By.tagName("tr")).getText().replace("\n", " ");
		String min = parseString(tableFirstRow, "Min. Inv.", null);
		currentFund.setMinimumInvestment(min);

		// Fund 6 - Turnover
		String turnover = parseString(tableText, "Turnover", "Status");
		currentFund.setTurnOverPercent(turnover);

		// Fund 7 - Total Assets
		String totalAssets;
		String totalAssets1 = parseString(tableText, "Total Assets", "Expenses");
		String totalAssets2 = parseString(tableText, "Total Assets", "Load");
		if (totalAssets1.length() < totalAssets2.length()) {
			totalAssets = totalAssets1;
		} else {
			totalAssets = totalAssets2;
		}
		currentFund.setTotalAssets(totalAssets);

		// Fund 8 - Morningstar rating
		String morningstarRating = mDriver.findElement(By.id("star_span")).getAttribute("class");
		currentFund.setMorningstarRating(morningstarRating);

		CustomTable performanceTable;
		WebElement table;

		// There are to different paths to the performance table
		if (Wait.sync(mDriver, new String[] { "id", "id", "id", "tagName" },
				new String[] { "mspr_performance_wrap", "idPerformanceContent", "mspr_performance", "table" }, 5)) {
			table = mDriver.findElement(By.id("mspr_performance_wrap")).findElement(By.id("idPerformanceContent"))
					.findElement(By.id("mspr_performance")).findElement(By.tagName("table"));
		} else if (Wait.sync(mDriver, new String[] { "id", "id", "tagName" },
				new String[] { "mspr_performance_wrap", "mspr_performance", "table" }, 5)) {
			table = mDriver.findElement(By.id("mspr_performance_wrap")).findElement(By.id("idPerformanceContent"))
					.findElement(By.id("mspr_performance")).findElement(By.tagName("table"));
		} else {// Sometimes there is no performance table
			table = null;
		}

		if (table == null) {// sometimes morningstar does not have a performance
							// table for a particular fund
			currentFund.setOneYearReturn("");
			currentFund.setThreeYearReturn("");
			currentFund.setOneYearReturnVsCategory("");
			currentFund.setThreeYearReturnVsCategory("");
			currentFund.setFiveYearReturnVsCategory("");
		} else {
			performanceTable = new CustomTable(table);

			// Fund 9 - 1 yr return
			currentFund.setOneYearReturn(performanceTable.getCellText(3, 3));

			// Fund 10 - 3 yr return
			currentFund.setThreeYearReturn(performanceTable.getCellText(3, 5));

			int performaanceTableRow = performanceTable.getRowWithCellText("+/- Category", 0);
			// Fund 11 - 1 year ret vs category
			currentFund.setOneYearReturnVsCategory(performanceTable.getCellText(performaanceTableRow, 3));

			// Fund 12 - 3 year ret vs category
			currentFund.setThreeYearReturnVsCategory(performanceTable.getCellText(performaanceTableRow, 5));

			// Fund 13 - 5 year ret vs category
			currentFund.setFiveYearReturnVsCategory(performanceTable.getCellText(performaanceTableRow, 7));
		}

		// Fund 14 - # of managers
		WebElement managementTable;
		if (Wait.sync(mDriver, new String[] { "id", "tagName" }, new String[] { "Snapshot_Management", "table" }, 5)) {
			managementTable = mDriver.findElement(By.id("Snapshot_Management")).findElement(By.tagName("table"));
		} else {
			Wait.sync(mDriver, new String[] { "className", "id", "tagName" },
					new String[] { "gr_colm_c1", "performanceWrap", "table" }, 5);
			managementTable = mDriver.findElement(By.className("gr_colm_c1")).findElement(By.id("performanceWrap"))
					.findElement(By.tagName("table"));
		}

		CustomTable managementCustomTable = new CustomTable(managementTable);
		currentFund.setNumberOfManagers(managementCustomTable.getRowCount() - 2);

		// Fund 15 - First start date of managers
		List<String> datesColumn = Arrays.asList(managementCustomTable.getColumnText(1, 3).split("~"));
		currentFund.setFirstManagerStartDate(datesColumn.get(0));

		// Fund 16 - Last start date of managers
		currentFund.setLastManagerStartDate(datesColumn.get(datesColumn.size() - 1));

		if (Wait.sync(mDriver, new String[] { "id" }, new String[] { "idHeadCompanyProfile" }, 1)) {
			mDriver.findElement(By.id("idHeadCompanyProfile")).click();
		} else if (Wait.sync(mDriver, new String[] { "className", "id", "className" },
				new String[] { "gr_colm_b1", "mspr_performance_wrap", "gr_text_subhead" }, 1)) {
			mDriver.findElement(By.className("gr_colm_b1")).findElement(By.id("mspr_performance_wrap"))
					.findElement(By.className("gr_text_subhead")).click();
		} else {
			mDriver.findElement(By.className("gr_colm_b1")).findElement(By.id("performanceWrap"))
					.findElement(By.className("gr_text_subhead")).click();
		}

		// Make sure page loads
		if (!Wait.sync(mDriver, new String[] { "id", "tagName" }, new String[] { "chart", "table" }, 25)) {
			System.out.println("Table did not load.  Therefore exiting this fund (" + fundTickerSymbolOrFundName + ")");
			return currentFund;
		}

		// Set the fund rating
		currentFund.setRating(fundNum + 1);

		// get row of +/- category column
		WebElement performanceTableElement = mDriver.findElement(By.id("chart")).findElement(By.tagName("table"));
		performanceTable = new CustomTable(performanceTableElement);
		List<WebElement> firstColumn = performanceTableElement.findElement(By.tagName("tBody"))
				.findElements(By.tagName("th"));
		int row;
		if (firstColumn.get(2).getText().contains("+/- Category")) {
			row = 3;
		} else {
			row = 5;
		}

		// Put required information in the HashMap
		for (int i = 0; i < 6; i++) {
			currentFund.setReturnVsCatYearsAgo(performanceTable.getCellText(row, i), 6 - i);
		}

		mDriver.navigate().back();
		Wait.sync(mDriver, new String[] { "className" }, new String[] { "gry" }, 10);

		if (fundNum == 1) {
			System.out.println("Finished " + fundNum + " fund");
		} else {
			System.out.println("Finished " + fundNum + " funds");
		}

		return currentFund;
	}

	protected void closeDown() {
		mDriver.close();
		mDriver.quit();
	}

	/**
	 * search for and select the fund
	 * 
	 * @param fundTickerSymbol
	 *            - Fund to search for. Must be an exact match.
	 */
	protected void searchFund(String fundTickerSymbol) {
		//WebElement searchBox = mDriver.findElement(By.id("AutoCompleteBox"));
		WebElement searchBox = mDriver.findElement(By.id("qs0"));
		
		//class=AutoCompleteBox

		// search for the fund
		searchBox.sendKeys("");
		searchBox.sendKeys(fundTickerSymbol.trim());
		mDriver.findElements(By.className("hqt_button")).get(0).click();
		Wait.sync(mDriver, new String[] { "className" }, new String[] { "gry" }, 25);
	}
	
	/**
	 * Launches the correct page directly by entering the URL
	 * @param tickerSymbol -Ticker symbol of fund to look up
	 */
	protected void launchFundByTickerSymbol(String tickerSymbol){
		mDriver.get("http://www.morningstar.com/funds/xnas/" + tickerSymbol + "/quote.html");
		Wait.sync(mDriver, new String[] { "className" }, new String[] { "gry" }, 25);
	}

	/**
	 * Parses a string out.
	 * 
	 * <pre>
	 * Example String x = parseString("abcdefghijkl", "c", "gh") x now contains
	 * "def"
	 * 
	 * @param allText
	 * @param before
	 * @param after
	 * @return
	 */
	private String parseString(String allText, String before, String after) {
		int beforePos = allText.toLowerCase().indexOf(before.toLowerCase());
		if (beforePos >= 0) {
			beforePos = beforePos + before.length();
		}
		if (after == null) {
			return allText.substring(beforePos);
		}

		int afterPos = allText.toLowerCase().indexOf(after.toLowerCase());

		if (beforePos < 0 || afterPos < 0)
			return "";
		if (afterPos < beforePos) {
			return allText.substring(beforePos).trim();
		}

		return allText.substring(beforePos, afterPos).trim();
	}

}
