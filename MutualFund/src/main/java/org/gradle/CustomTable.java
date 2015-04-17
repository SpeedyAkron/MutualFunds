package org.gradle;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

/**
 * Provides a number of useful table functions. The function in this class assume the following. Be careful with merged cells to make sure
 * you are retrieving the correct cell/List of cells. None of the functions take into account merged cells or merged header cells. If the
 * HTML table is missing or contains extra tags the functions in this class may not behave correctly. Rows and columns start numbering at 0.
 * 
 * @author Geoffrey Bergmann
 * @date 10/23/2013
 */
public class CustomTable {
	private WebElement mTable;
	private char mDelimitor;
	private List<WebElement> mAllRows;

	public CustomTable(WebElement sampleTable) {
		this(sampleTable, '~');
	}

	/**
	 * Creates a new CustomTable object.
	 * 
	 * @param sampleTable
	 *            The table to use
	 * @param defaultDelimitor
	 *            The delimiter to use for returning row text, or header columns. Used in getColumnHeadersText, getColumnText, and
	 *            getRowText.
	 */
	public CustomTable(WebElement sampleTable, char defaultDelimitor) {
		if (!sampleTable.getTagName().equalsIgnoreCase("table")) {
			throw new UnexpectedTagNameException("table", sampleTable.getTagName());
		}
		mTable = sampleTable;
		mDelimitor = defaultDelimitor;
		setAllRows();
	}

	/**
	 * 
	 * @return The current table
	 */
	public WebElement getTable() {
		return mTable;
	}

	/**
	 * @return The current delimiter
	 */
	public char getDelimitor() {
		return mDelimitor;
	}

	/**
	 * 
	 * @param newDefaultDelimitor
	 *            - New delimiter to be used returning row text, or header columns. Used in getColumnHeadersText, getColumnText, and
	 *            getRowText.
	 */
	public void setDelimitor(char newDefaultDelimitor) {
		mDelimitor = newDefaultDelimitor;
	}

	/**
	 * Resets the table object. Should be called after a page refresh.
	 * 
	 * @param newTable
	 *            Table to bind with object. Should have "table" tag.
	 * 
	 * @throws {@link UnexpectedTagNameException} if newTable does not have "table" tag.
	 */
	public void setTable(WebElement newTable) {
		if (!newTable.getTagName().equalsIgnoreCase("table")) {
			throw new UnexpectedTagNameException("table", newTable.getTagName());
		}
		mTable = newTable;
		setAllRows();
	}

	/**
	 * @return - Returns the number of rows in the table.
	 */
	public int getRowCount() {
		return mAllRows.size();
	}

	/**
	 * @return The number of columns in the first row of the table.
	 * @throws {@link NoSuchElementException} if table does not contain any rows
	 */
	public int getColumnCount() {
		return getColumnCount(0);
	}

	/**
	 * @return The # of columns in the row with the most columns in the table. -1 if there are no rows
	 */
	public int getColumnCountMax() {
		int max = -1;
		for (WebElement row : mAllRows) {
			int currentRowSize = row.findElements(By.tagName(rowTagType(row))).size();
			if (currentRowSize > max) {
				max = currentRowSize;
			}
		}
		return max;
	}

	/**
	 * @return The # of columns in the row with the fewest columns in the table. -1 if there are no rows
	 */
	public int getColumnCountMin() {
		int min = -1;
		for (WebElement row : mAllRows) {
			int currentRowSize = row.findElements(By.tagName(rowTagType(row))).size();
			if (currentRowSize < min || min == -1) {
				min = currentRowSize;
			}
		}
		return min;
	}

	/**
	 * @param row
	 *            Row to count columns
	 * @return The # of columns in row
	 * @throws {@link NoSuchElementException} if row > # rows in table
	 */
	public int getColumnCount(int row) {
		return getRow(row).size();
	}

	/**
	 * Returns column number of first matching cell in header. Starts counting at 0. Returns -1 if column not found. Columns are numbered
	 * starting at 0.
	 * 
	 * @param columnCellText
	 *            - Text to search for. Is not case sensitive.
	 * @return The # of matching header column. Returns -1 if the column is not found.
	 * @throws {@link NoSuchElementException} if there are no column headers.
	 */
	public int getColumnNumber(String columnCellText) {
		String[] allColumns = getColumnHeadersText().split(Character.toString(mDelimitor));
		for (int i = 0; i < allColumns.length; i++)
			if (allColumns[i].equalsIgnoreCase(columnCellText))
				return i;
		return -1;
	}

	/**
	 * Returns the first row in column columnNumber which contains text. Search is not case sensitive. Returns -1 if not found. Rows are
	 * numbered starting at 0.
	 * 
	 * @param text
	 *            Text to look for. Is not case sensitive.
	 * @param columnNumber
	 *            - column to look in
	 * @param startFromRow
	 *            - Starting row to start search in (optional)
	 * @return First row number where text is found.
	 */
	public int getRowWithCellText(String text, int columnNumber, int startFromRow) {

		verifyColumnIsValid(columnNumber);
		verifyRowIsValid(startFromRow);

		for (int i = startFromRow; i < getRowCount(); i++) {
			if (getCellText(i, columnNumber).toLowerCase().contains(text.toLowerCase())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the first row # with text in it. Returns -1 if not found. Text must occur all within a given cell. Is not case sensitive.
	 * Rows are numbered starting at 0.
	 * 
	 * @param text
	 *            text to look for
	 * @param columnNumber
	 *            column to # to search within (optional)
	 * @return row number with text in it. -1 if not found.
	 * @throws {@link NoSuchElementException} if columnNumber does not exist in the table
	 */
	public int getRowWithCellText(String text, int columnNumber) {
		verifyColumnIsValid(columnNumber);
		return getRowWithCellText(text, columnNumber, 0);
	}

	/**
	 * Returns the first row # with text in it. Returns -1 if not found. Text must occur all within a given cell. Is not case sensitive.
	 * Rows are numbered starting at 0.
	 * 
	 * @param text
	 *            text to look for
	 * @return row number with text in it. -1 if not found.
	 */
	public int getRowWithCellText(String text) {
		for (int i = 0; i < mAllRows.size(); i++) {
			WebElement row = mAllRows.get(i);
			for (WebElement cell : row.findElements(By.tagName(rowTagType(row)))) {
				if (cell.getText().toLowerCase().contains(text.toLowerCase())) {
					return i;
				}
			}

		}

		return -1;
	}

	/**
	 * 
	 * @return The column headers separated by the delimiter
	 * @throws {@link NoSuchElementException} if there are no column headers.
	 */
	public String getColumnHeadersText() {

		WebElement headerRow = mTable.findElement(By.tagName("thead")).findElement(By.tagName("tr"));
		String columnHeaders = "";

		for (WebElement columnCell : headerRow.findElements(By.tagName("th"))) {
			columnHeaders += columnCell.getText() + mDelimitor;
		}
		if (columnHeaders.length() > 0) {
			columnHeaders = columnHeaders.substring(0, columnHeaders.length() - 1);
		}

		return columnHeaders;
	}

	/**
	 * Table must have exactly one header row All table rows must be the same length
	 * 
	 * @param rowNumber
	 * @param columnNumber
	 * @return
	 * @throws {@link NoSuchElementException} if column columnNumber does not exist in row rowNumber
	 */
	public String getCellText(int rowNumber, int columnNumber) {
		verifyColumnIsValid(rowNumber, columnNumber);
		WebElement row = mAllRows.get(rowNumber);
		return row.findElements(By.tagName(rowTagType(row))).get(columnNumber).getText();

	}

	/**
	 * Returns the text of the column
	 * 
	 * @param columnNumber
	 *            - column to return text of
	 * @return - Text of column
	 * @throws {@link NoSuchElementException} if columnNumber does not exist in the table
	 */
	public String getColumnText(int columnNumber) {
		verifyColumnIsValid(columnNumber);
		String colText = "";

		for (int i = 0; i < getRowCount(); i++) {
			colText += getCellText(i, columnNumber) + mDelimitor;
		}
		if (colText.length() > 0) {
			colText = colText.substring(0, colText.length() - 1);
		}

		return colText;
	}
	
	public String getColumnText(int columnNumber, int startRow) {
		verifyColumnIsValid(columnNumber);
		String colText = "";

		for (int i = startRow-1; i < getRowCount(); i++) {
			colText += getCellText(i, columnNumber) + mDelimitor;
		}
		if (colText.length() > 0) {
			colText = colText.substring(0, colText.length() - 1);
		}

		return colText;
	}

	public String getColumnText(int columnNumber, int startRow, int endRow) {
		verifyColumnIsValid(columnNumber);
		String colText = "";

		for (int i = startRow; i < endRow+1; i++) {
			colText += getCellText(i, columnNumber) + mDelimitor;
		}
		if (colText.length() > 0) {
			colText = colText.substring(0, colText.length() - 1);
		}

		return colText;
	}

	/**
	 * Returns the text of the column with header columnHeader
	 * 
	 * @param columnHeader
	 *            - Must be a header column. Is not case sensitive.
	 * @return - Text of column
	 * @throws {@link NoSuchElementException} if there are no column headers.
	 * 
	 */
	public String getColumnText(String columnHeader) {
		return getColumnText(getColumnNumber(columnHeader));
	}

	/**
	 * 
	 * @param rowNumber
	 *            - Row # to get
	 * @return - Returns all the text in rowNum
	 * 
	 * @throws NoSuchElementException
	 *             if rowNum > rows in table
	 */
	public String getRowText(int rowNumber) {
		verifyRowIsValid(rowNumber);

		String rowText = "";
		List<WebElement> cells = getRow(rowNumber);
		for (WebElement cell : cells) {
			rowText += cell.getText() + mDelimitor;
		}
		if (rowText.length() > 0)
			rowText = rowText.substring(0, rowText.length() - 1);
		return rowText;
	}

	/**
	 * Returns text value of table on the screen.
	 */
	@Override
	public String toString() {
		return getText();
	}

	/**
	 * Useful for debugging. Debugs just like a normal WebElement
	 * 
	 * @return table.getText();
	 */
	public String getText() {
		return mTable.getText();
	}

	/**
	 * 
	 * @return a List containing all table rows as WebElements. A list of tr WebElements
	 */
	public List<WebElement> getAllTableRows() {
		return mAllRows;
	}

	/**
	 * Gets all the td and th elements in a column into a list of WebElements in order.
	 * 
	 * @param columnNumber
	 * @return list containing WebElements (td and/or th) representing cells in column columnNumber
	 * @throws {@link NoSuchElementException} if columnNumber does not exist in the table
	 */
	public List<WebElement> getColumn(int columnNumber) {
		verifyColumnIsValid(columnNumber);

		List<WebElement> allTableRowsOfColNum = new ArrayList<WebElement>();
		for (WebElement tableRow : mAllRows) {
			allTableRowsOfColNum.add(tableRow.findElements(By.tagName("td")).get(columnNumber));
		}

		return allTableRowsOfColNum;
	}

	/**
	 * Gets all the td and th elements in a column into a list of WebElements in order.
	 * 
	 * @param columnHeader
	 *            - The exact text of the column looking for. Ignores case sensitivity.
	 * @return list containing WebElements (td and/or th) representing cells in column columnNumber
	 * @throws {@link NoSuchElementException} if no header row exists
	 */
	public List<WebElement> getColumn(String columnHeader) {
		return getColumn(getColumnNumber(columnHeader));
	}

	/**
	 * Returns a list of WebElements containing all the cells in a given row.
	 * 
	 * @param rowNumber
	 * @return list containing WebElements (td or th) representing all cells in row rowNumber
	 * @throws {@link NoSuchElementException} if row rowNumber does not exist in the table
	 */
	public List<WebElement> getRow(int rowNumber) {
		verifyRowIsValid(rowNumber);

		WebElement tableRow = getAllTableRows().get(rowNumber);
		return tableRow.findElements(By.tagName(rowTagType(tableRow)));
	}

	/**
	 * @param row
	 *            - Row of table. Should be a tr tag element
	 * @return - Returns "th" if this is a table header row or "td" if this is a table body row
	 */
	private String rowTagType(WebElement row) {
		if (!row.getTagName().equalsIgnoreCase("tr")) {
			throw new IllegalArgumentException("Table row passed is not a valid table row");
		}
		if (row.findElements(By.tagName("td")).size() > 0)
			return "td";
		else if (row.findElements(By.tagName("th")).size() > 0)
			return "th";
		else
			return "td";
	}

	/**
	 * Resets the mAllRows variable when the table is refreshed.
	 */
	private void setAllRows() {
		List<WebElement> allTableRows = new ArrayList<WebElement>();
		try {
			allTableRows.addAll(mTable.findElement(By.tagName("thead")).findElements(By.tagName("tr")));
		} catch (NoSuchElementException e) {
		}

		try {
			allTableRows.addAll(mTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")));
		} catch (NoSuchElementException e) {
		}

		mAllRows = allTableRows;
	}

	/**
	 * @throws {@link NoSuchElementException} if col columnNumber does not exist in the table
	 */
	private void verifyColumnIsValid(int columnNumber) {
		if (columnNumber > getColumnCountMax()) {
			throw new NoSuchElementException("Column (" + columnNumber + ") doesn't exist is a table with only (" + getColumnCountMax()
					+ ") columns");
		}

	}

	/**
	 * @throws {@link NoSuchElementException} if column columnNumber does not exist in row rowNumber
	 */
	private void verifyColumnIsValid(int rowNumber, int columnNumber) {
		if (columnNumber > getColumnCount(rowNumber)) {
			throw new NoSuchElementException("Column (" + columnNumber + ") doesn't exist is a table with only ("
					+ getColumnCount(rowNumber) + ") columns");
		}

	}

	/**
	 * @throws {@link NoSuchElementException} if row rowNumber does not exist in the table
	 */
	private void verifyRowIsValid(int rowNumber) {
		if (rowNumber >= getRowCount()) {
			throw new NoSuchElementException("Row (" + rowNumber + ") doesn't exist is a table with only (" + getRowCount() + ") rows");
		}
	}
}
