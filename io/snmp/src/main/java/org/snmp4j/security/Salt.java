package org.snmp4j.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Class that holds a 64 bit salt value for crypto operations.
 * This class tries to use the SecureRandom class to initialize
 * the salt value. If SecureRandom is not available the class Random
 * is used.
 */
class Salt {
    private static final Logger logger = LogManager.getLogger(Salt.class);
    private static Salt instance = null;
    private long salt;

    /**
     * Default constructor, initializes the salt to a random value.
     */
    protected Salt() {
        byte[] rnd = new byte[8];

        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(rnd);
        } catch (NoSuchAlgorithmException nsae) {
            logger.warn("Could not use SecureRandom. Using Random instead.");
            Random r = new Random();
            r.nextBytes(rnd);
        }

        salt = rnd[0];

        for (int i = 0; i < 7; i++) {
            salt = (salt * 256) + ((int) rnd[i]) + 128;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Initialized Salt to " + Long.toHexString(salt) + ".");
        }
    }

    /**
     * Get a initialized Salt object.
     *
     * @return the Salt object
     */
    public static Salt getInstance() {
        if (instance == null) {
            instance = new Salt();
        }
        return instance;
    }

    /**
     * Get the next value of the salt.
     *
     * @return previous value increased by one.
     */
    public synchronized long getNext() {
        return salt++;
    }
}
