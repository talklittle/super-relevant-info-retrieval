package coms6111.proj1;

public class Query {
	private String myQueryString;
	private int iteration; // how many times have I been expanded
	
	public Query(String queryString) {
		myQueryString = queryString;
	}
	
	public String getString() {
		return myQueryString;
	}
	
	public void setString(String queryString) {
		myQueryString = queryString;
	}
}
