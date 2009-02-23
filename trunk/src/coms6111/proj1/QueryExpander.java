package coms6111.proj1;

/**
 * Interface for QueryExpanders
 * A QueryExpander is a class that expands a Query by looking at the relevant Resultset
 */
public interface QueryExpander {
	public Query apply(Query query, Resultset relevantResultset);
}
