package org.snmp4j.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.smi.OctetString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The <code>UsmUserTable</code> class stores USM user
 * information as part of the Local Configuration Datastore (LCD).
 */
public class UsmUserTable {

    private static final Logger logger = LogManager.getLogger(UsmUserTable.class);

    private Map<UsmUserKey, UsmUserEntry> table = new TreeMap<UsmUserKey, UsmUserEntry>();

    public UsmUserTable() {
    }

    public synchronized UsmUserEntry addUser(UsmUserEntry user) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding user " + user.getUserName() + " = " + user.getUsmUser());
        }
        return table.put(new UsmUserKey(user), user);
    }

    public synchronized void setUsers(Collection<UsmUserEntry> c) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting users to " + c);
        }
        table.clear();
        for (UsmUserEntry user : c) {
            table.put(new UsmUserKey(user), user);
        }
    }

    /**
     * Gets all user entries with the supplied user name.
     *
     * @param userName an <code>OctetString</code> denoting the user name.
     * @return a possibly empty <code>List</code> containing all user entries with
     * the specified <code>userName</code>.
     */
    public synchronized List<UsmUserEntry> getUserEntries(OctetString userName) {
        LinkedList<UsmUserEntry> users = new LinkedList<UsmUserEntry>();
        for (UsmUserEntry value : table.values()) {
            if (userName.equals(value.getUserName())) {
                users.add(value);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning user entries for " + userName + " = " + users);
        }
        return users;
    }

    public synchronized List<UsmUserEntry> getUserEntries() {
        LinkedList<UsmUserEntry> l = new LinkedList<UsmUserEntry>();
        for (UsmUserEntry usmUserEntry : table.values()) {
            l.add(usmUserEntry);
        }
        return l;
    }

    public synchronized List<UsmUserEntry> removeAllUsers(OctetString securityName, OctetString engineID) {
        if (engineID == null) {
            List<UsmUserEntry> deleted = new ArrayList<UsmUserEntry>();
            for (Iterator<UsmUserEntry> usmUserEntryIterator = table.values().iterator(); usmUserEntryIterator.hasNext(); ) {
                UsmUserEntry usmUserEntry = usmUserEntryIterator.next();
                if (securityName.equals(usmUserEntry.getUsmUser().getSecurityName())) {
                    deleted.add(usmUserEntry);
                    usmUserEntryIterator.remove();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removed user " + usmUserEntry);
                    }
                }
            }
            return deleted;
        }
        UsmUserEntry entry =
                table.remove(new UsmUserKey(engineID, securityName));
        if (logger.isDebugEnabled()) {
            logger.debug("Removed user with secName=" + securityName +
                    " and engineID=" + engineID);
        }
        return (entry != null) ? Collections.singletonList(entry) : Collections.<UsmUserEntry>emptyList();
    }

    public synchronized UsmUserEntry removeUser(OctetString engineID,
                                                OctetString securityName) {
        UsmUserEntry entry =
                table.remove(new UsmUserKey(engineID, securityName));
        if (logger.isDebugEnabled()) {
            logger.debug("Removed user with secName=" + securityName +
                    " and engineID=" + engineID);
        }
        return entry;
    }

    public synchronized UsmUserEntry getUser(OctetString engineID,
                                             OctetString securityName) {
        return table.get(new UsmUserKey(engineID, securityName));
    }

    public synchronized UsmUserEntry getUser(OctetString securityName) {
        return table.get(new UsmUserKey(new OctetString(), securityName));
    }

    public synchronized void clear() {
        table.clear();
        if (logger.isDebugEnabled()) {
            logger.debug("Cleared UsmUserTable");
        }
    }

    public static class UsmUserKey implements Comparable {
        OctetString engineID;
        OctetString securityName;

        public UsmUserKey(UsmUserEntry entry) {
            setEngineID(entry.getEngineID());
            this.securityName = entry.getUsmUser().getSecurityName();
        }

        public UsmUserKey(OctetString engineID, OctetString securityName) {
            setEngineID(engineID);
            this.securityName = securityName;
        }

        private void setEngineID(OctetString engineID) {
            if (engineID == null) {
                this.engineID = new OctetString();
            } else {
                this.engineID = engineID;
            }
        }

        public int hashCode() {
            return engineID.hashCode() ^ 2 + securityName.hashCode();
        }

        public boolean equals(Object o) {
            if ((o instanceof UsmUserEntry) || (o instanceof UsmUserKey)) {
                return (compareTo(o) == 0);
            }
            return false;
        }

        public int compareTo(Object o) {
            if (o instanceof UsmUserEntry) {
                return compareTo(new UsmUserKey((UsmUserEntry) o));
            }
            UsmUserKey other = (UsmUserKey) o;
            int result = 0;
            if ((engineID != null) && (other.engineID != null)) {
                result = engineID.compareTo(other.engineID);
            } else if (engineID != null) {
                result = 1;
            } else if (other.engineID != null) {
                result = -1;
            }
            if (result == 0) {
                result = securityName.compareTo(other.securityName);
            }
            return result;
        }
    }
}

