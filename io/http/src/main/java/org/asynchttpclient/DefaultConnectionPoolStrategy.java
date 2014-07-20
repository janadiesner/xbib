package org.asynchttpclient;

import java.net.URI;

import org.asynchttpclient.util.AsyncHttpProviderUtils;

public enum DefaultConnectionPoolStrategy implements ConnectionPoolKeyStrategy {

	INSTANCE;
	
	@Override
	public String getKey(URI uri) {
		return AsyncHttpProviderUtils.getBaseUrl(uri);
	}
}
