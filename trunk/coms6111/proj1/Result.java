package coms6111.proj1;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents one result as returned by search engine
 */
public class Result {
	private static Log log = LogFactory.getLog(Result.class);
	
	public String title, summary;
	public URL url;
	
	public Result(String theTitle, String theSummary, String theUrl) {
		title = theTitle;
		summary = theSummary;
		try {
			url = new URL(theUrl);
		} catch (MalformedURLException e) {
			log.warn(e);
			url = null;
		}
	}
}
