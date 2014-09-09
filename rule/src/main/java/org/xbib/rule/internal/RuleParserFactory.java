package org.xbib.rule.internal;

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
import org.xbib.rule.Parser;
import org.xbib.rule.ParserFactory;
import org.xbib.rule.Rule;
import org.xbib.rule.internal.RuleLexer;
import org.xbib.rule.internal.RuleParser;
import org.xbib.rule.io.RuleSource;

import java.io.IOException;
import java.io.Reader;

public class RuleParserFactory implements ParserFactory {

    @Override
    public Parser create() {
        return new Parser() {

            @Override
            public Rule parse(final RuleSource source) throws IOException {
                /*Reader reader = null;
                try {
                    final ANTLRErrorListener errorReporter = new RuleErrorReporter(source.filename());

                    reader = source.reader();
                    // 1. Lexer
                    final RuleLexer lexer = newLexer(newStream(source.filename(), reader));
                    configure(lexer, errorReporter);

                    // 2. Parser
                    final RuleParser parser = newParser(lexer);
                    configure(parser, errorReporter);

                    // 3. Parse
                    ParseTree tree = parser.template();

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
                }*/
                return null;
            }

        };
    }

    private ANTLRInputStream newStream(final String filename, final Reader reader)
            throws IOException {
        ANTLRInputStream stream = new ANTLRInputStream(reader);
        stream.name = filename;
        return stream;
    }

    private RuleLexer newLexer(final ANTLRInputStream stream) {
        return new RuleLexer(stream) {

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


    private RuleParser newParser(final RuleLexer lexer) {
        return new RuleParser(new CommonTokenStream(lexer));
    }

    @SuppressWarnings("rawtypes")
    private void configure(final RuleParser parser, final ANTLRErrorListener errorReporter) {
        //configure((Recognizer) parser, errorReporter);

        //parser.setErrorHandler(new HbsErrorStrategy());
        //parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
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
