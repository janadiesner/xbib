package org.asynchttpclient.util;

import static org.asynchttpclient.util.MiscUtil.isNonEmpty;

import org.asynchttpclient.ProxyServer;
import org.asynchttpclient.Realm;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public final class AuthenticatorUtils {

    public static String computeBasicAuthentication(Realm realm) throws UnsupportedEncodingException {
        String s = realm.getPrincipal() + ":" + realm.getPassword();
        return "Basic " + Base64.encode(s.getBytes(realm.getEncoding()));
    }

    public static String computeBasicAuthentication(ProxyServer proxyServer) throws UnsupportedEncodingException {
        String s = proxyServer.getPrincipal() + ":" + proxyServer.getPassword();
        return "Basic " + Base64.encode(s.getBytes(proxyServer.getEncoding()));
    }

    public static String computeDigestAuthentication(Realm realm) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        StringBuilder builder = new StringBuilder().append("Digest ");
        construct(builder, "username", realm.getPrincipal());
        construct(builder, "realm", realm.getRealmName());
        construct(builder, "nonce", realm.getNonce());
        construct(builder, "uri", realm.getUri());
        builder.append("algorithm").append('=').append(realm.getAlgorithm()).append(", ");

        construct(builder, "response", realm.getResponse());
        if (isNonEmpty(realm.getOpaque()))
            construct(builder, "opaque", realm.getOpaque());
        builder.append("qop").append('=').append(realm.getQop()).append(", ");
        builder.append("nc").append('=').append(realm.getNc()).append(", ");
        construct(builder, "cnonce", realm.getCnonce(), true);

        return new String(builder.toString().getBytes("ISO_8859_1"));
    }

	public static String computeDigestAuthentication(ProxyServer proxy) {
		try{
	        StringBuilder builder = new StringBuilder().append("Digest ");
	        construct(builder, "username", proxy.getPrincipal(),true);
	        return new String(builder.toString().getBytes("ISO_8859_1"));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

    private static StringBuilder construct(StringBuilder builder, String name, String value) {
        return construct(builder, name, value, false);
    }

    private static StringBuilder construct(StringBuilder builder, String name, String value, boolean tail) {
        return builder.append(name).append('=').append('"').append(value).append(tail ? "\"" : "\", ");
    }
}
