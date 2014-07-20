package org.asynchttpclient;

import java.net.URI;

public interface ConnectionPoolKeyStrategy {

	String getKey(URI uri);
}
