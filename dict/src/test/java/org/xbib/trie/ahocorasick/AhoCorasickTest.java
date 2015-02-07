package org.xbib.trie.ahocorasick;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class AhoCorasickTest {

	private static AhoCorasick tree;

    @BeforeMethod
	public void setUp() {
		tree = new AhoCorasick();
	}

    @Test
	public void testConstruction() {
		tree.add("hello".toCharArray(), "hello");
		tree.add("hi".toCharArray(), "hi");
		tree.prepare();

		State s0 = tree.getRoot();
		State s1 = s0.get('h');
		State s2 = s1.get('e');
		State s3 = s2.get('l');
		State s4 = s3.get('l');
		State s5 = s4.get('o');
		State s6 = s1.get('i');

		assertEquals(s0, s1.getFail());
		assertEquals(s0, s2.getFail());
		assertEquals(s0, s3.getFail());
		assertEquals(s0, s4.getFail());
		assertEquals(s0, s5.getFail());
		assertEquals(s0, s6.getFail());

		assertEquals(0, s0.getOutputs().size());
		assertEquals(0, s1.getOutputs().size());
		assertEquals(0, s2.getOutputs().size());
		assertEquals(0, s3.getOutputs().size());
		assertEquals(0, s4.getOutputs().size());
		assertEquals(1, s5.getOutputs().size());
		assertEquals(1, s6.getOutputs().size());

	}

    @Test
	public void testExample() {
		tree.add("he".toCharArray(), "he");
		tree.add("she".toCharArray(), "she");
		tree.add("his".toCharArray(), "his");
		tree.add("hers".toCharArray(), "hers");
		assertEquals(10, tree.getRoot().size());
		tree.prepare();
		State s0 = tree.getRoot();
		State s1 = s0.get('h');
		State s2 = s1.get('e');

		State s3 = s0.get('s');
		State s4 = s3.get('h');
		State s5 = s4.get('e');

		State s6 = s1.get('i');
		State s7 = s6.get('s');

		State s8 = s2.get('r');
		State s9 = s8.get('s');

		assertEquals(s0, s1.getFail());
		assertEquals(s0, s2.getFail());
		assertEquals(s0, s3.getFail());
		assertEquals(s0, s6.getFail());
		assertEquals(s0, s8.getFail());

		assertEquals(s1, s4.getFail());
		assertEquals(s2, s5.getFail());
		assertEquals(s3, s7.getFail());
		assertEquals(s3, s9.getFail());

		assertEquals(0, s1.getOutputs().size());
		assertEquals(0, s3.getOutputs().size());
		assertEquals(0, s4.getOutputs().size());
		assertEquals(0, s6.getOutputs().size());
		assertEquals(0, s8.getOutputs().size());
		assertEquals(1, s2.getOutputs().size());
		assertEquals(1, s7.getOutputs().size());
		assertEquals(1, s9.getOutputs().size());
		assertEquals(2, s5.getOutputs().size());
	}

    @Test
	public void testStartSearchWithSingleResult() {
		tree.add("apple".toCharArray(), "apple");
		tree.prepare();
		SearchResult result = tree.startSearch("washington cut the apple tree".toCharArray());
		assertEquals(1, result.getOutputs().size());
		assertEquals("apple", result.getOutputs().iterator().next());
		assertEquals(24, result.lastIndex);
		assertEquals(null, tree.continueSearch(result));
	}

    @Test
	public void testStartSearchWithUnicodeResult() {
		tree.add("españa".toCharArray(), "españa");
		tree.prepare();
		SearchResult result = tree.startSearch("la campeona del mundo de fútbol es españa"
				.toCharArray());
		assertEquals(1, result.getOutputs().size());
		assertEquals("españa",result.getOutputs().iterator().next());
		assertEquals(41, result.lastIndex);
		assertEquals(null, tree.continueSearch(result));
	}

    @Test
	public void testStartSearchWithAdjacentResults() {
		tree.add("john".toCharArray(), "john");
		tree.add("jane".toCharArray(), "jane");
		tree.prepare();
		SearchResult firstResult = tree.startSearch("johnjane".toCharArray());
		SearchResult secondResult = tree.continueSearch(firstResult);
		assertEquals(null, tree.continueSearch(secondResult));
	}

    @Test
	public void testStartSearchOnEmpty() {
		tree.add("cipher".toCharArray(), "0");
		tree.add("zip".toCharArray(), "1");
		tree.add("nought".toCharArray(), "2");
		tree.prepare();
		SearchResult result = tree.startSearch("".toCharArray());
		assertEquals(null, result);
	}

    @Test
	public void testMultipleOutputs() {
		tree.add("x".toCharArray(), "x");
		tree.add("xx".toCharArray(), "xx");
		tree.add("xxx".toCharArray(), "xxx");
		tree.prepare();

		SearchResult result = tree.startSearch("xxx".toCharArray());
		assertEquals(1, result.lastIndex);
		assertEquals(new HashSet<Object>(Arrays.asList(new String[] { "x" })),
				result.getOutputs());

		result = tree.continueSearch(result);
		assertEquals(2, result.lastIndex);
		assertEquals(
				new HashSet<Object>(Arrays.asList(new String[] { "xx", "x" })),
				result.getOutputs());

		result = tree.continueSearch(result);
		assertEquals(3, result.lastIndex);
		assertEquals(
				new HashSet<Object>(Arrays.asList(new String[] { "xxx", "xx",
						"x" })), result.getOutputs());

		assertEquals(null, tree.continueSearch(result));
	}

    @Test
	public void testIteratorInterface() {
		tree.add("moo".toCharArray(), "moo");
		tree.add("one".toCharArray(), "one");
		tree.add("on".toCharArray(), "on");
		tree.add("ne".toCharArray(), "ne");
		tree.prepare();
		Iterator<SearchResult> iter = tree.progressiveSearch("one moon ago".toCharArray());

		assertTrue(iter.hasNext());
		SearchResult r = iter.next();
		assertEquals(new HashSet<Object>(Arrays.asList(new String[] { "on" })),
				r.getOutputs());
		assertEquals(2, r.lastIndex);

		assertTrue(iter.hasNext());
		r =  iter.next();
		assertEquals(
				new HashSet<Object>(Arrays.asList(new String[] { "one", "ne" })),
				r.getOutputs());
		assertEquals(3, r.lastIndex);

		assertTrue(iter.hasNext());
		r = iter.next();
		assertEquals(
				new HashSet<Object>(Arrays.asList(new String[] { "moo" })),
				r.getOutputs());
		assertEquals(7, r.lastIndex);

		assertTrue(iter.hasNext());
		r = iter.next();
		assertEquals(new HashSet<Object>(Arrays.asList(new String[] { "on" })),
				r.getOutputs());
		assertEquals(8, r.lastIndex);

		assertFalse(iter.hasNext());

		try {
			iter.next();
			fail();
		} catch (NoSuchElementException e) {
            //
		}
	}

    @Test
	public void largerTextExample() {
		String text = "The ga3 mutant of Arabidopsis is a gibberellin-responsive dwarf. We present data showing that the ga3-1 mutant is deficient in ent-kaurene oxidase activity, the first cytochrome P450-mediated step in the gibberellin biosynthetic pathway. By using a combination of conventional map-based cloning and random sequencing we identified a putative cytochrome P450 gene mapping to the same location as GA3. Relative to the progenitor line, two ga3 mutant alleles contained single base changes generating in-frame stop codons in the predicted amino acid sequence of the P450. A genomic clone spanning the P450 locus complemented the ga3-2 mutant. The deduced GA3 protein defines an additional class of cytochrome P450 enzymes. The GA3 gene was expressed in all tissues examined, RNA abundance being highest in inflorescence tissue.";
		String[] terms = { "microsome", "cytochrome",
				"cytochrome P450 activity", "gibberellic acid biosynthesis",
				"GA3", "cytochrome P450", "oxygen binding", "AT5G25900.1",
				"protein", "RNA", "gibberellin", "Arabidopsis",
				"ent-kaurene oxidase activity", "inflorescence", "tissue", };
        for (String term : terms) {
            tree.add(term.toCharArray(), term);
        }
		tree.prepare();

		Set<Object> termsThatHit = new HashSet<Object>();
		for (Iterator<SearchResult> iter = tree.progressiveSearch(text.toCharArray()); iter
				.hasNext();) {
			SearchResult result = iter.next();
			termsThatHit.addAll(result.getOutputs());
		}
		assertEquals(
				new HashSet<Object>(Arrays.asList(new String[] { "cytochrome",
						"GA3", "cytochrome P450", "protein", "RNA",
						"gibberellin", "Arabidopsis",
						"ent-kaurene oxidase activity", "inflorescence",
						"tissue", })), termsThatHit);

	}

    @Test
	// Without overlapping
	public void testRemoveOverlapping1() {
		List<Result> results = new ArrayList<Result>();
		results.add(new Result(0, 0, 2));
		results.add(new Result(1, 2, 4));
		results.add(new Result(2, 5, 6));
		
		assertEquals(3, results.size());
		tree.removeOverlapping(results); // No effect
		assertEquals(3, results.size());
	}

    @Test
	// With a clear overlapping
	public void testRemoveOverlapping2() {
		List<Result> results = new ArrayList<Result>();
		results.add(new Result(0, 0, 2));
		results.add(new Result(1, 1, 4));
		results.add(new Result(2, 5, 6));
		
		assertEquals(3, results.size());
		tree.removeOverlapping(results);
		assertEquals(2, results.size());
		assertEquals(0, results.get(0).getOutput());
		assertEquals(2, results.get(1).getOutput());
	}

    @Test
	// With two overlapping, one with the same start index
	public void testRemoveOverlapping3() {
		List<Result> results = new ArrayList<Result>();
		results.add(new Result(0, 0, 2));
		results.add(new Result(1, 0, 4));
		results.add(new Result(2, 3, 6));
		
		assertEquals(3, results.size());
		tree.removeOverlapping(results);
		assertEquals(1, results.size());
		assertEquals(1, results.get(0).getOutput());
	}

    @Test
	public void testCompleteSearchNotOverlapping() {
		tree.add("Apple");
		tree.add("App");
		tree.add("Microsoft");
		tree.add("Mic");
		tree.prepare();
		
		String inputText = "Apple is better than Microsoft";
		List<Result> results = tree.completeSearch(inputText, false);
		
		assertEquals(2, results.size());
		assertEquals("Apple", results.get(0).getOutput());
		assertEquals("Microsoft", results.get(1).getOutput());
	}

    @Test
	public void testCompleteSearchOverlapping() {
		tree.add("Apple");
		tree.add("App");
		tree.add("Microsoft");
		tree.add("Mic");
		tree.prepare();
		
		String inputText = "Apple is better than Microsoft";
		List<Result> results = tree.completeSearch(inputText, true);
		
		assertEquals(4, results.size());
		assertEquals("App", results.get(0).getOutput());
		assertEquals("Apple", results.get(1).getOutput());
		assertEquals("Mic", results.get(2).getOutput());
		assertEquals("Microsoft", results.get(3).getOutput());
	}

    @Test
	public void testCompleteSearchTokenized1() {
		tree.add("Apple");
		tree.add("e i");
		tree.add("than Microsoft");
		tree.add("Microsoft");
		tree.add("er than");
		tree.prepare();
		
		String inputText = "Apple is better than Microsoft";
		// [[0,5]: Apple, [13,20]: er than, [21,30]: Microsoft]
        List<Result> results = tree.completeSearch(inputText, false);

		assertEquals(3, results.size());
		assertEquals("Apple", results.get(0).getOutput());
        assertEquals("er than", results.get(1).getOutput());
        assertEquals("Microsoft", results.get(2).getOutput());

		results = tree.completeSearch(inputText, true);
        //[[0,5]: Apple, [4,7]: e i, [13,20]: er than, [16,30]: than Microsoft, [21,30]: Microsoft]

		assertEquals(5, results.size());
        assertEquals("Apple", results.get(0).getOutput());
        assertEquals("e i", results.get(1).getOutput());
        assertEquals("er than", results.get(2).getOutput());
		assertEquals("than Microsoft", results.get(3).getOutput());
		assertEquals("Microsoft", results.get(4).getOutput());
	}

    @Test
	public void testCompleteSearchTokenized2() {
		tree.add("Real Madrid");
		tree.add("Madrid");
		tree.add("Barcelona");
		tree.add("Messi");
		tree.add("esp");
		tree.add("o p");
		tree.add("Mes");
		tree.add("Rea");
		tree.prepare();
		
		String inputText = "El Real Madrid no puede fichar a Messi porque es del Barcelona";
		List<Result> results = tree.completeSearch(inputText, false);
        // [[3,14]: Real Madrid, [16,19]: o p, [33,38]: Messi, [53,62]: Barcelona]

		assertEquals(4, results.size());
		assertEquals("Real Madrid", results.get(0).getOutput());
        assertEquals("o p", results.get(1).getOutput());
		assertEquals("Messi", results.get(2).getOutput());
		assertEquals("Barcelona", results.get(3).getOutput());
	}

    @Test
	public void testCompleteSearchTokenized3() {
		tree.add("comp");
		tree.prepare();
		
		String inputText = "A complete sentence";
		List<Result> results = tree.completeSearch(inputText, false);

		assertEquals(1, results.size());
	}

    @Test
	public void testCompleteSearchTokenized4() {
		tree.add("Madrid");
		tree.add("Real");
		tree.add("Real Madrid");
		tree.add("El Real de España");
		tree.prepare();
		
		String inputText = "El Real Madrid no puede fichar a Messi porque es del Barcelona";
		List<Result> results = tree.completeSearch(inputText, false);
		
		assertEquals(1, results.size());
		assertEquals("Real Madrid", results.get(0).getOutput());
	}

    @Test
	public void testCompleteSearchTokenized5() {
		tree.add("Microsoft");
		tree.add("than Microsoft");
		tree.add("han Microsoft");
		tree.add("n Microsoft");
		tree.add(" Microsoft");
		tree.prepare();
		
		String inputText = "Apple is better than Microsoft";
		List<Result> results = tree.completeSearch(inputText, true);
        // [[16,30]: than Microsoft, [17,30]: han Microsoft, [19,30]: n Microsoft, [20,30]:  Microsoft, [21,30]: Microsoft]

		assertEquals(5, results.size());
		assertEquals("than Microsoft", results.get(0).getOutput());
        assertEquals("han Microsoft", results.get(1).getOutput());
        assertEquals("n Microsoft", results.get(2).getOutput());
        assertEquals(" Microsoft", results.get(3).getOutput());
		assertEquals("Microsoft", results.get(4).getOutput());
	}

}
