
package org.xbib.fsa.moore.levenshtein;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.fsa.moore.Automaton;
import org.xbib.fsa.moore.Suggester;

/**
 * Simple tests for Levenshtein automaton
 */
public class LevenshteinAutomatonTest {

    private final static Logger logger = LogManager.getLogger(LevenshteinAutomatonTest.class);

    private final String[] terms = new String[]{
        "hello",
        "ello",
        "hbllo",
        "ehllo",
        "world",
        "wrld",
        "elastic",
        "elatsic",
        "elastc",
        "elstc",
        "search",
        "saerch",
        "srch",
        "sear",
        "prais",
        "joile",
        "monteruil"
    };

    private final static int rounds = 1;
    private final static int warmup = 0;

    @Test
    public void testTop50Wiki() throws Exception {
        Top50WikiDictionary top50Wiki = new Top50WikiDictionary();
        LevenshteinAutomatonSuggester top50WikiSuggester = new LevenshteinAutomatonSuggester(top50Wiki.getWordsIterator());
        Automaton automaton = top50WikiSuggester.getAutomaton();
        logger.info("top50wiki aut alphabet size = " + automaton.getAlphabet().size());
        logger.info(String.format(Locale.ENGLISH, "top50wiki aut %-15s up",
                automaton.getClass().getSimpleName()));
        bench(top50WikiSuggester, terms);
    }

    public void bench(final Suggester suggester, final String[] input) {
        
        BenchmarkResult result = measure(() -> {
            int v = 0;
            Collection<CharSequence> sugg;
            for (CharSequence term : input) {
                sugg = suggester.getSuggestionsFor(term, true, 3);
                v += sugg.size();
                logger.info("term=" + term + " sorted sugg=" + sugg);
            }
            return v;
        });
        logger.info(String.format(Locale.ENGLISH, "%-15s words: %d, time[ms]: %s",
                suggester.getClass().getSimpleName(),
                input.length,
                result.average.toString()));
    }
    
    @SuppressWarnings("unused")
    private static volatile int guard;

    private BenchmarkResult measure(Callable<Integer> callable) {
        final double NANOS_PER_MS = 1000000;

        try {
            List<Double> times = new ArrayList<Double>();
            for (int i = 0; i < warmup + rounds; i++) {
                final long start = System.nanoTime();
                guard = callable.call();
                times.add((System.nanoTime() - start) / NANOS_PER_MS);
            }
            return new BenchmarkResult(times, warmup, rounds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class BenchmarkResult {

        public final Average average;

        public BenchmarkResult(List<Double> times, int warmup, int rounds) {
            this.average = Average.from(times.subList(warmup, times.size()));
        }
    }

    private static class Average {

        final double avg;
        final double stddev;

        Average(double avg, double stddev) {
            this.avg = avg;
            this.stddev = stddev;
        }

        static Average from(List<Double> values) {
            double sum = 0;
            double sumSquares = 0;
            for (double l : values) {
                sum += l;
                sumSquares += l * l;
            }
            double avg = sum / (double) values.size();
            return new Average(
                    (sum / (double) values.size()),
                    Math.sqrt(sumSquares / (double) values.size() - avg * avg));
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH, "%.0f [+- %.2f]", avg, stddev);
        }
    }
}