package org.gradle;

import java.util.Date;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public class WebElements {
	
	/**
	 * Blocks until the specified WebElement is displayed.
	 * @param element element which should be timed for display
	 * @param elementName Name of the element to use in reporting
	 * @return Human readable representation of how long it took
	 */
	private static final String LINE_BREAK = "****************************************************";
	public static String timeUntilIsDisplayed(WebElement element, String elementName) {
		Date start = new Date();
		while (!element.isDisplayed()) {
			// Do absolutely nothing, just wait
		}
		Date end = new Date();
		
		long diff = end.getTime() - start.getTime();
		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;      
        
        return "Time to display WebElement " + elementName + ": " + diffMinutes + " Minutes, " + diffSeconds + " Second.";
	}
	
	/**
	 * Checks if the object exists on the screen or not. <p>  
	 * 
	 * @param element
	 * @return <tt>true</tt> if element exists and <tt>false</tt> if it does not
	 */
	public static boolean exist(WebElement element){
		try{
			return element.isDisplayed();			
		}
		catch (NoSuchElementException exception){
			return false;
		}
		catch(TimeoutException e){
			return false;
		}
	}
	
	
	/**
	 * Prints the most useful properties of a WebElement.  This is meant to be used for debugging only.  
	 * @author Geoffrey Bergmann
	 * @param w - WebElement to print properties of
	 */
	public static void printPropertiesShort(WebElement w) {
		System.out.println(LINE_BREAK);
		System.out.println("*************************" + w.getText() + "***********************");
		System.out.println(LINE_BREAK);
		
		System.out.println("getTagName = " + w.getTagName());
		System.out.println("getText = " + w.getText());
		System.out.println("getClass = " + w.getClass());
		System.out.println("getLocation = " + w.getLocation());
		System.out.println("getSize = " + w.getSize());
		System.out.println("isDisplayed = " + w.isDisplayed());
		System.out.println("isEnabled = " + w.isEnabled());
		System.out.println("isSelected = " + w.isSelected());
		
		System.out.println("getAttribute(checked) = " + w.getAttribute("checked"));
		System.out.println("getAttribute(class) = " + w.getAttribute("class"));		
		System.out.println("getAttribute(disabled) = " + w.getAttribute("disabled"));
		System.out.println("getAttribute(hidden) = " + w.getAttribute("hidden"));
		System.out.println("getAttribute(readonly) = " + w.getAttribute("readonly"));
		System.out.println("getAttribute(required) = " + w.getAttribute("required"));
		System.out.println("getAttribute(selected) = " + w.getAttribute("selected"));
	
		System.out.println(LINE_BREAK);
		System.out.println(LINE_BREAK);
	}
	
	/**
	 * Prints all the properties of a WebElement.  This is meant to be used for debugging only.  
	 * @author Geoffrey Bergmann
	 * @param w - WebElement to print properties of
	 */
	public static void printAllProperties(WebElement w) {
		System.out.println(LINE_BREAK);
		System.out.println("*************************" + w.getText() + "***********************");
		System.out.println(LINE_BREAK);
		
		System.out.println("getTagName = " + w.getTagName());
		System.out.println("getText = " + w.getText());
		System.out.println("getClass = " + w.getClass());
		System.out.println("getLocation = " + w.getLocation());
		System.out.println("getSize = " + w.getSize());
		System.out.println("isDisplayed = " + w.isDisplayed());
		System.out.println("isEnabled = " + w.isEnabled());
		System.out.println("isSelected = " + w.isSelected());
		
		System.out.println("getAttribute(async) = " + w.getAttribute("async"));
		System.out.println("getAttribute(autofocus) = " + w.getAttribute("autofocus"));
		System.out.println("getAttribute(autoplay) = " + w.getAttribute("autoplay"));
		System.out.println("getAttribute(checked) = " + w.getAttribute("checked"));
		System.out.println("getAttribute(class) = " + w.getAttribute("class"));
		System.out.println("getAttribute(compact) = " + w.getAttribute("compact"));
		System.out.println("getAttribute(complete) = " + w.getAttribute("complete"));
		System.out.println("getAttribute(controls) = " + w.getAttribute("controls"));
		System.out.println("getAttribute(declare) = " + w.getAttribute("declare"));
		System.out.println("getAttribute(defaultchecked) = " + w.getAttribute("defaultchecked"));
		System.out.println("getAttribute(defaultselected) = " + w.getAttribute("defaultselected"));
		System.out.println("getAttribute(defer) = " + w.getAttribute("selecdeferted"));
		System.out.println("getAttribute(disabled) = " + w.getAttribute("disabled"));
		System.out.println("getAttribute(draggable) = " + w.getAttribute("draggable"));
		System.out.println("getAttribute(ended) = " + w.getAttribute("ended"));
		System.out.println("getAttribute(formnovalidate) = " + w.getAttribute("formnovalidate"));
		System.out.println("getAttribute(hidden) = " + w.getAttribute("hidden"));
		System.out.println("getAttribute(indeterminate) = " + w.getAttribute("indeterminate"));
		System.out.println("getAttribute(iscontenteditable) = " + w.getAttribute("iscontenteditable"));
		System.out.println("getAttribute(ismap) = " + w.getAttribute("ismap"));
		System.out.println("getAttribute(itemscope) = " + w.getAttribute("itemscope"));
		System.out.println("getAttribute(loop) = " + w.getAttribute("loop"));
		System.out.println("getAttribute(multiple) = " + w.getAttribute("multiple"));
		System.out.println("getAttribute(nohref) = " + w.getAttribute("nohref"));
		System.out.println("getAttribute(noresize) = " + w.getAttribute("noresize"));
		System.out.println("getAttribute(noshade) = " + w.getAttribute("noshade"));
		System.out.println("getAttribute(novalidate) = " + w.getAttribute("novalidate"));
		System.out.println("getAttribute(nowrap) = " + w.getAttribute("nowrap"));
		System.out.println("getAttribute(open) = " + w.getAttribute("open"));
		System.out.println("getAttribute(paused) = " + w.getAttribute("paused"));
		System.out.println("getAttribute(pubdate) = " + w.getAttribute("pubdate"));
		System.out.println("getAttribute(readonly) = " + w.getAttribute("readonly"));
		System.out.println("getAttribute(required) = " + w.getAttribute("required"));
		System.out.println("getAttribute(reversed) = " + w.getAttribute("reversed"));
		System.out.println("getAttribute(scoped) = " + w.getAttribute("scoped"));
		System.out.println("getAttribute(seamless) = " + w.getAttribute("seamless"));
		System.out.println("getAttribute(seeking) = " + w.getAttribute("seeking"));
		System.out.println("getAttribute(selected) = " + w.getAttribute("selected"));
		System.out.println("getAttribute(spellcheck) = " + w.getAttribute("spellcheck"));
		System.out.println("getAttribute(truespeed) = " + w.getAttribute("truespeed"));
		System.out.println("getAttribute(willvalidate) = " + w.getAttribute("willvalidate"));
		System.out.println("getAttribute(style) = " + w.getAttribute("style"));
	
		System.out.println(LINE_BREAK);
		System.out.println(LINE_BREAK);
	}
	
}
