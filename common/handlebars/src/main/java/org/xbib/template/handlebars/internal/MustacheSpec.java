
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.internal.HbsParser.AmpvarContext;
import org.xbib.template.handlebars.internal.HbsParser.BlockContext;
import org.xbib.template.handlebars.internal.HbsParser.CommentContext;
import org.xbib.template.handlebars.internal.HbsParser.DelimitersContext;
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
 * Remove space and lines according to the Mustache Spec.
 */
public class MustacheSpec extends HbsParserBaseListener {

    /**
     * Track if the current line has real text (not spaces).
     */
    private boolean nonSpace = false;

    /**
     * Track if the current line has mustache instruction.
     */
    private Boolean hasTag;

    /**
     * Track the current line.
     */
    protected StringBuilder line = new StringBuilder();

    /**
     * Track the spaces/lines that need to be excluded.
     */
    private List<CommonToken> spaces = new ArrayList<CommonToken>();

    @Override
    public void enterSpaces(final SpacesContext ctx) {
        CommonToken space = (CommonToken) ctx.SPACE().getSymbol();
        line.append(space.getText());
        spaces.add(space);
    }

    @Override
    public void enterNewline(final NewlineContext ctx) {
        CommonToken newline = (CommonToken) ctx.NL().getSymbol();
        spaces.add(newline);
        stripSpaces();
    }

    @Override
    public void exitTemplate(final TemplateContext ctx) {
        stripSpaces();
    }

    /**
     * Move tokens to the hidden channel if necessary.
     */
    private void stripSpaces() {
        boolean hasTag = this.hasTag == null ? false : this.hasTag.booleanValue();
        if (hasTag && !nonSpace) {
            for (CommonToken space : spaces) {
                space.setChannel(Token.HIDDEN_CHANNEL);
            }
        } else {
            spaces.clear();
        }

        this.hasTag = null;
        nonSpace = false;
        line.setLength(0);
    }

    @Override
    public void enterText(final TextContext ctx) {
        nonSpace = true;
    }

    @Override
    public void enterBlock(final BlockContext ctx) {
        hasTag(true);
    }

    @Override
    public void exitBlock(final BlockContext ctx) {
        hasTag(true);
    }

    @Override
    public void enterComment(final CommentContext ctx) {
        hasTag(true);
    }

    @Override
    public void exitPartial(final PartialContext ctx) {
        hasTag(true);
    }

    @Override
    public void enterDelimiters(final DelimitersContext ctx) {
        hasTag(true);
    }

    @Override
    public void enterUnless(final UnlessContext ctx) {
        hasTag(true);
    }

    @Override
    public void exitUnless(final UnlessContext ctx) {
        hasTag(true);
    }

    @Override
    public void enterAmpvar(final AmpvarContext ctx) {
        hasTag(false);
    }

    @Override
    public void enterTvar(final TvarContext ctx) {
        hasTag(false);
    }

    @Override
    public void enterVar(final VarContext ctx) {
        hasTag(false);
    }

    /**
     * Mark the current line with a mustache instruction.
     *
     * @param hasTag True, to indicate there is a mustache instruction.
     */
    private void hasTag(final boolean hasTag) {
        if (this.hasTag != Boolean.FALSE) {
            this.hasTag = hasTag;
        }
    }
}
