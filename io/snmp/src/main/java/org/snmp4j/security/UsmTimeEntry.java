package org.snmp4j.security;

import org.snmp4j.smi.OctetString;

/**
 * The <code>UsmTimeEntry</code> class represents time synchronization
 * information associated with an engine ID.
 */
public class UsmTimeEntry {

    private OctetString engineID;
    private int engineBoots;
    private int timeDiff;
    private int latestReceivedTime;

    /**
     * Creates a time entry with engine ID, engine boots and time.
     *
     * @param engineID    the engine ID for which time synchronization information is created.
     * @param engineBoots the number of engine boots of the engine.
     * @param engineTime  the time in seconds elapsed since the last reboot of the engine.
     */
    public UsmTimeEntry(OctetString engineID, int engineBoots, int engineTime) {
        this.engineID = engineID;
        this.engineBoots = engineBoots;
        setEngineTime(engineTime);
    }

    public OctetString getEngineID() {
        return engineID;
    }

    public int getEngineBoots() {
        return engineBoots;
    }

    public void setEngineBoots(int engineBoots) {
        this.engineBoots = engineBoots;
    }

    public int getTimeDiff() {
        return timeDiff;
    }

    public void setTimeDiff(int timeDiff) {
        this.timeDiff = timeDiff;
    }

    /**
     * Gets the time when a message has been received last from the associated
     * SNMP engine.
     *
     * @return the engine time in seconds.
     */
    public int getLatestReceivedTime() {
        return latestReceivedTime;
    }

    /**
     * Sets the time when a message has been received last from the associated
     * SNMP engine.
     *
     * @param latestReceivedTime the engine time in seconds.
     */
    public void setLatestReceivedTime(int latestReceivedTime) {
        this.latestReceivedTime = latestReceivedTime;
    }

    /**
     * Sets the engine time which also sets the last received engine time
     * to the supplied value.
     *
     * @param engineTime the time in seconds elapsed since the last reboot of the engine.
     */
    public void setEngineTime(int engineTime) {
        this.latestReceivedTime = engineTime;
        this.timeDiff = engineTime - (int) (System.nanoTime() / UsmTimeTable.TIME_PRECISION);
    }
}
