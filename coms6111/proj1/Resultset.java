package coms6111.proj1;

import java.io.*;
import java.util.*;

import javax.xml.xpath.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;

/**
 * A Resultset from search engine
 */
public class Resultset {
	private static Log log = LogFactory.getLog(Resultset.class);
	
	private Query myQuery;
	private List<Result> myResults;
	private int resultSize;
	
	/**
	 * Constructor using input stream returned from HttpClient
	 */
	public Resultset(InputStream rstream) throws Exception {
		Document response = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rstream);
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xPath=factory.newXPath();

		//Get all search Result nodes
		NodeList nodes = (NodeList)xPath.evaluate("/ResultSet/Result", response, XPathConstants.NODESET);
		int nodeCount = nodes.getLength();
        
		myResults = new ArrayList<Result>();
		//iterate over search Result nodes
		for (int i = 0; i < nodeCount; i++) {
			//Get each xpath expression as a string
			String title = (String)xPath.evaluate("Title", nodes.item(i), XPathConstants.STRING);
			String summary = (String)xPath.evaluate("Summary", nodes.item(i), XPathConstants.STRING);
			String url = (String)xPath.evaluate("Url", nodes.item(i), XPathConstants.STRING);
			//print out the Title, Summary, and URL for each search result
			log.trace("Title: " + title);
			log.trace("Summary: " + summary);
			log.trace("URL: " + url);
			log.trace("--");
			
			myResults.add(new Result(title, summary, url));
		}
	}
	
	public Iterator<Result> getIterator() {
		return myResults.iterator();
	}
	public ListIterator<Result> getListIterator() {
		return myResults.listIterator();
	}
}
