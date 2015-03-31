package org.snmp4j.security;

import org.snmp4j.smi.OctetString;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAuthSHA extends Assert {

    @Test
    public void testPasswordToKey1() {
        String password = "maplesyrup";
        byte[] engineId = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02};
        byte[] expectedKey = {
                (byte) 0x66, (byte) 0x95, (byte) 0xfe, (byte) 0xbc,
                (byte) 0x92, (byte) 0x88, (byte) 0xe3, (byte) 0x62,
                (byte) 0x82, (byte) 0x23, (byte) 0x5f, (byte) 0xc7,
                (byte) 0x15, (byte) 0x1f, (byte) 0x12, (byte) 0x84,
                (byte) 0x97, (byte) 0xb3, (byte) 0x8f, (byte) 0x3f};

        AuthSHA auth = new AuthSHA();
        try {
            byte[] key = auth.passwordToKey(new OctetString(password), engineId);
            assertEquals(expectedKey.length, key.length);
            for (int i = 0; i < key.length; i++) {
                assertEquals(key[i], expectedKey[i]);
            }
        } catch (Exception e) {
            System.err.println("Exception thrown:  " + e);
        }
    }

    @Test
    public void testPasswordToKey2() {
        String password = "newsyrup";
        byte[] engineId = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02};
        byte[] expectedKey = {
                (byte) 0x78, (byte) 0xe2, (byte) 0xdc, (byte) 0xce,
                (byte) 0x79, (byte) 0xd5, (byte) 0x94, (byte) 0x03,
                (byte) 0xb5, (byte) 0x8c, (byte) 0x1b, (byte) 0xba,
                (byte) 0xa5, (byte) 0xbf, (byte) 0xf4, (byte) 0x63,
                (byte) 0x91, (byte) 0xf1, (byte) 0xcd, (byte) 0x25};

        AuthSHA auth = new AuthSHA();
        try {
            byte[] key = auth.passwordToKey(new OctetString(password), engineId);
            assertEquals(expectedKey.length, key.length);
            for (int i = 0; i < key.length; i++) {
                assertEquals(expectedKey[i], key[i]);
            }
        } catch (Exception e) {
            System.err.println("Exception thrown:  " + e);
        }
    }

    @Test
    public void testChangeDelta() {
        String oldPass = "maplesyrup";
        String newPass = "newsyrup";
        byte[] oldKey;
        byte[] newKey;
        byte[] random = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        byte[] engineId = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02};
        byte[] expectedDelta = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x9c, (byte) 0x10, (byte) 0x17, (byte) 0xf4,
                (byte) 0xfd, (byte) 0x48, (byte) 0x3d, (byte) 0x2d,
                (byte) 0xe8, (byte) 0xd5, (byte) 0xfa, (byte) 0xdb,
                (byte) 0xf8, (byte) 0x43, (byte) 0x92, (byte) 0xcb,
                (byte) 0x06, (byte) 0x45, (byte) 0x70, (byte) 0x51};

        AuthSHA auth = new AuthSHA();
        try {
            oldKey = auth.passwordToKey(new OctetString(oldPass), engineId);
            newKey = auth.passwordToKey(new OctetString(newPass), engineId);
            byte[] delta = auth.changeDelta(oldKey, newKey, random);
            assertEquals(expectedDelta.length, delta.length);
            for (int i = 0; i < delta.length; i++) {
                assertEquals(delta[i], expectedDelta[i]);
            }
        } catch (Exception e) {
            System.err.println("Exception thrown:  " + e);
        }

    }
}
