package coms6111.proj1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class TermFreqQueryExpander implements QueryExpander {

	private static Log log = LogFactory.getLog(TermFreqQueryExpander.class);
	
	@Override
	public Query apply(Query query, Resultset relevantResultset,
			Resultset nonrelevantResultset) {
		
		Query returnMe;
		Directory relevantIndex, nonrelevantIndex;
		relevantIndex = createIndex(relevantResultset);
		nonrelevantIndex = createIndex(nonrelevantResultset);
		
		Term[] top2Terms =
			selectTerms(relevantIndex, nonrelevantIndex);
		returnMe = addTermsToQuery(query, top2Terms);
		
		try {
			relevantIndex.close();
			nonrelevantIndex.close();
		} catch (IOException e) {
			log.error("Error closing Directory", e);
		}
		
	    return null;
	}
	
	private Directory createIndex(Resultset resultset) {
		Directory directory;
		Analyzer analyzer = new StandardAnalyzer();
		
		try {
		    // Store the index in memory:
		    directory = new RAMDirectory();
		    IndexWriter iwriter = new IndexWriter(directory, analyzer,
		    		IndexWriter.MaxFieldLength.UNLIMITED);
		    iwriter.setMaxFieldLength(25000);
		    Document doc = new Document();
		    String text = "This is the text to be indexed.";
		    doc.add(new Field("fieldname", text, Field.Store.YES,
		        Field.Index.ANALYZED));
		    iwriter.addDocument(doc);
		    iwriter.optimize();
		    iwriter.close();

		    return directory;

		} catch (IOException e) {
			log.error("Oh No", e);
			return null;
		}
	}
	
	private Query addTermsToQuery(Query query, Term[] terms,
			Directory relevantIndex) {
		IndexReader ireader;
		
		try {
			ireader = IndexReader.open(relevantIndex);
			TermPositions tp = ireader.termPositions);
			
		} catch (IOException e) {
			log.error(e);
		}
	}
	
	private Term[] selectTerms(Query query,
			Directory relevantIndex,
			Directory nonrelevantIndex) {
		
		Term[] top2Terms = new Term[2];
	    
		TermFreqQEVector[] relevantTfqeVectors =
			computeTermFrequencies(relevantIndex);
		TermFreqQEVector[] nonrelevantTfqeVectors =
			computeTermFrequencies(nonrelevantIndex);
	    
		TermFreqQEVector[] combinedTfqeVectors
			= combineTfqeVectors(relevantTfqeVectors, nonrelevantTfqeVectors);
		
		Arrays.sort(combinedTfqeVectors);
		int topNthTerm = 0;
		boolean alreadyInQuery;
		Iterator<String> queryIterator;
		// Go through the sorted list of relevant terms to find top 2
		for (int i = 0; i < combinedTfqeVectors.length; i++) {
			queryIterator = query.iterator();
			alreadyInQuery = false;
			// Exclude terms that are already in old query
			while (queryIterator.hasNext()) {
				String queryTerm = queryIterator.next();
				if (queryTerm.equals(combinedTfqeVectors[i].term.text())) {
					alreadyInQuery = true;
					break;
				}
			}
			if (alreadyInQuery)
				continue;
			top2Terms[topNthTerm++] = combinedTfqeVectors[i].term;
			if (topNthTerm >= 2)
				break;
		}
		
		return top2Terms;
	}
	
	private TermFreqQEVector[] combineTfqeVectors(
			TermFreqQEVector[] relevantTfqeVectors,
			TermFreqQEVector[] nonrelevantTfqeVectors) {
		
		for (int i = 0; i < nonrelevantTfqeVectors.length; i++) {
			nonrelevantTfqeVectors[i].freq1=(-1)*nonrelevantTfqeVectors[i].freq1;
		}
		
		for (int i = 0; i< relevantTfqeVectors.length; i++) {
			for (int j=0 ; j< nonrelevantTfqeVectors.length; j++) {
				// decrease term freq by # of times it appears in nonrel docs
				if (relevantTfqeVectors[i].term.equals(nonrelevantTfqeVectors[j].term)){
					relevantTfqeVectors[i].freq1=relevantTfqeVectors[i].freq1+nonrelevantTfqeVectors[j].freq1;
				}
			}
		}
		return relevantTfqeVectors;
	}
		
	
	
	private TermFreqQEVector[] computeTermFrequencies(Directory index) {
		ArrayList<TermFreqQEVector> tfqevList
			= new ArrayList<TermFreqQEVector>();
		
		try {
			IndexReader ireader = IndexReader.open(index);
			TermEnum allTerms = ireader.terms();
			do {
				TermFreqQEVector tfqeVector = new TermFreqQEVector();
				Term currTerm = allTerms.term();
				tfqeVector.term = allTerms.term();
				tfqeVector.freq1 = ireader.docFreq(currTerm);
				// Calculate idf
				tfqeVector.freq2 = 0;
				TermDocs td = ireader.termDocs(currTerm);
				do {
					tfqeVector.freq2 += td.freq(); 
				} while (td.next());
			
				tfqevList.add(tfqeVector);
			} while (allTerms.next());
			
			ireader.close();
			return (TermFreqQEVector[]) tfqevList.toArray();
			
		} catch (IOException e) {
			log.error("Error opening Directory", e);
			return null;
		}
	}
	
	
	/**
	 * Holds the <term, freq1, freq2> vector
	 * freq1: docFreq(term)
	 * freq2: idf (sum of tf over all documents)
	 */
	private class TermFreqQEVector implements Comparable {
		public Term term;
		public int freq1, freq2;
		
		public int compareTo(Object obj) {
			TermFreqQEVector other = (TermFreqQEVector) obj;
			if (this.freq1 > other.freq1) {
				return 1;
			} else if (this.freq1 < other.freq1) {
				return -1;
			}
			if (this.freq2 > other.freq2) {
				return 1;
			} else if (this.freq2 < other.freq2) {
				return -1;
			}
			return 0;
		}
	}

}
