package coms6111.proj1;

import java.util.Scanner;

public class Runner {
	
	private static Query userQuery;
	private static Resultset resultset;
	private static QueryExpander queryExpander;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// Get input query from user
		System.out.println("Hi please enter a query:");
		Scanner in = new Scanner(System.in);
		userQuery = new Query(in.nextLine());
		
		// FIXME set queryExpander to an instance of a class
		queryExpander = null;
		
		// Do query processing
		resultset = processQuery(userQuery, queryExpander);
		
		// Display results
		if (resultset != null) {
			System.out.println("Cool, you got results");
		} else {
			System.err.println("Error generating resultset");
		}
		
		
	}
	
	/**
	 * Begin the whole process:
	 * 
	 * 1. Perform the top-10 query using current search terms
	 * 2. Present results and let user mark which ones are relevant
	 * 3. Expand the query with limited number of new words (no deletions!)
	 * 
	 * Repeat until the fraction of relevant pages is high enough.
	 */
	public static Resultset processQuery(Query query, QueryExpander queryExpander) {
		return null;
	}

}
