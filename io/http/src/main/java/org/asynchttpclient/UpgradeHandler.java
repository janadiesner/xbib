package org.asynchttpclient;

/**
 * Invoked when an {@link org.asynchttpclient.AsyncHandler.STATE#UPGRADE} is returned. Currently the library only support {@link org.asynchttpclient.websocket.WebSocket}
 * as type.
 *
 * @param <T>
 */
public interface UpgradeHandler<T> {

    /**
     * If the HTTP Upgrade succeed (response's status code equals 101), the {@link org.asynchttpclient.AsyncHttpProvider} will invoke that
     * method
     *
     * @param t an Upgradable entity
     */
    void onSuccess(T t);

    /**
     * If the upgrade fail.
     * @param t a {@link Throwable}
     */
    void onFailure(Throwable t);

}
