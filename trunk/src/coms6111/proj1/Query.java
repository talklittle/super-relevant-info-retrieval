package coms6111.proj1;

import java.io.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Query {
	private static Log log = LogFactory.getLog(Query.class);
	
	public final String appid = "8ZdPHgrV34GqaKRLc2FEMULwfYT9_rn1xE0swnm6JcfB_IflTBEdzba7HAuPJcDwGA--";
	public final int numResults = 10;
	
	private String myQueryString;
	private int myIteration; // how many times have I been expanded
	
	public Query(String queryString) {
		myQueryString = queryString;
		myIteration = 0;
	}
	public Query(String queryString, int iteration) {
		myQueryString = queryString;
		myIteration = iteration;
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

	public String toString() {
		return "[" + myQueryString + "]";
	}
	
	public int getIteration() {
		return myIteration;
	}
	
	public void setString(String queryString) {
		myQueryString = queryString;
	}
	
	public void setIteration(int iteration) {
		myIteration = iteration;
	}
}
