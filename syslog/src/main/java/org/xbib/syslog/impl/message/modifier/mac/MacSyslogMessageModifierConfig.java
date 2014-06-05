package org.xbib.syslog.impl.message.modifier.mac;

import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.message.modifier.AbstractSyslogMessageModifierConfig;
import org.xbib.syslog.util.Base64;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * MacSyslogMessageModifierConfig is an implementation of AbstractSyslogMessageModifierConfig
 * that provides configuration for HashSyslogMessageModifier.
 */
public class MacSyslogMessageModifierConfig extends AbstractSyslogMessageModifierConfig {

    protected String macAlgorithm = null;
    protected String keyAlgorithm = null;
    protected Key key = null;

    public MacSyslogMessageModifierConfig(String macAlgorithm, String keyAlgorithm, Key key) {
        this.macAlgorithm = macAlgorithm;
        this.keyAlgorithm = keyAlgorithm;
        this.key = key;
    }

    public MacSyslogMessageModifierConfig(String macAlgorithm, String keyAlgorithm, byte[] keyBytes) {
        this.macAlgorithm = macAlgorithm;
        this.keyAlgorithm = keyAlgorithm;

        try {
            this.key = new SecretKeySpec(keyBytes, keyAlgorithm);

        } catch (IllegalArgumentException iae) {
            throw new SyslogRuntimeException(iae);
        }
    }

    public MacSyslogMessageModifierConfig(String macAlgorithm, String keyAlgorithm, String base64Key) {
        this.macAlgorithm = macAlgorithm;
        this.keyAlgorithm = keyAlgorithm;

        byte[] keyBytes = Base64.decode(base64Key);

        try {
            this.key = new SecretKeySpec(keyBytes, keyAlgorithm);

        } catch (IllegalArgumentException iae) {
            throw new SyslogRuntimeException(iae);
        }
    }

    public static MacSyslogMessageModifierConfig createHmacSHA1(Key key) {
        return new MacSyslogMessageModifierConfig("HmacSHA1", "SHA1", key);
    }

    public static MacSyslogMessageModifierConfig createHmacSHA1(String base64Key) {
        return new MacSyslogMessageModifierConfig("HmacSHA1", "SHA1", base64Key);
    }

    public static MacSyslogMessageModifierConfig createHmacSHA256(Key key) {
        return new MacSyslogMessageModifierConfig("HmacSHA256", "SHA-256", key);
    }

    public static MacSyslogMessageModifierConfig createHmacSHA256(String base64Key) {
        return new MacSyslogMessageModifierConfig("HmacSHA256", "SHA-256", base64Key);
    }

    public static MacSyslogMessageModifierConfig createHmacSHA512(Key key) {
        return new MacSyslogMessageModifierConfig("HmacSHA512", "SHA-512", key);
    }

    public static MacSyslogMessageModifierConfig createHmacSHA512(String base64Key) {
        return new MacSyslogMessageModifierConfig("HmacSHA512", "SHA-512", base64Key);
    }

    public static MacSyslogMessageModifierConfig createHmacMD5(Key key) {
        return new MacSyslogMessageModifierConfig("HmacMD5", "MD5", key);
    }

    public static MacSyslogMessageModifierConfig createHmacMD5(String base64Key) {
        return new MacSyslogMessageModifierConfig("HmacMD5", "MD5", base64Key);
    }

    public String getMacAlgorithm() {
        return this.macAlgorithm;
    }

    public String getKeyAlgorithm() {
        return this.keyAlgorithm;
    }

    public Key getKey() {
        return this.key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}
