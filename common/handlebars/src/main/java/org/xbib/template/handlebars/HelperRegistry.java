
package org.xbib.template.handlebars;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The helper registry.
 */
public interface HelperRegistry {

    /**
     * The missing helper's name.
     */
    String HELPER_MISSING = "helperMissing";

    /**
     * Find a helper by name.
     *
     * @param <C>  The helper runtime type.
     * @param name The helper's name. Required.
     * @return A helper or null if it's not found.
     */
    <C> Helper<C> helper(String name);

    /**
     * List all the helpers from registry.
     *
     * @return Available helpers in the registry.
     */
    Set<Entry<String, Helper<?>>> helpers();

    /**
     * Register a helper in the helper registry.
     *
     * @param <H>    The helper runtime type.
     * @param name   The helper's name. Required.
     * @param helper The helper object. Required.
     * @return This handlebars.
     */
    <H> HelperRegistry registerHelper(String name, Helper<H> helper);

    /**
     * <p>
     * Register all the helper methods for the given helper source.
     * </p>
     * <p>
     * A helper method looks like:
     * </p>
     * <p/>
     * <pre>
     * public static? CharSequence methodName(context?, parameter*, options?) {
     * }
     * </pre>
     * <p/>
     * Where:
     * <ul>
     * <li>A method can/can't be static</li>
     * <li>The method's name became the helper's name</li>
     * <li>Context, parameters and options are all optional</li>
     * <li>If context and options are present they must be the first and last method arguments.</li>
     * </ul>
     * <p/>
     * Instance and static methods will be registered as helpers.
     *
     * @param helperSource The helper source. Required.
     * @return This handlebars object.
     */
    HelperRegistry registerHelpers(Object helperSource);

    /**
     * <p>
     * Register all the helper methods for the given helper source.
     * </p>
     * <p>
     * A helper method looks like:
     * </p>
     * <p/>
     * <pre>
     * public static? CharSequence methodName(context?, parameter*, options?) {
     * }
     * </pre>
     * <p/>
     * Where:
     * <ul>
     * <li>A method can/can't be static</li>
     * <li>The method's name became the helper's name</li>
     * <li>Context, parameters and options are all optional</li>
     * <li>If context and options are present they must be the first and last method arguments.</li>
     * </ul>
     * <p/>
     * Only static methods will be registered as helpers.
     * <p>Enums are supported too</p>
     *
     * @param helperSource The helper source. Enums are supported. Required.
     * @return This handlebars object.
     */
    HelperRegistry registerHelpers(Class<?> helperSource);

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param location A classpath location. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    HelperRegistry registerHelpers(URI location) throws Exception;

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param input A JavaScript file name. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    HelperRegistry registerHelpers(File input) throws Exception;

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param filename The file name (just for debugging purpose). Required.
     * @param source   The JavaScript source. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    HelperRegistry registerHelpers(String filename, Reader source) throws Exception;

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param filename The file name (just for debugging purpose). Required.
     * @param source   The JavaScript source. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    HelperRegistry registerHelpers(String filename, InputStream source)
            throws Exception;

    /**
     * <p>
     * Register helpers from a JavaScript source.
     * </p>
     * <p>
     * A JavaScript source file looks like:
     * </p>
     * <p/>
     * <pre>
     *  Handlebars.registerHelper('hey', function (context) {
     *    return 'Hi ' + context.name;
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, options) {
     *    return 'Hi ' + context.name + options.hash['x'];
     *  });
     *  ...
     *  Handlebars.registerHelper('hey', function (context, p1, p2, options) {
     *    return 'Hi ' + context.name + p1 + p2 + options.hash['x'];
     *  });
     *  ...
     * </pre>
     * <p/>
     * To keep your helpers reusable between server and client avoid DOM manipulation.
     *
     * @param filename The file name (just for debugging purpose). Required.
     * @param source   The JavaScript source. Required.
     * @return This handlebars object.
     * @throws Exception If the JavaScript helpers can't be registered.
     */
    HelperRegistry registerHelpers(String filename, String source) throws Exception;

}
