package coms6111.proj1;

public interface QueryExpander {
	public Query apply(Query query, Resultset relevantResultset);
}
