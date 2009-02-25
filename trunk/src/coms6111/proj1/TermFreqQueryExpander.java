package coms6111.proj1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

		log.debug("Creating index for relevantResultset");
		relevantIndex = createIndex(relevantResultset);
		log.debug("Creating index for nonrelevantResultset");
		nonrelevantIndex = createIndex(nonrelevantResultset);
		
		log.debug("Calling selectTerms(query, relevantIndex, nonrelevantIndex) to get top2");
		Term[] top2Terms =
			selectTerms(query, relevantIndex, nonrelevantIndex);
		
		log.debug("Adding top2 new terms to query");
		returnMe = addTermsToQuery(query, top2Terms, relevantIndex);
		
		try {
			relevantIndex.close();
			nonrelevantIndex.close();
		} catch (IOException e) {
			log.error("Error closing Directory", e);
		}
		
	    return returnMe;
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
		    // Add each Result's text to the index
		    Iterator<Result> iterator = resultset.iterator();
		    while (iterator.hasNext()) {
		    	Document doc = new Document();
			    Result r = iterator.next();
		    	String text = r.summary;
		    	log.debug("Adding new document to query, field \"body\", text: " + text);
			    doc.add(new Field("body", text, Field.Store.YES,
			        Field.Index.ANALYZED));
			    iwriter.addDocument(doc);
		    }
		    iwriter.optimize();
		    iwriter.close();

		    log.debug("Created an index. Returning the directory.");
		    return directory;

		} catch (IOException e) {
			log.error("Oh No", e);
			return null;
		}
	}
	
	private Query addTermsToQuery(Query query, Term[] terms,
			Directory relevantIndex) {
		IndexReader ireader;
		ArrayList<Term> allOurTerms = new ArrayList<Term>(); // = old query terms + new terms, unordered
		
		try {
			ireader = IndexReader.open(relevantIndex);
			Term term1 = terms[0];
			TermPositions tp1 = ireader.termPositions(term1);      
			Term term2 = terms[1];
			TermPositions tp2 = ireader.termPositions(term2);
			log.debug("addTermsToQuery: top term 1: " + term1 + "; top term 2: " + term2);
			allOurTerms.add(term1);
			allOurTerms.add(term2);
			Iterator<String> queryIterator;
			queryIterator = query.iterator();
			// Add terms from old query
			while (queryIterator.hasNext()) {
				String queryWord = queryIterator.next();
				Term aTerm = new Term("body", queryWord);
				log.debug("addTermsToQuery: adding old query term: " + aTerm);
				allOurTerms.add(aTerm);
			}
			
			Term[][] termsInOrder = getTermsPositionsInDocuments(allOurTerms.toArray(new Term[0]), ireader);
			Term[] bestDocTerms = null; // the terms in order, representing a (best) document
			int numTermsBestDoc = 0;
			for (int i = 0; i < termsInOrder.length; i++) {
				if (termsInOrder[i] == null)
					break;
				int numTermsCurrDoc = termsInOrder[i].length;
				if (numTermsCurrDoc > numTermsBestDoc) {
					bestDocTerms = termsInOrder[i];
					numTermsBestDoc = numTermsCurrDoc;
				}
				
			}
			
			// Add the terms from the document w/ the most terms, in the order they first appear in that doc.
			// Afterwards, add any remaining terms that were not in the top document, in order by freq.
			ArrayList<Term> finalBestTerms = new ArrayList<Term>();
			for (Term t : bestDocTerms) {
				log.debug("Adding term from bestDoc: " + t);
				finalBestTerms.add(t);
			}
			for (Term t : allOurTerms) {
				boolean inBestDoc = false;
				for (Term bdt : bestDocTerms) {
					if (t.equals(bdt)) {
						inBestDoc = true;
						break;
					}
				}
				if (!inBestDoc) {
					log.debug("Adding term not in bestDoc: " + t);
					finalBestTerms.add(t);
				}
			}
			if (numTermsBestDoc == 0) {
				// TODO handle this
				log.warn("numTermsBestDoc == 0");
			}
			
			return new Query(finalBestTerms.toArray(new Term[0]), query.getIteration() + 1);
			
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}
	
	/**
	 * Return array of array.
	 * Each outer array entry represents a document.
	 * Each inner array entry represents a term in that document, in order by first occurrence.
	 * @param allOurTerms
	 * @param ireader
	 * @return
	 */
	private Term[][] getTermsPositionsInDocuments(Term[] allOurTerms, IndexReader ireader) {
		Term[][] returnMe = new Term[10][];
		HashMap<Integer, List<TermDocPosVector>> docToTerms = new HashMap<Integer, List<TermDocPosVector>>();
		
		try {
			// Go thru all our terms and retrieve term positions for each document
			for (Term t : allOurTerms) {
				TermPositions tp = ireader.termPositions(t);
				while (tp.next()) {
					int currDoc = tp.doc();
					int firstPos = tp.nextPosition(); // first position in current doc
					if (!docToTerms.containsKey(currDoc))
						docToTerms.put(currDoc, new ArrayList<TermDocPosVector>());
					docToTerms.get(currDoc).add(new TermDocPosVector(t, currDoc, firstPos));
				}
			}
			// For each document, sort the gathered terms by position in that doc
			Iterator<Integer> iterator = docToTerms.keySet().iterator();
			int j = 0;
			while (iterator.hasNext()) {
				Integer key = iterator.next();
				Term[] termsInOrderForADoc = new Term[docToTerms.get(key).size()];
				TermDocPosVector[] temp = docToTerms.get(key).toArray(new TermDocPosVector[0]);
				Arrays.sort(temp);
				for (int i = 0; i < temp.length; i++) {
					termsInOrderForADoc[i] = temp[i].term;
				}
				returnMe[j++] = termsInOrderForADoc;
			}
			
			return returnMe;
			
		} catch (IOException e) {
			log.error(e);
			return null;
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
			if (alreadyInQuery) {
				log.debug("Would add term " + combinedTfqeVectors[i].term + ", but it's already in query.");
				continue;
			}
			log.debug("Adding term " + combinedTfqeVectors[i].term + " to top2");
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
			nonrelevantTfqeVectors[i].freq1 *= -1;
		}
		
		for (int i = 0; i< relevantTfqeVectors.length; i++) {
			for (int j=0 ; j< nonrelevantTfqeVectors.length; j++) {
				// decrease term freq by # of times it appears in nonrel docs
				if (relevantTfqeVectors[i].term.equals(nonrelevantTfqeVectors[j].term)){
					relevantTfqeVectors[i].freq1 += nonrelevantTfqeVectors[j].freq1;
					log.debug("term \"" + relevantTfqeVectors[i].term + "\" found in both relevant and nonrelevant docs. "
							+ "new freq: " + relevantTfqeVectors[i].freq1);
				}
			}
		}
		return relevantTfqeVectors;
	}
		
	
	
	private TermFreqQEVector[] computeTermFrequencies(Directory index) {
		ArrayList<TermFreqQEVector> tfqevList
			= new ArrayList<TermFreqQEVector>();
		
		log.debug("In computeTermFrequencies. index is " + index);
		try {
			IndexReader ireader = IndexReader.open(index);
			TermEnum allTerms = ireader.terms();
			log.debug("created allTerms. it is " + allTerms);
			while (allTerms.next()) {
				Term currTerm = allTerms.term();
				log.debug("currTerm is " + currTerm);
				TermFreqQEVector tfqeVector = new TermFreqQEVector(
						allTerms.term(),
						ireader.docFreq(currTerm),
						0);
				// calculate idf (freq2)
				TermDocs td = ireader.termDocs(currTerm);
				while (td.next()) {
					tfqeVector.freq2 += td.freq(); 
				}
			
				tfqevList.add(tfqeVector);
			}
			
			ireader.close();
			return tfqevList.toArray(new TermFreqQEVector[0]);
			
		} catch (IOException e) {
			log.error("Error opening Directory", e);
			return null;
		}
	}

	/**
	 * Holds the <term, docNum, firstPos> vector
	 */
	private class TermDocPosVector implements Comparable {
		public Term term;
		public int doc, pos;
		
		public TermDocPosVector(Term t, int d, int p) {
			term = t;
			doc = d;
			pos = p;
		}
		
		public int compareTo(Object obj) {
			TermDocPosVector other = (TermDocPosVector) obj;
			if (this.pos < other.pos)
				return -1;
			else if (this.pos > other.pos)
				return 1;
			else
				return 0;
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
		
		public TermFreqQEVector(Term t, int f1, int f2) {
			term = t;
			freq1 = f1;
			freq2 = f2;
		}
		
		/**
		 * If "this" is more frequent than "other", return -1
		 * so that Arrays.sort orders the highest frequencies first
		 */
		public int compareTo(Object obj) {
			TermFreqQEVector other = (TermFreqQEVector) obj;
			if (this.freq1 > other.freq1) {
				return -1;
			} else if (this.freq1 < other.freq1) {
				return 1;
			}
			if (this.freq2 > other.freq2) {
				return -1;
			} else if (this.freq2 < other.freq2) {
				return 1;
			}
			return 0;
		}
	}

}
