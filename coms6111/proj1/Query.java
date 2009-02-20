package coms6111.proj1;

import java.io.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

public class Query {
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
	
	/**
	 * Apply a query expansion to the current query
	 * Return a new expanded query
	 * 
	 * @param qe An implementation of a QueryExpander
	 * @return Newly expanded query
	 */
	public Query applyExpansion(QueryExpander qe) {
		String newQueryString = myQueryString;
		
		// TODO apply qe
		
		return new Query(newQueryString, myIteration + 1);
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
	        	System.err.println("Method failed: " + method.getStatusLine());
	        	return null;
	        }
	    } catch (IOException e) {
	    	System.err.println(e.getLocalizedMessage());
	    	return null;
	    }

	    InputStream rstream = null;
	    
	    // Get the response body
	    try {
	    	rstream = method.getResponseBodyAsStream();
	    } catch (IOException e) {
	    	System.err.println(e.getLocalizedMessage());
	    	return null;
	    }
	    
	    /********/
	    /* XXX DEBUG */
        // Process the response from Yahoo! Web Services
        BufferedReader br = new BufferedReader(new InputStreamReader(rstream));
        String line;
        try {
	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	        }
	        br.close();
        } catch (IOException e) {
        	System.err.println(e.getLocalizedMessage());
        }
        /*******/

	    return new Resultset(rstream);
	}

	public String getString() {
		return myQueryString;
	}
	
	public void setString(String queryString) {
		myQueryString = queryString;
	}
}
