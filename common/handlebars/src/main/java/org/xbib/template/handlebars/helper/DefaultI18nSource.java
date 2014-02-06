package org.xbib.template.handlebars.helper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.xbib.template.handlebars.util.Validate.isTrue;

/**
 * Default implementation of I18NSource.
 */
class DefaultI18nSource implements I18nSource {

    /**
     * The resource bundle.
     */
    private ResourceBundle bundle;

    /**
     * Creates a new {@link DefaultI18nSource}.
     *
     * @param baseName    The base name.
     * @param locale      The locale.
     * @param classLoader The classloader.
     */
    public DefaultI18nSource(final String baseName, final Locale locale, final ClassLoader classLoader) {
        bundle = ResourceBundle.getBundle(baseName, locale, classLoader);
    }

    @Override
    public String[] keys(final String basename, final Locale locale) {
        Enumeration<String> keys = bundle.getKeys();
        List<String> result = new ArrayList<String>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            result.add(key);
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public String message(final String key, final Locale locale, final Object... args) {
        isTrue(bundle.containsKey(key), "no message found: '%s' for locale '%s'.", key, locale);
        String message = bundle.getString(key);
        if (args.length == 0) {
            return message;
        }
        MessageFormat format = new MessageFormat(message, locale);
        return format.format(args);
    }

}