
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Options;
import org.xbib.template.handlebars.util.LocaleUtil;
import org.xbib.template.handlebars.util.StringUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Implementation of i18n helper for Java and JavaScript.
 * <p>
 * The Java implementation use {@link java.util.ResourceBundle}.
 * </p>
 * <p>
 * The JavaScript version use the <a href="https://github.com/fnando/i18n-js">I18n</a> library.
 * {@link java.util.ResourceBundle} are converted to JavaScript code.
 * </p>
 *
 * @see java.util.ResourceBundle
 */
public class I18nJSHelper implements Helper<String> {

    private final ClassLoader classLoader;

    /**
     * The default locale. Required.
     */
    protected Locale defaultLocale = Locale.getDefault();

    /**
     * The default's bundle. Required.
     */
    protected String defaultBundle = "messages";

    /**
     * The message source to use.
     */
    protected I18nSource source;

    /**
     * The message format pattern.
     */
    private final Pattern pattern = Pattern.compile("\\{(\\d+)\\}");

    public I18nJSHelper(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Set the message source.
     *
     * @param source The message source. Required.
     */
    public void setSource(final I18nSource source) {
        this.source = notNull(source, "The i18n source is required.");
    }

    /**
     * <p>
     * Translate a {@link java.util.ResourceBundle} into JavaScript code. The generated code assume you added
     * the <a href="https://github.com/fnando/i18n-js">I18n</a>
     * </p>
     * <p>
     * It converts message patterns like: <code>Hi {0}</code> into <code>Hi {{arg0}}</code>. This
     * make possible to the I18n JS library to interpolate variables.
     * </p>
     * <p>
     * Note: make sure you include <a href="https://github.com/fnando/i18n-js">I18n</a> in your
     * application. Otherwise, the generated code will fail.
     * </p>
     * <p>
     * Usage:
     * </p>
     *
     * <pre>
     *  {{i18nJs [locale] [bundle=messages] [wrap=true]}}
     * </pre>
     *
     * If locale argument is present it will translate that locale to JavaScript. Otherwise, the
     * default locale.
     *
     * Use wrap=true for wrapping the code with a script tag.
     *
     * @param localeName The locale's name. Optional.
     * @param options The helper's options. Not null.
     * @return JavaScript code from {@link java.util.ResourceBundle}.
     * @throws java.io.IOException If bundle wasn't resolve.
     */
    @Override
    public CharSequence apply(final String localeName, final Options options) throws IOException {
        Locale locale = LocaleUtil.toLocale(localeName != null ? localeName : defaultLocale.toString());
        String baseName = options.hash("bundle", defaultBundle);
        I18nSource localSource = source == null
                ? new DefaultI18nSource(baseName, locale, classLoader) : source;
        StringBuilder buffer = new StringBuilder();
        Boolean wrap = options.hash("wrap", true);
        if (wrap) {
            buffer.append("<script type='text/javascript'>\n");
        }
        buffer.append("  // ").append(locale.getDisplayName()).append("\n");
        buffer.append("  I18n.translations = I18n.translations || {};\n");
        buffer.append("  I18n.translations['").append(locale.toString()).append("'] = {\n");
        StringBuilder body = new StringBuilder();
        String separator = ",\n";
        String[] keys = localSource.keys(baseName, locale);
        for (String key : keys) {
            String message = message(localSource.message(key, locale));
            body.append("    \"").append(key).append("\": ");
            body.append("\"").append(message).append("\"").append(separator);
        }
        if (body.length() > 0) {
            body.setLength(body.length() - separator.length());
            buffer.append(body);
        }
        buffer.append("\n  };\n");
        if (wrap) {
            buffer.append("</script>\n");
        }
        return new Handlebars.SafeString(buffer);
    }

    /**
     * Convert expression <code>{0}</code> into <code>{{arg0}}</code> and escape EcmaScript
     * characters.
     *
     * @param message The candidate message.
     * @return A valid I18n message.
     */
    private String message(final String message) {
        String escapedMessage = //Handlebars.Utils.escapeExpression(message);
                StringUtil.htmlEscape(message);
        Matcher matcher = pattern.matcher(escapedMessage);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, "{{arg" + matcher.group(1) + "}}");
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
