
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.HandlebarsException;
import org.xbib.template.handlebars.util.StringUtil;
import org.xbib.template.handlebars.internal.HbsParser.AmpvarContext;
import org.xbib.template.handlebars.internal.HbsParser.BlockContext;
import org.xbib.template.handlebars.internal.HbsParser.CommentContext;
import org.xbib.template.handlebars.internal.HbsParser.DelimitersContext;
import org.xbib.template.handlebars.internal.HbsParser.PartialContext;
import org.xbib.template.handlebars.internal.HbsParser.TvarContext;
import org.xbib.template.handlebars.internal.HbsParser.UnlessContext;
import org.xbib.template.handlebars.internal.HbsParser.VarContext;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.IntervalSet;

import static org.xbib.template.handlebars.util.Validate.notEmpty;

/**
 * Fail in upon first error.
 */
class HbsErrorStrategy extends DefaultErrorStrategy {

    /**
     * Help to provide better error.
     */
    private class ErrorStrategyVisitor extends HbsParserBaseVisitor<String> {

        /**
         * The start delimiter.
         */
        private String startDelimiter;

        /**
         * The end delimiter.
         */
        private String endDelimiter;

        /**
         * Creates a new {@link org.xbib.template.handlebars.internal.HbsErrorStrategy.ErrorStrategyVisitor}.
         *
         * @param startDelimiter The start delimiter.
         * @param endDelimiter   The end delimiter.
         */
        public ErrorStrategyVisitor(final String startDelimiter, final String endDelimiter) {
            this.startDelimiter = notEmpty(startDelimiter, "The startDelimiter can't be empty/null.");
            this.endDelimiter = notEmpty(endDelimiter, "The end delimiter can't be empty/null.");
        }

        @Override
        public String visitVar(final VarContext ctx) {
            if (ctx.stop == null) {
                return endDelimiter;
            }
            return null;
        }

        @Override
        public String visitTvar(final TvarContext ctx) {
            if (ctx.stop == null) {
                return "}" + endDelimiter;
            }
            return null;
        }

        @Override
        public String visitAmpvar(final AmpvarContext ctx) {
            if (ctx.stop == null) {
                return endDelimiter;
            }
            return null;
        }

        @Override
        public String visitBlock(final BlockContext ctx) {
            if (ctx.stop == null) {
                return startDelimiter + "/";
            }
            return null;
        }

        @Override
        public String visitUnless(final UnlessContext ctx) {
            if (ctx.stop == null) {
                return endDelimiter;
            }
            return null;
        }

        @Override
        public String visitPartial(final PartialContext ctx) {
            if (ctx.stop == null) {
                return endDelimiter;
            }
            return null;
        }

        @Override
        public String visitComment(final CommentContext ctx) {
            if (ctx.stop == null) {
                return endDelimiter;
            }
            return null;
        }

        @Override
        public String visitDelimiters(final DelimitersContext ctx) {
            if (ctx.stop == null) {
                return "=" + endDelimiter;
            }
            return null;
        }
    }

    @Override
    public void recover(final Parser recognizer, final RecognitionException e) {
        // always fail
        throw new HandlebarsException(e);
    }

    @Override
    public Token recoverInline(final Parser recognizer) {
        // always fail
        throw new InputMismatchException(recognizer);
    }

    @Override
    public void reportNoViableAlternative(final Parser recognizer, final NoViableAltException e) {
        HbsParser parser = (HbsParser) recognizer;
        TokenStream tokens = parser.getTokenStream();
        HbsLexer lexer = (HbsLexer) tokens.getTokenSource();
        String msg = new ErrorStrategyVisitor(lexer.start, lexer.end).visit(e.getCtx());
        if (msg != null) {
            recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
        } else {
            super.reportNoViableAlternative(recognizer, e);
        }
    }

    @Override
    public void reportMissingToken(final Parser recognizer) {
        if (errorRecoveryMode) {
            return;
        }
        Token offendingToken = recognizer.getCurrentToken();
        IntervalSet expecting = getExpectedTokens(recognizer);
        String msg = expecting.toString(recognizer.getTokenNames());

        recognizer.notifyErrorListeners(offendingToken, msg, null);
    }

    @Override
    public void reportInputMismatch(final Parser recognizer, final InputMismatchException e) {
        String[] displayNames = displayNames(recognizer);
        String msg = e.getExpectedTokens().toString(displayNames);
        recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
    }

    /**
     * Translate the token's name to name that make more sense to the user.
     *
     * @param recognizer The lexer/parser.
     * @return User error messages.
     */
    private String[] displayNames(final Parser recognizer) {
        HbsParser parser = (HbsParser) recognizer;
        TokenStream tokens = parser.getTokenStream();
        HbsLexer lexer = (HbsLexer) tokens.getTokenSource();
        String[] tokenNames = recognizer.getTokenNames();
        String[] displayName = new String[tokenNames.length];
        for (int i = 0; i < displayName.length; i++) {
            String[] parts = StringUtil.split(tokenNames[i], "_");
            if (parts[0].equals("START")) {
                String suffix = "";
                if (parts.length > 1) {
                    if (parts[1].equals("COMMENT")) {
                        suffix = "!";
                    } else if (parts[1].equals("AMP")) {
                        suffix = "&";
                    } else if (parts[1].equals("T")) {
                        suffix = "{";
                    } else if (parts[1].equals("BLOCK")) {
                        suffix = "#";
                    } else if (parts[1].equals("DELIM")) {
                        suffix = "=";
                    } else if (parts[1].equals("PARTIAL")) {
                        suffix = ">";
                    }
                }
                displayName[i] = lexer.start + suffix;
            } else if (parts[0].equals("END")) {
                String prefix = "";
                if (parts.length > 1) {
                    if (parts[1].equals("BLOCK")) {
                        displayName[i] = lexer.start + "/";
                    } else if (parts[1].equals("DELIM")) {
                        prefix = "=";
                        displayName[i] = prefix + lexer.end;
                    } else if (parts[1].equals("T")) {
                        prefix = "}";
                        displayName[i] = prefix + lexer.end;
                    } else {
                        displayName[i] = prefix + lexer.end;
                    }
                } else {
                    displayName[i] = prefix + lexer.end;
                }
            } else if (parts[0].equals("UNLESS")) {
                displayName[i] = "^";
            } else if (parts[0].equals("NL")) {
                displayName[i] = "\\n";
            } else if (parts[0].equals("WS")) {
                displayName[i] = "space";
            } else if (parts[0].equals("DOUBLE")) {
                displayName[i] = "string";
            } else if (parts[0].equals("SINGLE")) {
                displayName[i] = "string";
            } else if (parts[0].equals("QID")) {
                displayName[i] = "id";
            } else {
                displayName[i] = tokenNames[i];
            }
            displayName[i] = displayName[i].toLowerCase().replace("'", "");
        }
        return displayName;
    }

    @Override
    public void sync(final Parser recognizer) {
        // never sync
    }
}
