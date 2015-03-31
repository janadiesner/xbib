package org.snmp4j.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.smi.OctetString;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestPrivAES extends Assert {

    private final static Logger cat = LogManager.getLogger(TestPrivAES.class);

    public static String asHex(byte buf[]) {
        return new OctetString(buf).toHexString();
    }

    @Test
    public void testCrypt() {
        SecurityProtocols secProts = SecurityProtocols.getInstance();
        secProts.addDefaultProtocols();

        PrivAES128 pd = new PrivAES128();
        DecryptParams pp = new DecryptParams();
        byte[] key = {
                (byte) 0x66, (byte) 0x95, (byte) 0xfe, (byte) 0xbc,
                (byte) 0x92, (byte) 0x88, (byte) 0xe3, (byte) 0x62,
                (byte) 0x82, (byte) 0x23, (byte) 0x5f, (byte) 0xc7,
                (byte) 0x15, (byte) 0x1f, (byte) 0x12, (byte) 0x84
        };
        byte[] plaintext = "This is a secret message, nobody is allowed to read it!".
                getBytes();
        byte[] ciphertext = null;
        byte[] decrypted = null;
        int engine_boots = 0xdeadc0de;
        int engine_time = 0xbeefdede;

        cat.debug("Cleartext: " + asHex(plaintext));
        ciphertext = pd.encrypt(plaintext, 0, plaintext.length, key, engine_boots,
                engine_time, pp);
        cat.debug("Encrypted: " + asHex(ciphertext));
        decrypted = pd.decrypt(ciphertext, 0, ciphertext.length, key, engine_boots, engine_time, pp);
        cat.debug("Cleartext: " + asHex(decrypted));

        assertEquals(asHex(plaintext), asHex(decrypted));

        cat.info("pp length is: " + pp.length);
        assertEquals(8, pp.length);
    }

    @Test
    public void testAesKeyExtension() {
        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA());
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES256());
        byte[] key =
                SecurityProtocols.getInstance().passwordToKey(PrivAES256.ID, AuthSHA.ID, new OctetString("maplesyrup"),
                        new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0,
                                (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                                (byte) 0, (byte) 0, (byte) 0, (byte) 2});
        assertEquals("66:95:fe:bc:92:88:e3:62:82:23:5f:c7:15:1f:12:84:97:b3:8f:3f:50:5e:07:eb:9a:f2:55:68:fa:1f:5d:be",
                new OctetString(key).toHexString());
    }
}
