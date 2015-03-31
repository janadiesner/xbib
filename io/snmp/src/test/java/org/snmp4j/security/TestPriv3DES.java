package org.snmp4j.security;

import org.snmp4j.asn1.BER;
import org.snmp4j.smi.OctetString;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestPriv3DES extends Assert {
    static {
        BER.setCheckSequenceLength(false);
    }

    @Test
    public void testKey() {
        SecurityProtocols protos = SecurityProtocols.getInstance();
        protos.addDefaultProtocols();
        protos.addPrivacyProtocol(new Priv3DES());
        OctetString engineid = OctetString.fromHexString("00:00:00:00:00:00:00:00:00:00:00:02");
        OctetString password = new OctetString("maplesyrup");
        byte[] expectedKey =
                OctetString.fromHexString("52:6f:5e:ed:9f:cc:e2:6f:89:64:c2:93:07:87:d8:2b:79:ef:f4:4a:90:65:0e:e0:a3:a4:0a:bf:ac:5a:cc:12").toByteArray();
        byte[] key = protos.passwordToKey(Priv3DES.ID, AuthMD5.ID, password, engineid.toByteArray());

        for (int i = 0; i < expectedKey.length; i++) {
            assertEquals(expectedKey[i], key[i]);
        }
    }

    @Test
    public  void testEncrypt() {

        Priv3DES pd = new Priv3DES();
        DecryptParams pp = new DecryptParams();
        byte[] key = "12345678901234561234567890123456".getBytes();
        byte[] plaintext =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".
                        getBytes();
        byte[] ciphertext = null;
        byte[] decrypted = null;
        int engine_boots = 1;
        int engine_time = 2;

        ciphertext = pd.encrypt(plaintext, 0, plaintext.length, key, engine_boots, engine_time, pp);
        decrypted = pd.decrypt(ciphertext, 0, ciphertext.length, key, engine_boots, engine_time, pp);

        for (int i = 0; i < plaintext.length; i++) {
            assertEquals(plaintext[i], decrypted[i]);
        }
        assertEquals(8, pp.length);
    }

}
