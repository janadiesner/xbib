
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.util.LocaleUtil;
import org.xbib.template.handlebars.Options;

import java.io.IOException;
import java.util.Locale;

import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Implementation of i18n helper for Java.
 * <p>
 * The Java implementation use {@link java.util.ResourceBundle}.
 * </p>
 *
 * @see java.util.ResourceBundle
 */
/**
 * <p>
 * A helper built on top of {@link java.util.ResourceBundle}. A {@link java.util.ResourceBundle} is the most well
 * known mechanism for internationalization (i18n).
 * </p>
 * <p>
 * <h3>messages.properties:</h3>
 * </p>
 * <p/>
 * <pre>
 *  hello=Hola
 * </pre>
 * <p/>
 * <h3>Basic Usage:</h3>
 * <p/>
 * <pre>
 *  {{i18n "hello"}}
 * </pre>
 * <p/>
 * <p>
 * Will result in: <code>Hola</code>
 * </p>
 * <h3>Using a locale:</h3>
 * <p/>
 * <pre>
 *  {{i18n "hello" locale="es_AR"}}
 * </pre>
 * <p/>
 * <h3>Using a different bundle:</h3>
 * <p/>
 * <pre>
 *  {{i18n "hello" bundle="myMessages"}}
 * </pre>
 * <p/>
 * <h3>Using a message format:</h3>
 * <p/>
 * <pre>
 *  hello=Hola {0}!
 * </pre>
 * <p/>
 * <pre>
 *  {{i18n "hello" "Handlebars.java"}}
 * </pre>
 *
 * @see java.util.ResourceBundle
 */

public class I18nHelper implements Helper<String> {

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

    public I18nHelper(ClassLoader classLoader) {
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
     * A helper built on top of {@link java.util.ResourceBundle}. A {@link java.util.ResourceBundle} is the most well
     * known mechanism for internationalization (i18n).
     * </p>
     * <p>
     * <h3>messages.properties:</h3>
     * </p>
     *
     * <pre>
     *  hello=Hola
     * </pre>
     *
     * <h3>Basic Usage:</h3>
     *
     * <pre>
     *  {{i18n "hello"}}
     * </pre>
     *
     * <p>
     * Will result in: <code>Hola</code>
     * </p>
     * <h3>Using a locale:</h3>
     *
     * <pre>
     * {{i18n "hello" locale="es_AR"}}
     * </pre>
     *
     * <h3>Using a different bundle:</h3>
     *
     * <pre>
     *  {{i18n "hello" bundle="myMessages"}}
     * </pre>
     *
     * <h3>Using a message format</h3>
     *
     * <pre>
     *  hello=Hola {0}!
     * </pre>
     *
     * <pre>
     *  {{i18n "hello" "Handlebars.java"}}
     * </pre>
     *
     * @param key The bundle's key. Required.
     * @param options The helper's options. Not null.
     * @return An i18n message.
     * @throws java.io.IOException If the bundle wasn't resolve.
     */
    @Override
    public CharSequence apply(final String key, final Options options) throws IOException {
        // allow empty strings, transform them to empty strings
        if (key == null) {
            return "";
        }
        Locale locale = LocaleUtil.toLocale((String) options.hash("locale", defaultLocale.toString()));
        String baseName = options.hash("bundle", defaultBundle);
        I18nSource localSource = source == null
                ? new DefaultI18nSource(baseName, locale, classLoader) : source;

        return localSource.message(key, locale, options.params);
    }

}
