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
import java.util.Locale;

import org.gradle.CustomTable;
import org.gradle.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class Everything {

	public static void main(String... args) throws IOException {

		GatherFunds f = new GatherFunds();

		String[] funds = f.readFile();
		if (funds == null) {
			System.out
					.println("No funds found to read.  Therefore exiting program.  ");
			f.closeDown();
			return;
		}

		// ArrayList<HashMap<String, String>> data = new
		// ArrayList<HashMap<String, String>>();
		ArrayList<MutualFund> data = new ArrayList<MutualFund>();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
		Date startDate = new Date();

		System.out.println("About to analyze " + funds.length + " fund(s)");
		System.out.println("Start time = " + dateFormat.format(startDate));

		f.launchMorningstar(funds[0]);
		// Search for the required fund
		for (int i = 0; i < funds.length; i++) {

			MutualFund fund = new MutualFund();

			try {
				// search for the fund
				f.searchFund(funds[i]);

				// capture the required information
				f.gatherInformation(funds[i], i + 1, fund);
			} catch (NoSuchElementException e) {
				System.out
						.println(funds[i]
								+ ", error searching or gathering information for fund: "
								+ e.toString());
				System.out.println(e.getStackTrace());
				System.out.println(e.getMessage());
				System.out.println();
			} catch (WebDriverException e) {
				System.out
						.println(funds[i]
								+ ", error searching or gathering information for fund: "
								+ e.toString());
				System.out.println(e.getMessage());
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
		System.out.println("Finished running in " + seconds / 60
				+ " minutes and " + seconds % 60 + " seconds");

	}
}

class GatherFunds {
	protected WebDriver mDriver;
	private String mFileLocation;

	final static private String INPUT_FILE_NAME = "input.txt";
	final static private String OUTPUT_FILE_NAME = "output.txt";

	/**
	 * Startup morningstar website and initialize driver
	 */
	public GatherFunds() {		
		
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\" + System.getProperty("user.name")
						+ "\\chromedriver.exe");
		mDriver = new ChromeDriver();
		// mDriver = new FirefoxDriver();

		mFileLocation = "/users/" + System.getProperty("user.name") + "/";
		if (!new File(mFileLocation + INPUT_FILE_NAME).exists()) {
			mFileLocation = "./";
		}

	}

	public void launchMorningstar(String firstFund) {
		// String baseUrl = "http://www.morningstar.com/";
		String baseUrl = "http://quotes.morningstar.com/fund/crsox/f?t=" + firstFund;
		mDriver.get(baseUrl);
		mDriver.manage().window().maximize();
		mDriver.get(baseUrl);
		syncQuotePage(20);
	}
	
	/**
	 * Reads the input information from input.txt
	 * 
	 * @return - Array of strings containing all the required funds to look up.
	 * @throws IOException
	 */
	public String[] readFile() throws IOException {		
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(mFileLocation
					+ INPUT_FILE_NAME));
		} catch (FileNotFoundException e) {
			return null;
		}

		String sCurrentLine = "";
//		String allText = "";
		StringBuffer allText = new StringBuffer(100);
		do {
			sCurrentLine = br.readLine();
			if (sCurrentLine != null) {
				allText.append(sCurrentLine.trim().replace("\n", " ")
						.replace("\t", " "));
//				allText += sCurrentLine.trim().replace("\n", " ")
//						.replace("\t", " ");
			}
		} while (sCurrentLine != null);

		br.close();
		return allText.toString().split(";");
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
	public void writeInformationToFile(List<MutualFund> fundInformation)
			throws IOException {

		File file = new File(mFileLocation + OUTPUT_FILE_NAME);

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
	public MutualFund gatherInformation(String fundTickerSymbolOrFundName,
			int fundNum, MutualFund currentFund) {

		// Make sure page loads		
		if (!syncQuotePage(15)) {
			System.out.println("Fund (" + fundTickerSymbolOrFundName
					+ ") was not found.  ");
			currentFund.setName(fundTickerSymbolOrFundName);
			currentFund.setTickerSymbol(fundTickerSymbolOrFundName);
			return currentFund;
		}

		// Fund 0 - ticker symbol
		String tickerSymbol = mDriver.findElement(By.className("gry"))
				.getText().trim();
		if (tickerSymbol.length() < 3
				&& fundTickerSymbolOrFundName.length() < 10) {
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
		WebElement tableBody = mDriver.findElement(By.className("gr_colm_a2b"))
				.findElement(By.tagName("table"))
				.findElement(By.tagName("tbody"));
		String tableText = tableBody.getText().replace("\n", " ");

		// Fund 2 - Category
		String category = parseString(tableText, "Category", "Investment style");
		if ("".equalsIgnoreCase(category)) {
			category = parseString(tableText, "Category", "Credit");
		}
		currentFund.setCategoryAndAbreviatedCategory(category);

		// Fund 4 - Expenses
		String expenses = parseString(tableText, "Expenses", "Fee Level");
		currentFund.setExpenses(expenses);

		// Fund 5 - min investment
		String tableFirstRow = tableBody.findElement(By.tagName("tr"))
				.getText().replace("\n", " ");
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
		String morningstarRating = mDriver.findElement(By.id("star_span"))
				.getAttribute("class");
		currentFund.setMorningstarRating(morningstarRating);

		CustomTable performanceTable;
		WebElement table;

		// There are to different paths to the performance table
		if (Wait.sync(mDriver, new String[] { "id", "id", "id", "tagName" },
				new String[] { "mspr_performance_wrap", "idPerformanceContent",
						"mspr_performance", "table" }, 5)) {
			table = mDriver.findElement(By.id("mspr_performance_wrap"))
					.findElement(By.id("idPerformanceContent"))
					.findElement(By.id("mspr_performance"))
					.findElement(By.tagName("table"));
		} else if (Wait.sync(mDriver, new String[] { "id", "id", "tagName" },
				new String[] { "mspr_performance_wrap", "mspr_performance",
						"table" }, 5)) {
			table = mDriver.findElement(By.id("mspr_performance_wrap"))
					.findElement(By.id("idPerformanceContent"))
					.findElement(By.id("mspr_performance"))
					.findElement(By.tagName("table"));
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

			int performaanceTableRow = performanceTable.getRowWithCellText(
					"+/- Category", 0);
			// Fund 11 - 1 year ret vs category
			currentFund.setOneYearReturnVsCategory(performanceTable
					.getCellText(performaanceTableRow, 3));

			// Fund 12 - 3 year ret vs category
			currentFund.setThreeYearReturnVsCategory(performanceTable
					.getCellText(performaanceTableRow, 5));

			// Fund 13 - 5 year ret vs category
			currentFund.setFiveYearReturnVsCategory(performanceTable
					.getCellText(performaanceTableRow, 7));
		}

		// Fund 14 - # of managers
		WebElement managementTable;
		if (Wait.sync(mDriver, new String[] { "id", "tagName" }, new String[] {
				"Snapshot_Management", "table" }, 5)) {
			managementTable = mDriver.findElement(By.id("Snapshot_Management"))
					.findElement(By.tagName("table"));
		} else {
			Wait.sync(mDriver, new String[] { "className", "id", "tagName" },
					new String[] { "gr_colm_c1", "performanceWrap", "table" },
					5);
			managementTable = mDriver.findElement(By.className("gr_colm_c1"))
					.findElement(By.id("performanceWrap"))
					.findElement(By.tagName("table"));
		}

		CustomTable managementCustomTable = new CustomTable(managementTable);
		currentFund
				.setNumberOfManagers(managementCustomTable.getRowCount() - 2);

		// Fund 15 - First start date of managers
		List<String> datesColumn = Arrays.asList(managementCustomTable
				.getColumnText(1, 3).split("~"));
		currentFund.setFirstManagerStartDate(datesColumn.get(0));

		// Fund 16 - Last start date of managers
		currentFund
				.setLastManagerStartDate(datesColumn.get(datesColumn.size() - 1));

		if (Wait.sync(mDriver, new String[] { "id" },
				new String[] { "idHeadCompanyProfile" }, 1)) {
			mDriver.findElement(By.id("idHeadCompanyProfile")).click();
		} else if (Wait.sync(mDriver, new String[] { "className", "id",
				"className" }, new String[] { "gr_colm_b1",
				"mspr_performance_wrap", "gr_text_subhead" }, 1)) {
			mDriver.findElement(By.className("gr_colm_b1"))
					.findElement(By.id("mspr_performance_wrap"))
					.findElement(By.className("gr_text_subhead")).click();
		} else {
			mDriver.findElement(By.className("gr_colm_b1"))
					.findElement(By.id("performanceWrap"))
					.findElement(By.className("gr_text_subhead")).click();
		}

		// Make sure page loads
		if (!Wait.sync(mDriver, new String[] { "id", "tagName" }, new String[] {
				"chart", "table" }, 25)) {
			System.out
					.println("Table did not load.  Therefore exiting this fund ("
							+ fundTickerSymbolOrFundName + ")");
			return currentFund;
		}

		// Set the fund rating
		currentFund.setRating(fundNum + 1);

		// get row of +/- category column
		WebElement performanceTableElement = mDriver
				.findElement(By.id("chart")).findElement(By.tagName("table"));
		performanceTable = new CustomTable(performanceTableElement);
		List<WebElement> firstColumn = performanceTableElement.findElement(
				By.tagName("tBody")).findElements(By.tagName("th"));
		int row;
		if (firstColumn.get(2).getText().contains("+/- Category")) {
			row = 3;
		} else {
			row = 5;
		}

		for (int i = 0; i < 6; i++) {
			currentFund.setReturnVsCatYearsAgo(
					performanceTable.getCellText(row, i), 6 - i);
		}

		mDriver.navigate().back();
		syncQuotePage(10);

		if (fundNum == 1) {
			System.out.println("Finished " + fundNum + " fund");
		} else {
			System.out.println("Finished " + fundNum + " funds");
		}

		return currentFund;
	}

	public void closeDown() {
		mDriver.close();
		mDriver.quit();
	}

	/**
	 * search for and select the fund
	 * 
	 * @param fundTickerSymbol
	 *            - Fund to search for. Must be an exact match.
	 */
	public void searchFund(String fundTickerSymbol) {
		WebElement searchBox;
		// try{
		searchBox = mDriver.findElement(By.id("AutoCompleteBox"));
		// }
		// catch(NoSuchElementException e){
		// searchBox = mDriver.findElement(By.id("qs0"));
		// }

		// search for the fund
		searchBox.sendKeys("");
		searchBox.sendKeys(fundTickerSymbol.trim());
		mDriver.findElements(By.className("hqt_button")).get(0).click();
		syncQuotePage(30);
		

	}
	
	private boolean syncQuotePage(int timeout){
		try{
			Wait.sync(mDriver, new String[] { "id", "id" }, new String[] {
					"mainGraphHolder", "targetHolder" }, timeout);
			Wait.sync(mDriver, new String[]{"id", "tagName", "tagName"}, new String[]{"targetHolder", "div", "canvas"}, timeout);
			return Wait.sync(mDriver, new String[] { "className" },
				new String[] { "gry" }, 5);
		}
		catch(StaleElementReferenceException e){
			mDriver.navigate().refresh();
			System.out.println("Stale element exception triggered an caught");
		}
		Wait.sync(mDriver, new String[] { "id", "id" }, new String[] {
				"mainGraphHolder", "targetHolder" }, timeout*2);
		return Wait.sync(mDriver, new String[] { "className" },
			new String[] { "gry" }, 5);

	}

	/**
	 * Parses a string out.
	 * 
	 * <pre>
	 * Example
	 * String x = parseString("abcdefghijkl", "c", "gh")
	 * x now contains "def"
	 * 
	 * @param allText
	 * @param before
	 * @param after
	 * @return
	 */
	private String parseString(String allText, String before, String after) {
		int beforePos = allText.toLowerCase(Locale.US).indexOf(before.toLowerCase(Locale.US));
		if (beforePos >= 0) {
			beforePos = beforePos + before.length();
		}
		if (after == null) {
			return allText.substring(beforePos);
		}

		int afterPos = allText.toLowerCase(Locale.US).indexOf(after.toLowerCase(Locale.US));

		if (beforePos < 0 || afterPos < 0){
			return "";
		}
		if (afterPos < beforePos) {
			return allText.substring(beforePos).trim();
		}

		return allText.substring(beforePos, afterPos).trim();
	}

}
