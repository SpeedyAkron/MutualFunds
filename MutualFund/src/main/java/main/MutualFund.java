package main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MutualFund{
	private static final String DEL = ";";
	private String tickerSymbol = "";
	private String name = "";
	private String category = "";
	private String abreviatedCat = "";
	private String expenses = "";
	private String minimumInvestment = "";
	private String turnOverPercent = "";
	private String totalAssets = "";
	private String morningstarRating = "";
	private String oneYearReturn = "";
	private String threeYearReturn = "";
	private String oneYrReturnVsCat = "";
	private String threeYrReturnVsCat = "";
	private String fiveYrReturnVsCat = "";
	private int numberOfManagers = -1;
	private String firstManagerStartDate = "";
	private String lastManagerStartDate = "";
	private String[] returnVsCatYearsAgo= new String[6];//[0]=1 year ago, [5]=6 years ago
	private String rating = "";
	
	MutualFund(){
		for(int i=0; i<returnVsCatYearsAgo.length;i++){
			returnVsCatYearsAgo[i] = "";
		}
	}

	public void setTickerSymbol(final String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setCategoryAndAbreviatedCategory(final String newCategory) {
		String category = newCategory;
		
		category = replaceFundCategories(category);		
		this.category = replaceFundCategories(category);

		if(category.length()<2){
			this.abreviatedCat = category;
		}
		else if(category.substring(0, 2).equalsIgnoreCase("EM")){ 
			this.abreviatedCat = "EM";//Emerging markets gets two letters
		}
		else{
			this.abreviatedCat = category.substring(0, 1);
		}
				
	}

//	public void setAbreviatedCategory(String abreviatedCategory) {
//		this.abreviatedCategory = abreviatedCategory;
//	}

	public void setExpenses(final String expenses) {
		this.expenses = expenses;
	}

	public void setMinimumInvestment(final String newMinInvestment) {
		String minimumInvestment = newMinInvestment; 
		minimumInvestment = minimumInvestment.replace(" ", "");
		this.minimumInvestment = minimumInvestment;
	}

	public void setTurnOverPercent(final String turnOverPercent) {
		this.turnOverPercent = turnOverPercent;
	}

	public void setTotalAssets(String totalAssets) {
		totalAssets = totalAssets.replace("bil", "b");
		totalAssets = totalAssets.replace("Bil", "b");
		totalAssets = totalAssets.replace("mil", "m");
		totalAssets = totalAssets.replace("Mil", "m");
		totalAssets = totalAssets.replace("$ ", "$");
		this.totalAssets = totalAssets;
	}

	public void setMorningstarRating(String morningstarRating) {
		morningstarRating = morningstarRating.replace("r_star", "");
		morningstarRating = morningstarRating.replace("E", "");
		this.morningstarRating = morningstarRating;
	}

	public void setOneYearReturn(final String oneYearReturn) {
		this.oneYearReturn = oneYearReturn;
	}

	public void setThreeYearReturn(final String threeYearReturn) {
		this.threeYearReturn = threeYearReturn;
	}

	public void setOneYearReturnVsCategory(final String oneYearReturnVsCategory) {
		this.oneYrReturnVsCat = oneYearReturnVsCategory;
	}

	public void setThreeYearReturnVsCategory(final String threeYearReturnVsCategory) {
		this.threeYrReturnVsCat = threeYearReturnVsCategory;
	}

	public void setFiveYearReturnVsCategory(final String fiveYearReturnVsCategory) {
		this.fiveYrReturnVsCat = fiveYearReturnVsCategory;
	}

	public void setNumberOfManagers(final int numberOfManagers) {
		this.numberOfManagers = numberOfManagers;
	}

	public void setFirstManagerStartDate(final String firstManagerStartDate) {
		this.firstManagerStartDate = firstManagerStartDate;
	}

	public void setLastManagerStartDate(final String lastManagerStartDate) {
		this.lastManagerStartDate = lastManagerStartDate;
	}

	public void setReturnVsCatYearsAgo(final String returnVsCatSixYearsAgo, final int numYearsAgo) {
		this.returnVsCatYearsAgo[numYearsAgo-1] = returnVsCatSixYearsAgo;
	}

	public void setRating(final int row) {
		//add formulas for sum and average sum at the end of the spreadsheet
		final int x = row; //x is used for a simpler line of code below (represents current row of excel sheet +1)
		long daysOffset = daysSince1900(5);
		
		this.rating = "=IF(MIN(P"+x+":Q"+x+")>"+daysOffset+",-9,(SUM(R"+x+":W"+x+")-MAX(R"+x+":W"+x+")+MIN(R"+x+":W"+x+")-STDEV.S(R"+x+":W"+x+"))/6)";
	}
	
	public String getFundTextExcel(){
		return tickerSymbol + DEL 
				+ name + DEL
				+ category + DEL 
				+ abreviatedCat + DEL
				+ expenses + DEL
				+ minimumInvestment + DEL 
				+ totalAssets + DEL 
				+ turnOverPercent + DEL 
				+ morningstarRating + DEL 
				+ oneYearReturn + DEL 
				+ threeYearReturn + DEL 
				+ oneYrReturnVsCat + DEL 
				+ threeYrReturnVsCat + DEL 
				+ fiveYrReturnVsCat + DEL 
				+ numberOfManagers + DEL 
				+ firstManagerStartDate + DEL 
				+ lastManagerStartDate + DEL 
				+ returnVsCatYearsAgo[5] + DEL 
				+ returnVsCatYearsAgo[4] + DEL 
				+ returnVsCatYearsAgo[3] + DEL 
				+ returnVsCatYearsAgo[2] + DEL 
				+ returnVsCatYearsAgo[1] + DEL 
				+ returnVsCatYearsAgo[0] + DEL 
				+ rating;			
		
	}
	
	public String getHeaders(){
		return "Ticker" + DEL + "Name" + DEL + "Cat" + DEL + "AC" + DEL + "Exp" + DEL + "Min" + DEL + "TO" + DEL + "TA" + DEL + "MR" + DEL + "1Ret" + DEL + "3Ret" + DEL + "1Cat" + DEL + "3Cat" + DEL + "5Cat" + DEL + "#Man" + DEL + "MgtSD" + DEL + "MgtED" + DEL + "C-6" + DEL + "C-5" + DEL + "C-4" + DEL + "C-3" + DEL + "C-2" + DEL + "C-1" + DEL + "Rat";
	}
	
	private long daysSince1900(int numYearsAgo) {	
		
		final Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH)+1; //zero based
		if (month < 2 ){
			numYearsAgo++; //Morningstar doesn't include current year as latest data until Feb
		}
		
		final int year = now.get(Calendar.YEAR) - numYearsAgo;
		
		final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
		Date date = null;
		Date date1900 = null;
		
		try{
			date = format.parse("01/01/" + year);
			date1900 = format.parse("01/01/1900");
		}
		catch(ParseException e){
			e.printStackTrace();
		}
		
		final long difference = (date.getTime() - date1900.getTime()) / 1000 / 60 / 60 / 24;
		return difference + 2; // excel calculates two days off from java
		
	}
	
	/**
	 * Changes funds category to a short name.  First letter is category
	 * S-Small cap
	 * M-Mid cap
	 * L-Large cap
	 * E-Emerging Markets
	 * F-International
	 * Y-Conservative
	 * Z-Specialty
	 * 
	 * @param category - long category from morningstar
	 * @return Shorter more usable fund category
	 */
	private String replaceFundCategories(String category){
		
		//US stock funds (S/M/L)
		category = category.replace("Mid-Cap", "M");
		category = category.replace("Small/Mid", "S/M");
		category = category.replace("Mid", "M");
		category = category.replace("Small", "S");
		category = category.replace("Large", "L");		
		category = category.replace("Blend", "B");
		category = category.replace("Growth", "G");
		category = category.replace("Value", "V");	
				
		
		//Foreign funds (F)
		category = category.replace("Foreign", "F"); //Covers FLB, FLV, FLG, FS/MB, FS/MV, FS/MG		
		category = category.replace("Global", "FGlobal");
		category = category.replace("Diversified Pacific/Asia", "FDiversPfc/Asia");
		category = category.replace("Europe Stock", "FEur");
		category = category.replace("World Stock", "FWld");		
		category = category.replace("Pacific/Asia ex-Japan Stk", "FPfc/AsiaJpn");
		category = category.replace("Japan Stock", "FJapanStock");
		category = category.replace("World Allocation", "FWldAlloc");		
		
		
		//Conservative Funds(Y) - Allocation
		category = category.replace("Intermediate-Term", "YIT");
		category = category.replace("Conservative Allocation", "YConsAlloc");
		category = category.replace("Moderate Allocation", "YModAlloc");		
		category = category.replace("Convertibles", "YConvertibles");
		category = category.replace("Conservative Allocation", "YConservative Allocation");		
		category = category.replace("Moderate Allocation", "YModerate Allocation");
		category = category.replace("World Allocation", "YWorld Allocation");		
		category = category.replace("Aggressive Allocation", "YAggressive Allocation");
		category = category.replace("Tactical Allocation", "YTactical Allocation");
		
		
		//Conservative Funds(Y) - Taxable bond funds
		category = category.replace("Long Government", "YLong Government");
		category = category.replace("Preferred Stock", "YPreferred Stock");
		category = category.replace("Long-Term Bond", "YLong-Term Bond");
		category = category.replace("Corporate Bond", "YCorporate Bond");
		category = category.replace("Intermediate-Term Bond", "YIntermediate-Term Bond");
		category = category.replace("Inflation-Protected Bond", "YInflation-Protected Bond");
		category = category.replace("Intermediate Government", "YIntermediate Government");
		category = category.replace("High Yield Bond", "YHigh Yield Bond");
		category = category.replace("Multisector Bond", "YMultisector Bond");
		category = category.replace("World Bond", "YWorld Bond");
		category = category.replace("Bank Loan", "YBank Loan");
		category = category.replace("Short-Term Bond", "YShort-Term Bond");
		category = category.replace("Short Government", "YShort Government");
		category = category.replace("Nontraditional Bond", "YNontraditional Bond");
		category = category.replace("Ultrashort Bond", "YUltrashort Bond");
		category = category.replace("Emerging Markets Bond", "YEmerging Markets Bond");

		
		//Conservative Funds(Y) - High yield bond funds
		category = category.replace("Target-Date 2051+", "YTarget-Date 2051+");
		category = category.replace("High Yield Muni", "YHigh Yield Muni");
		category = category.replace("Muni California Long", "YMuni California Long");
		category = category.replace("Muni National Long", "YMuni National Long");
		category = category.replace("Muni New Jersey", "YMuni New Jersey");
		category = category.replace("Muni Single State Long", "YMuni Single State Long");
		category = category.replace("Muni New York Long", "YMuni New York Long");
		category = category.replace("Muni Pennsylvania", "YMuni Pennsylvania");
		category = category.replace("Muni Massachusetts", "YMuni Massachusetts");
		category = category.replace("Muni Ohio", "YMuni Ohio");
		category = category.replace("Muni Minnesota", "YMuni Minnesota");
		category = category.replace("Muni California Intermediate", "YMuni California Intermediate");
		category = category.replace("Muni National Interm", "YMuni National Interm");
		category = category.replace("Muni Single State Interm", "YMuni Single State Interm");
		category = category.replace("Muni New York Intermediate", "YMuni New York Intermediate");
		category = category.replace("Muni Single State Short", "YMuni Single State Short");
		category = category.replace("Muni National Short", "YMuni National Short");

		
		//Target Date Funds (T)
		category = category.replace("Target Date 2000-2010", "Target Date 2000-2010");
		category = category.replace("Target Date 2011-2015", "Target Date 2011-2015");
		category = category.replace("Target Date 2016-2020", "Target Date 2016-2020");
		category = category.replace("Target Date 2021-2025", "Target Date 2021-2025");		
		category = category.replace("Target Date 2026-2030", "Target Date 2026-2030");
		category = category.replace("Target Date 2031-2035", "Target Date 2031-2035");		
		category = category.replace("Target Date 2036-2040", "Target Date 2036-2040");
		category = category.replace("Target Date 2041-2045", "Target Date 2041-2045");		
		category = category.replace("Target Date 2046-2050", "Target Date 2046-2050");	
		category = category.replace("Target Date 2051-2055", "Target Date 2051-2055");		
		category = category.replace("Target Date 2056-2060", "Target Date 2056-2060");	
		category = category.replace("Retirement Income", "TRetirement Income");
		
		
		//Alternative Funds (A)
		category = category.replace("Trading-Leveraged Debt", "ATrading-Leveraged Debt");
		category = category.replace("Trading-Miscellaneous", "ATrading-Miscellaneous");
		category = category.replace("Bear Market", "ABear Market");
		category = category.replace("Trading-Inverse Equity", "ATrading-Inverse Equity");
		category = category.replace("Trading-Leveraged Commodities", "ATrading-Leveraged Commodities");
		category = category.replace("Market Neutral", "AMarket Neutral");
		category = category.replace("Long/Short Equity", "ALong/Short Equity");
		category = category.replace("Multialternative", "AMultialternative");
		category = category.replace("Multicurrency", "AMulticurrency");
		category = category.replace("Managed Futures", "AManaged Futures");
		category = category.replace("Trading-Leveraged Equity", "ATrading-Leveraged Equity");
		category = category.replace("Trading-Inverse Debt", "ATrading-Inverse Debt");
		category = category.replace("Trading-Inverse Commodities", "ATrading-Inverse Commodities");
		category = category.replace("Nontraditional Bond", "ANontraditional Bond");
		category = category.replace("Ultrashort Bond", "AUltrashort Bond");
		category = category.replace("Emerging Markets Bond", "AEmerging Markets Bond");
		
		
		//Emerging Market Funds (EM)
		category = category.replace("Diversified Emerging Mkts", "EMDivEM");
		category = category.replace("India Equity", "EMIndiaEquity");
		category = category.replace("Emerging Markets", "EM");
		category = category.replace("Latin America Stock", "EMLatin America");
		category = category.replace("China Region", "EMChinaRgn");
		category = category.replace("Miscellaneous Region", "EMMiscRegion");
		
		
		//Specialty Funds (Z)
		category = category.replace("Real Estate", "ZRealEst");
		category = category.replace("Commodities Broad Basket", "ZCommodBB");
		category = category.replace("Commodities Precious Metals", "ZCommodPrecMet");
		category = category.replace("Health", "ZHealth");
		category = category.replace("Natural Resources", "ZNat Res");		
		category = category.replace("Technology", "ZTech");
		category = category.replace("Communications", "ZCommunic");
		category = category.replace("Financial", "ZFinancial");
		category = category.replace("Industrials", "ZIndustrials");
		category = category.replace("Equity Precious Metals", "ZEquityPrecMtls");
		category = category.replace("Utilities", "ZUtil");
		category = category.replace("Miscellaneous Sector", "ZMiscSector");
		category = category.replace("Equity Energy", "ZEquityEngy");
		category = category.replace("Consumer Defensive", "ZConsDefensive");
		category = category.replace("Consumer Cyclical", "ZConsCyclical");
		
		
		category = category.replace(" ", "");
		return category;
	}

}


