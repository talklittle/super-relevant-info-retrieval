package coms6111.proj1;

import java.io.*;
import java.util.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;

public class Query {
	private static Log log = LogFactory.getLog(Query.class);
	
	public final String appid = "8ZdPHgrV34GqaKRLc2FEMULwfYT9_rn1xE0swnm6JcfB_IflTBEdzba7HAuPJcDwGA--";
	public final int numResults = 10;
	
	private String myQueryString;
	private List<String> myQueryStringList; // query string broken into individual terms
	private int myIteration; // how many times have I been expanded
	
	public Query(String queryString) {
		this(queryString, 0);
	}
	public Query(String queryString, int iteration) {
		myIteration = iteration;
		this.setString(queryString);
	}
	public Query(Term[] termsInOrder, int iteration) {
		myQueryString = "";
		for (Term t : termsInOrder) {
			myQueryString += (t.text() + " ");
		}
		myQueryString = myQueryString.trim();
		myIteration = iteration;
		this.setString(myQueryString);
	}
	
	public Resultset execute() {
		String request = "http://api.search.yahoo.com/WebSearchService/V1/webSearch";
	    HttpClient client = new HttpClient();

	    PostMethod method = new PostMethod(request);

	    // Add POST parameters
	    method.addParameter("appid", appid);
	    method.addParameter("query", myQueryString);
	    method.addParameter("results", ""+numResults);

	    // Send POST request
	    try {
	    	int statusCode = client.executeMethod(method);
	        if (statusCode != HttpStatus.SC_OK) {
	        	log.error("Method failed: " + method.getStatusLine());
	        	return null;
	        }
	    } catch (IOException e) {
	    	log.error(e.getLocalizedMessage());
	    	return null;
	    }

	    InputStream rstream = null;
	    
	    // Get the response body
	    try {
	    	rstream = method.getResponseBodyAsStream();
	    } catch (IOException e) {
	    	log.error(e.getLocalizedMessage());
	    	return null;
	    }

	    try {
	    	return new Resultset(rstream);
	    } catch (Exception e) {
	    	log.warn("Error creating Resultset from result stream");
	    	return null;
	    }
	}

	/**
	 * Return the queryString enclosed by square brackets.
	 */
	public String toString() {
		return "[" + myQueryString + "]";
	}
	
	/**
	 * Return the number of times the Query has been expanded.
	 * @return The number of times the Query has been expanded
	 */
	public int getIteration() {
		return myIteration;
	}
	
	/**
	 * Set the string of the query.
	 * @param queryString The String of the Query
	 */
	public void setString(String queryString) {
		StringTokenizer st = new StringTokenizer(queryString);
		myQueryStringList = new ArrayList<String>();
		while (st.hasMoreTokens())
			myQueryStringList.add(st.nextToken());
		myQueryString = queryString;
	}
	
	/**
	 * Set the number of times this Query has been expanded.
	 * @param iteration The number of times this Query has been expanded
	 */
	public void setIteration(int iteration) {
		myIteration = iteration;
	}
	
	/**
	 * Return an Iterator<String> to inspect each query term separately.
	 * @return Iterator<String>
	 */
	public Iterator<String> iterator() {
		return myQueryStringList.iterator();
	}
	
	/**
	 * Return the number of terms in the query
	 * @return int of number of terms in the query
	 */
	public int length() {
		return myQueryStringList.size();
	}
}
