
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Parser;
import org.xbib.template.handlebars.ParserFactory;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.io.TemplateSource;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.Reader;

/**
 * The default {@link ParserFactory}.
 */
public class HbsParserFactory implements ParserFactory {

    /**
     * Creates a new {@link Parser}.
     *
     * @param handlebars     The parser owner.
     * @param startDelimiter The start delimiter.
     * @param endDelimiter   The end delimiter.
     * @return A new {@link Parser}.
     */
    @Override
    public Parser create(final Handlebars handlebars,
                         final String startDelimiter,
                         final String endDelimiter) {
        return new Parser() {

            @Override
            public Template parse(final TemplateSource source) throws IOException {
                Reader reader = null;
                try {
                    final ANTLRErrorListener errorReporter = new HbsErrorReporter(source.filename());

                    reader = source.reader();
                    // 1. Lexer
                    final HbsLexer lexer = newLexer(newStream(source.filename(), reader),
                            startDelimiter, endDelimiter);
                    configure(lexer, errorReporter);

                    // 2. Parser
                    final HbsParser parser = newParser(lexer);
                    configure(parser, errorReporter);

                    // 3. Parse
                    ParseTree tree = parser.template();

                    // remove unnecessary spaces and new lines?
                    if (handlebars.prettyPrint()) {
                        new ParseTreeWalker().walk(new MustacheSpec(), tree);
                    }

                    if (lexer.whiteSpaceControl) {
                        new ParseTreeWalker().walk(new WhiteSpaceControl(), tree);
                    }

                    /**
                     * Build the AST.
                     */
                    TemplateBuilder builder = new TemplateBuilder(handlebars, source) {
                        @Override
                        protected void reportError(final CommonToken offendingToken, final int line,
                                                   final int column,
                                                   final String message) {
                            errorReporter.syntaxError(parser, offendingToken, line, column, message, null);
                        }
                    };
                    Template template = builder.visit(tree);
                    return template;
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }

        };
    }

    /**
     * Creates a new {@link ANTLRInputStream}.
     *
     * @param filename The file's name.
     * @param reader   A reader.
     * @return A new {@link ANTLRInputStream}.
     * @throws java.io.IOException If the reader can't be open.
     */
    private ANTLRInputStream newStream(final String filename, final Reader reader)
            throws IOException {
        ANTLRInputStream stream = new ANTLRInputStream(reader);
        stream.name = filename;
        return stream;
    }

    /**
     * Creates a new {@link HbsLexer}.
     *
     * @param stream         The input stream.
     * @param startDelimiter The start delimiter.
     * @param endDelimiter   The end delimiter.
     * @return A new {@link HbsLexer}.
     */
    private HbsLexer newLexer(final ANTLRInputStream stream, final String startDelimiter,
                              final String endDelimiter) {
        return new HbsLexer(stream, startDelimiter, endDelimiter) {

            @Override
            public void notifyListeners(final LexerNoViableAltException e) {
                String text = _input.getText(Interval.of(_tokenStartCharIndex, _input.index()));
                String msg = "found: '" + getErrorDisplay(text) + "'";
                ANTLRErrorListener listener = getErrorListenerDispatch();
                listener
                        .syntaxError(this, null, _tokenStartLine, _tokenStartCharPositionInLine, msg, e);
            }

            @Override
            public void recover(final LexerNoViableAltException e) {
                throw new IllegalArgumentException(e);
            }
        };
    }

    /**
     * Creates a new {@link HbsParser}.
     *
     * @param lexer The {@link HbsLexer}.
     * @return A new {@link HbsParser}.
     */
    private HbsParser newParser(final HbsLexer lexer) {
        return new HbsParser(new CommonTokenStream(lexer)) {
            @Override
            void setStart(final String start) {
                lexer.start = start;
            }

            @Override
            void setEnd(final String end) {
                lexer.end = end;
            }
        };
    }

    /**
     * Configure a {@link HbsParser}.
     *
     * @param parser        The {@link HbsParser}.
     * @param errorReporter The error reporter.
     */
    @SuppressWarnings("rawtypes")
    private void configure(final HbsParser parser, final ANTLRErrorListener errorReporter) {
        configure((Recognizer) parser, errorReporter);

        parser.setErrorHandler(new HbsErrorStrategy());
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
    }

    /**
     * Configure a recognizer with an error reporter.
     *
     * @param recognizer    A recongnizer.
     * @param errorReporter The error reporter.
     */
    private void configure(final Recognizer<?, ?> recognizer,
                           final ANTLRErrorListener errorReporter) {
        recognizer.removeErrorListeners();
        recognizer.addErrorListener(errorReporter);
    }
}
