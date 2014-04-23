
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.internal.HbsParser.AmpvarContext;
import org.xbib.template.handlebars.internal.HbsParser.BlockContext;
import org.xbib.template.handlebars.internal.HbsParser.CommentContext;
import org.xbib.template.handlebars.internal.HbsParser.DelimitersContext;
import org.xbib.template.handlebars.internal.HbsParser.ElseBlockContext;
import org.xbib.template.handlebars.internal.HbsParser.NewlineContext;
import org.xbib.template.handlebars.internal.HbsParser.PartialContext;
import org.xbib.template.handlebars.internal.HbsParser.SpacesContext;
import org.xbib.template.handlebars.internal.HbsParser.TemplateContext;
import org.xbib.template.handlebars.internal.HbsParser.TextContext;
import org.xbib.template.handlebars.internal.HbsParser.TvarContext;
import org.xbib.template.handlebars.internal.HbsParser.UnlessContext;
import org.xbib.template.handlebars.internal.HbsParser.VarContext;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of white-space control. It trims output on left/right of mustache expressions or
 * text.
 */
public class WhiteSpaceControl extends HbsParserBaseListener {

    /**
     * Track the spaces/lines that need to be excluded.
     */
    private List<CommonToken> spaces = new ArrayList<CommonToken>();

    /**
     * Greater than zero, if a trim-right operation is required.
     */
    private int pending = 0;

    @Override
    public void enterSpaces(final SpacesContext ctx) {
        CommonToken space = (CommonToken) ctx.SPACE().getSymbol();
        spaces.add(space);
    }

    @Override
    public void enterNewline(final NewlineContext ctx) {
        CommonToken newline = (CommonToken) ctx.NL().getSymbol();
        spaces.add(newline);
    }

    @Override
    public void exitTemplate(final TemplateContext ctx) {
        trimRight();
    }

    /**
     * Trim-left operation.
     */
    private void trimLeft() {
        hideSpaces();
    }

    /**
     * Move space tokens to the hidden channel.
     */
    private void hideSpaces() {
        for (CommonToken space : spaces) {
            space.setChannel(Token.HIDDEN_CHANNEL);
        }
    }

    /**
     * Trim-right, if ONLY if pending > 0.
     */
    private void trimRight() {
        if (pending > 0) {
            hideSpaces();
            pending -= 1;
        }
    }

    @Override
    public void enterText(final TextContext ctx) {
        trim(ctx.start, ctx.stop);
    }

    @Override
    public void enterBlock(final BlockContext ctx) {
        trim(ctx.start, ctx.END(0).getSymbol());
    }

    @Override
    public void enterElseBlock(final ElseBlockContext ctx) {
        trim(ctx.start, ctx.END().getSymbol());
    }

    @Override
    public void exitBlock(final BlockContext ctx) {
        trim(ctx.END_BLOCK().getSymbol(), ctx.END(1).getSymbol());
    }

    @Override
    public void enterComment(final CommentContext ctx) {
        trim(ctx.start, ctx.stop);
    }

    @Override
    public void enterPartial(final PartialContext ctx) {
        trim(ctx.start, ctx.stop);
    }

    @Override
    public void enterDelimiters(final DelimitersContext ctx) {
        trim(ctx.start, ctx.stop);
    }

    @Override
    public void enterUnless(final UnlessContext ctx) {
        trim(ctx.start, ctx.END().get(0).getSymbol());
    }

    @Override
    public void enterAmpvar(final AmpvarContext ctx) {
        trim(ctx.start, ctx.stop);
    }

    @Override
    public void enterTvar(final TvarContext ctx) {
        trim(ctx.start, ctx.stop);
    }

    @Override
    public void enterVar(final VarContext ctx) {
        trim(ctx.start, ctx.stop);
    }

    /**
     * Trim on left/right is required.
     *
     * @param startToken The start token.
     * @param endToken   The end token.
     */
    private void trim(final Token startToken, final Token endToken) {
        trimRight();

        String start = text(startToken);
        if (start.indexOf("~") > 0) {
            trimLeft();
        }

        String end = text(endToken);
        if (end.indexOf("~") == 0) {
            pending += 1;
        }

        // clear tokens
        spaces.clear();
    }

    /**
     * @param token The candidate token.
     * @return Text of the candidate token.
     */
    private String text(final Token token) {
        return token.getText();
    }
}
