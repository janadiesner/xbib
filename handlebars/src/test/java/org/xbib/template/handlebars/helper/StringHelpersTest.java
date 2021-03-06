package org.xbib.template.handlebars.helper;

import org.junit.Test;
import org.xbib.template.handlebars.AbstractTest;
import org.xbib.template.handlebars.HandlebarsContext;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;
import org.xbib.template.handlebars.TagType;
import org.xbib.template.handlebars.Template;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.xbib.template.handlebars.helper.StringHelpers.abbreviate;
import static org.xbib.template.handlebars.helper.StringHelpers.capitalize;
import static org.xbib.template.handlebars.helper.StringHelpers.capitalizeFirst;
import static org.xbib.template.handlebars.helper.StringHelpers.center;
import static org.xbib.template.handlebars.helper.StringHelpers.cut;
import static org.xbib.template.handlebars.helper.StringHelpers.defaultIfEmpty;
import static org.xbib.template.handlebars.helper.StringHelpers.ljust;
import static org.xbib.template.handlebars.helper.StringHelpers.lower;
import static org.xbib.template.handlebars.helper.StringHelpers.replace;
import static org.xbib.template.handlebars.helper.StringHelpers.rjust;
import static org.xbib.template.handlebars.helper.StringHelpers.slugify;
import static org.xbib.template.handlebars.helper.StringHelpers.stringFormat;
import static org.xbib.template.handlebars.helper.StringHelpers.stripTags;
import static org.xbib.template.handlebars.helper.StringHelpers.substring;
import static org.xbib.template.handlebars.helper.StringHelpers.upper;
import static org.xbib.template.handlebars.helper.StringHelpers.yesno;

/**
 * Unit test for {@link StringHelpers}.
 */
public class StringHelpersTest extends AbstractTest {

    @Test
    public void capFirst() throws IOException {
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("capitalizeFirst", capitalizeFirst.name());
        assertEquals("Handlebars.java",
                capitalizeFirst.apply("handlebars.java", options));

        verify(options);
    }

    @Test
    public void center() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("size")).andReturn(19);
        expect(options.isFalsy(anyObject())).andReturn(false);
        expect(options.hash("pad", " ")).andReturn(null);

        replay(options);

        assertEquals("center", center.name());
        assertEquals("  Handlebars.java  ",
                center.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void centerWithPad() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("size")).andReturn(19);
        expect(options.hash("pad", " ")).andReturn("*");
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("center", center.name());
        assertEquals("**Handlebars.java**",
                center.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void cut() throws IOException {
        Options options = createMock(Options.class);
        expect(options.param(0, " ")).andReturn(" ");
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("cut", cut.name());
        assertEquals("handlebars.java",
                cut.apply("handle bars .  java", options));

        verify(options);
    }

    @Test
    public void cutNoWhitespace() throws IOException {
        Options options = createMock(Options.class);
        expect(options.param(0, " ")).andReturn("*");
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("cut", cut.name());
        assertEquals("handlebars.java",
                cut.apply("handle*bars*.**java", options));

        verify(options);
    }

    @Test
    public void defaultStr() throws IOException {
        Options options = createMock(Options.class);
        expect(options.param(0, "")).andReturn("handlebars.java").anyTimes();

        replay(options);

        assertEquals("defaultIfEmpty", defaultIfEmpty.name());
        assertEquals("handlebars.java",
                defaultIfEmpty.apply(null, options));
        assertEquals("handlebars.java",
                defaultIfEmpty.apply(false, options));
        assertEquals("handlebars.java",
                defaultIfEmpty.apply(Collections.emptyList(), options));

        assertEquals("something",
                defaultIfEmpty.apply("something", options));

        verify(options);
    }

    @Test
    public void joinIterable() throws IOException {
        shouldCompileTo("{{{join this \", \"}}}", Arrays.asList("6", "7", "8"),
                $("join", StringHelpers.join), "6, 7, 8");
    }

    @Test
    public void joinIterator() throws IOException {
        shouldCompileTo("{{{join this \", \"}}}", Arrays.asList("6", "7", "8").iterator(),
                $("join", StringHelpers.join), "6, 7, 8");
    }

    @Test
    public void joinArray() throws IOException {
        shouldCompileTo("{{{join this \", \"}}}", new String[]{"6", "7", "8"},
                $("join", StringHelpers.join), "6, 7, 8");
    }

    @Test
    public void joinValues() throws IOException {
        shouldCompileTo("{{{join \"6\" 7 n8 \"-\"}}}", $("n8", 8), $("join", StringHelpers.join),
                "6-7-8");
    }

    @Test
    public void joinWithPrefixAndSuffix() throws IOException {
        shouldCompileTo("{{{join this \", \" prefix='<' suffix='>'}}}", Arrays.asList("6", "7", "8"),
                $("join", StringHelpers.join), "<6, 7, 8>");
    }

    @Test
    public void ljust() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("size")).andReturn(20);
        expect(options.hash("pad", " ")).andReturn(null);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("ljust", ljust.name());
        assertEquals("Handlebars.java     ",
                ljust.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void ljustWithPad() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("size")).andReturn(17);
        expect(options.hash("pad", " ")).andReturn("+");
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("ljust", ljust.name());
        assertEquals("Handlebars.java++",
                ljust.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void rjust() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("size")).andReturn(20);
        expect(options.hash("pad", " ")).andReturn(null);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("rjust", rjust.name());
        assertEquals("     Handlebars.java",
                rjust.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void rjustWithPad() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("size")).andReturn(17);
        expect(options.hash("pad", " ")).andReturn("+");
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("rjust", rjust.name());
        assertEquals("++Handlebars.java",
                rjust.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void substringWithStart() throws IOException {
        Handlebars hbs = createMock(Handlebars.class);
        HandlebarsContext ctx = createMock(HandlebarsContext.class);
        Template fn = createMock(Template.class);

        Options options = new Options.Builder(hbs, TagType.VAR, ctx, fn)
                .setParams(new Object[]{11})
                .build();

        assertEquals("substring", substring.name());
        assertEquals("java",
                substring.apply("Handlebars.java", options));
    }

    @Test
    public void substringWithStartAndEnd() throws IOException {
        Handlebars hbs = createMock(Handlebars.class);
        HandlebarsContext ctx = createMock(HandlebarsContext.class);
        Template fn = createMock(Template.class);

        Options options = new Options.Builder(hbs, TagType.VAR, ctx, fn)
                .setParams(new Object[]{0, 10})
                .build();

        assertEquals("substring", substring.name());
        assertEquals("Handlebars",
                substring.apply("Handlebars.java", options));
    }

    @Test
    public void lower() throws IOException {
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("lower", lower.name());
        assertEquals("handlebars.java",
                lower.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void upper() throws IOException {
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("upper", upper.name());
        assertEquals("HANDLEBARS.JAVA",
                upper.apply("Handlebars.java", options));

        verify(options);
    }

    @Test
    public void slugify() throws IOException {
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("slugify", slugify.name());
        assertEquals("joel-is-a-slug",
                slugify.apply("  Joel is a slug  ", options));

        verify(options);
    }

    @Test
    public void replace() throws IOException {
        Handlebars hbs = createMock(Handlebars.class);
        HandlebarsContext ctx = createMock(HandlebarsContext.class);
        Template fn = createMock(Template.class);

        Options options = new Options.Builder(hbs, TagType.VAR, ctx, fn)
                .setParams(new Object[]{"...", "rocks"})
                .build();

        assertEquals("replace", replace.name());
        assertEquals("Handlebars rocks",
                replace.apply("Handlebars ...", options));
    }

    @Test
    public void stringFormat() throws IOException {
        Handlebars hbs = createMock(Handlebars.class);
        HandlebarsContext ctx = createMock(HandlebarsContext.class);
        Template fn = createMock(Template.class);

        Options options = new Options.Builder(hbs, TagType.VAR, ctx, fn)
                .setParams(new Object[]{"handlebars.java"})
                .build();

        assertEquals("stringFormat", stringFormat.name());

        assertEquals("Hello handlebars.java!",
                stringFormat.apply("Hello %s!", options));
    }

    @Test
    public void stringDecimalFormat() throws IOException {
        Handlebars hbs = createMock(Handlebars.class);
        HandlebarsContext ctx = createMock(HandlebarsContext.class);
        Template fn = createMock(Template.class);

        Options options = new Options.Builder(hbs, TagType.VAR, ctx, fn)
                .setParams(new Object[]{10.0 / 3.0})
                .build();

        assertEquals("stringFormat", stringFormat.name());

        assertEquals(String.format("10 / 3 = %.2f", 10.0 / 3.0),
                stringFormat.apply("10 / 3 = %.2f", options));
    }

    @Test
    public void stripTags() throws IOException {
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("stripTags", stripTags.name());
        assertEquals("Joel is a slug",
                stripTags.apply("<b>Joel</b> <button>is</button> a <span>slug</span>",
                        options));

        verify(options);
    }

    @Test
    public void stripTagsMultiLine() throws IOException {
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(false);

        replay(options);

        assertEquals("stripTags", stripTags.name());
        assertEquals("Joel\nis a slug",
                stripTags.apply(
                        "<b>Joel</b>\n<button>is<\n/button> a <span>slug</span>",
                        options));

        verify(options);
    }

    @Test
    public void capitalize() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("fully", false)).andReturn(false);
        expect(options.hash("fully", false)).andReturn(true);
        expect(options.isFalsy(anyObject())).andReturn(false).times(2);

        replay(options);

        assertEquals("capitalize", capitalize.name());

        assertEquals("Handlebars Java",
                capitalize.apply("handlebars java", options));

        assertEquals("Handlebars Java",
                capitalize.apply("HAndleBars JAVA", options));

        verify(options);
    }

    @Test
    public void abbreviate() throws IOException {
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(false);
        expect(options.param(0, null)).andReturn(13);

        replay(options);

        assertEquals("abbreviate", abbreviate.name());

        assertEquals("Handlebars...",
                abbreviate.apply("Handlebars.java", options));

        verify(options);
    }

  /*
  public void wordWrap() throws IOException {
    Options options = createMock(Options.class);
    expect(options.isFalsy(anyObject())).andReturn(false);
    expect(options.param(0, null)).andReturn(5);

    replay(options);

    assertEquals("wordWrap", wordWrap.name());

    assertEquals("Joel" + SystemUtils.LINE_SEPARATOR + "is a"
        + SystemUtils.LINE_SEPARATOR + "slug",
        wordWrap.apply("Joel is a slug", options));

    verify(options);
  }*/

    @Test
    public void yesno() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("yes", "yes")).andReturn("yes");
        expect(options.hash("no", "no")).andReturn("no");
        expect(options.hash("maybe", "maybe")).andReturn("maybe");

        replay(options);

        assertEquals("yesno", yesno.name());

        assertEquals("yes", yesno.apply(true, options));
        assertEquals("no", yesno.apply(false, options));
        assertEquals("maybe", yesno.apply(null, options));

        verify(options);
    }

    @Test
    public void yesnoCustom() throws IOException {
        Options options = createMock(Options.class);
        expect(options.hash("yes", "yes")).andReturn("yea");
        expect(options.hash("no", "no")).andReturn("nop");
        expect(options.hash("maybe", "maybe")).andReturn("whatever");

        replay(options);

        assertEquals("yesno", yesno.name());

        assertEquals("yea", yesno.apply(true, options));
        assertEquals("nop", yesno.apply(false, options));
        assertEquals("whatever", yesno.apply(null, options));

        verify(options);
    }

    @Test
    public void nullContext() throws IOException {
        Set<Helper<Object>> helpers = new LinkedHashSet<Helper<Object>>(Arrays.asList(StringHelpers
                .values()));
        helpers.remove(StringHelpers.yesno);
        helpers.remove(StringHelpers.defaultIfEmpty);

        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(true).times(helpers.size());
        expect(options.param(0, null)).andReturn(null).times(helpers.size());

        replay(options);

        for (Helper<Object> helper : helpers) {
            assertEquals(null, helper.apply($, options));
        }

        verify(options);
    }

    @Test
    public void nullContextWithDefault() throws IOException {
        Set<Helper<Object>> helpers = new LinkedHashSet<Helper<Object>>(Arrays.asList(StringHelpers
                .values()));
        helpers.remove(StringHelpers.yesno);
        helpers.remove(StringHelpers.defaultIfEmpty);

        String nothing = "nothing";
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(true).times(helpers.size());
        expect(options.param(0, null)).andReturn(nothing).times(helpers.size());

        replay(options);

        for (Helper<Object> helper : helpers) {
            assertEquals(nothing, helper.apply($, options));
        }

        verify(options);
    }

    @Test
    public void nullContextWithNumber() throws IOException {
        Set<Helper<Object>> helpers = new LinkedHashSet<Helper<Object>>(Arrays.asList(StringHelpers
                .values()));
        helpers.remove(StringHelpers.yesno);
        helpers.remove(StringHelpers.defaultIfEmpty);

        Object number = 32;
        Options options = createMock(Options.class);
        expect(options.isFalsy(anyObject())).andReturn(true).times(helpers.size());
        expect(options.param(0, null)).andReturn(number).times(helpers.size());

        replay(options);

        for (Helper<Object> helper : helpers) {
            assertEquals(number.toString(), helper.apply($, options));
        }

        verify(options);
    }
}
