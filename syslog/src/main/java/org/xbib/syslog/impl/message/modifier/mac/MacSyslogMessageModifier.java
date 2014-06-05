package org.xbib.syslog.impl.message.modifier.mac;

import org.xbib.syslog.SyslogIF;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.message.modifier.AbstractSyslogMessageModifier;
import org.xbib.syslog.util.Base64;
import org.xbib.syslog.util.SyslogUtility;

import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * MacSyslogMessageModifier is an implementation of SyslogMessageModifierIF
 * that provides support for Java Cryptographic signed hashes (HmacSHA1, etc.)
 */
public class MacSyslogMessageModifier extends AbstractSyslogMessageModifier {

    protected MacSyslogMessageModifierConfig config = null;

    protected Mac mac = null;

    public MacSyslogMessageModifier(MacSyslogMessageModifierConfig config) throws SyslogRuntimeException {
        super(config);

        this.config = config;

        try {
            this.mac = Mac.getInstance(config.getMacAlgorithm());
            this.mac.init(config.getKey());

        } catch (NoSuchAlgorithmException nsae) {
            throw new SyslogRuntimeException(nsae);

        } catch (InvalidKeyException ike) {
            throw new SyslogRuntimeException(ike);
        }
    }

    public static MacSyslogMessageModifier createHmacSHA1(Key key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacSHA1(key));
    }

    public static MacSyslogMessageModifier createHmacSHA1(String base64Key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacSHA1(base64Key));
    }

    public static MacSyslogMessageModifier createHmacSHA256(Key key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacSHA256(key));
    }

    public static MacSyslogMessageModifier createHmacSHA256(String base64Key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacSHA256(base64Key));
    }

    public static MacSyslogMessageModifier createHmacSHA512(Key key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacSHA512(key));
    }

    public static MacSyslogMessageModifier createHmacSHA512(String base64Key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacSHA512(base64Key));
    }

    public static MacSyslogMessageModifier createHmacMD5(Key key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacMD5(key));
    }

    public static MacSyslogMessageModifier createHmacMD5(String base64Key) {
        return new MacSyslogMessageModifier(MacSyslogMessageModifierConfig.createHmacMD5(base64Key));
    }

    public MacSyslogMessageModifierConfig getConfig() {
        return this.config;
    }

    public String modify(SyslogIF syslog, int facility, int level, String message) {
        synchronized (this.mac) {
            byte[] messageBytes = SyslogUtility.getBytes(syslog.getConfig(), message);

            StringBuffer buffer = new StringBuffer(message);

            byte[] macBytes = this.mac.doFinal(messageBytes);

            String macString = Base64.encodeBytes(macBytes, Base64.DONT_BREAK_LINES);

            buffer.append(this.config.getPrefix());
            buffer.append(macString);
            buffer.append(this.config.getSuffix());

            return buffer.toString();
        }
    }

    public boolean verify(String message, String base64Signature) {
        byte[] signature = Base64.decode(base64Signature);

        return verify(message, signature);
    }

    public boolean verify(String message, byte[] signature) {
        synchronized (this.mac) {
            byte[] messageBytes = SyslogUtility.getBytes(this.config, message);

            byte[] macBytes = this.mac.doFinal(messageBytes);

            return Arrays.equals(macBytes, signature);
        }
    }
}
