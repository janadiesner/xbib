package org.asynchttpclient;

import java.util.Map;
import java.util.Set;

/**
 * {@link org.asynchttpclient.AsyncHttpProvider} proprietary configurable properties. Note that properties are
 * <strong>AsyncHttpProvider</strong> dependent, so make sure you consult the AsyncHttpProvider's documentation
 * about what is supported and what's not.
 */
public interface AsyncHttpProviderConfig<U, V> {

    /**
     * Add a property that will be used when the AsyncHttpClient initialize its {@link org.asynchttpclient.AsyncHttpProvider}
     *
     * @param name  the name of the property
     * @param value the value of the property
     * @return this instance of AsyncHttpProviderConfig
     */
    public AsyncHttpProviderConfig<U,V> addProperty(U name, V value);

    /**
     * Return the value associated with the property's name
     *
     * @param name
     * @return this instance of AsyncHttpProviderConfig
     */
    public V getProperty(U name);

    /**
     * Remove the value associated with the property's name
     *
     * @param name
     * @return true if removed
     */
    public V removeProperty(U name);

    /**
     * Return the curent entry set.
     *
     * @return a the curent entry set.
     */
    public Set<Map.Entry<U, V>> propertiesSet();
}
