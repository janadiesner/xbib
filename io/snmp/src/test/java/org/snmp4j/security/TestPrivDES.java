package org.snmp4j.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.smi.OctetString;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestPrivDES extends Assert {

    private final static Logger cat = LogManager.getLogger(TestPrivDES.class.getName());

    public static String asHex(byte buf[]) {
        return new OctetString(buf).toHexString();
    }

    @Test
    public static void testEncrypt() {
        PrivDES pd = new PrivDES();
        DecryptParams pp = new DecryptParams();
        byte[] key = "1234567890123456".getBytes();
        byte[] plaintext =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".
                        getBytes();
        byte[] ciphertext = null;
        byte[] decrypted = null;
        int engine_boots = 1;
        int engine_time = 2;

        cat.debug("Cleartext: " + asHex(plaintext));
        ciphertext = pd.encrypt(plaintext, 0, plaintext.length, key, engine_boots, engine_time, pp);
        cat.debug("Encrypted: " + asHex(ciphertext));
        decrypted = pd.decrypt(ciphertext, 0, ciphertext.length, key, engine_boots, engine_time, pp);
        cat.debug("Cleartext: " + asHex(decrypted));

        for (int i = 0; i < plaintext.length; i++) {
            assertEquals(plaintext[i], decrypted[i]);
        }
        cat.info("pp length is: " + pp.length);
        assertEquals(8, pp.length);
    }

}
