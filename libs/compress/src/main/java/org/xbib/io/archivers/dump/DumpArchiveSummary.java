/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.io.archivers.dump;

import java.util.Date;


/**
 * This class represents identifying information about a Dump archive volume.
 * It consists the archive's dump date, label, hostname, device name and possibly
 * last mount point plus the volume's volume id andfirst record number.
 *
 * For the corresponding C structure see the header of {@link DumpArchiveEntry}.
 */
public class DumpArchiveSummary {
    private long dumpDate;
    private long previousDumpDate;
    private int volume;
    private String label;
    private int level;
    private String filesys;
    private String devname;
    private String hostname;
    private int flags;
    private int firstrec;
    private int ntrec;

    DumpArchiveSummary(byte[] buffer) {
        dumpDate = 1000L * DumpArchiveUtil.convert32(buffer, 4);
        previousDumpDate = 1000L * DumpArchiveUtil.convert32(buffer, 8);
        volume = DumpArchiveUtil.convert32(buffer, 12);
        label = new String(buffer, 676, DumpArchiveConstants.LBLSIZE).trim(); // TODO default charset?
        level = DumpArchiveUtil.convert32(buffer, 692);
        filesys = new String(buffer, 696, DumpArchiveConstants.NAMELEN).trim(); // TODO default charset?
        devname = new String(buffer, 760, DumpArchiveConstants.NAMELEN).trim(); // TODO default charset?
        hostname = new String(buffer, 824, DumpArchiveConstants.NAMELEN).trim(); // TODO default charset?
        flags = DumpArchiveUtil.convert32(buffer, 888);
        firstrec = DumpArchiveUtil.convert32(buffer, 892);
        ntrec = DumpArchiveUtil.convert32(buffer, 896);

        //extAttributes = DumpArchiveUtil.convert32(buffer, 900);
    }

    /**
     * Get the date of this dump.
     * @return the date of this dump.
     */
    public Date getDumpDate() {
        return new Date(dumpDate);
    }

    /**
     * Set dump date.
     */
    public void setDumpDate(Date dumpDate) {
        this.dumpDate = dumpDate.getTime();
    }

    /**
     * Get the date of the previous dump at this level higher.
     * @return dumpdate may be null
     */
    public Date getPreviousDumpDate() {
        return new Date(previousDumpDate);
    }

    /**
     * Set previous dump date.
     */
    public void setPreviousDumpDate(Date previousDumpDate) {
        this.previousDumpDate = previousDumpDate.getTime();
    }

    /**
     * Get volume (tape) number.
     * @return volume (tape) number.
     */
    public int getVolume() {
        return volume;
    }

    /**
     * Set volume (tape) number.
     */
    public void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * Get the level of this dump. This is a number between 0 and 9, inclusive,
     * and a level 0 dump is a complete dump of the partition. For any other dump
     * 'n' this dump contains all files that have changed since the last dump
     * at this level or lower. This is used to support different levels of
     * incremental backups.
     * @return dump level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set level.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Get dump label. This may be autogenerated or it may be specified
     * bu the user.
     * @return dump label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set dump label.
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the last mountpoint, e.g., /home.
     * @return last mountpoint
     */
    public String getFilesystem() {
        return filesys;
    }

    /**
     * Set the last mountpoint.
     */
    public void setFilesystem(String filesystem) {
        this.filesys = filesystem;
    }

    /**
     * Get the device name, e.g., /dev/sda3 or /dev/mapper/vg0-home.
     * @return device name
     */
    public String getDevname() {
        return devname;
    }

    /**
     * Set the device name.
     * @param devname
     */
    public void setDevname(String devname) {
        this.devname = devname;
    }

    /**
     * Get the hostname of the system where the dump was performed.
     * @return hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the hostname.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Get the miscellaneous flags. See below.
     * @return flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Set the miscellaneous flags.
     * @param flags
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Get the inode of the first record on this volume.
     * @return inode of the first record on this volume.
     */
    public int getFirstRecord() {
        return firstrec;
    }

    /**
     * Set the inode of the first record.
     * @param firstrec
     */
    public void setFirstRecord(int firstrec) {
        this.firstrec = firstrec;
    }

    /**
     * Get the number of records per tape block. This is typically
     * between 10 and 32.
     * @return the number of records per tape block
     */
    public int getNTRec() {
        return ntrec;
    }

    /**
     * Set the number of records per tape block.
     */
    public void setNTRec(int ntrec) {
        this.ntrec = ntrec;
    }

    /**
     * Is this the new header format? (We do not currently support the
     * old format.)
     *
     * @return true if using new header format
     */
    public boolean isNewHeader() {
        return (flags & 0x0001) == 0x0001;
    }

    /**
     * Is this the new inode format? (We do not currently support the
     * old format.)
     * @return true if using new inode format
     */
    public boolean isNewInode() {
        return (flags & 0x0002) == 0x0002;
    }

    /**
     * Is this volume compressed? N.B., individual blocks may or may not be compressed.
     * The first block is never compressed.
     * @return true if volume is compressed
     */
    public boolean isCompressed() {
        return (flags & 0x0080) == 0x0080;
    }

    /**
     * Does this volume only contain metadata?
     * @return true if volume only contains meta-data
     */
    public boolean isMetaDataOnly() {
        return (flags & 0x0100) == 0x0100;
    }

    /**
     * Does this volume cotain extended attributes.
     * @return true if volume cotains extended attributes.
     */
    public boolean isExtendedAttributes() {
        return (flags & 0x8000) == 0x8000;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 17;

        if (label != null) {
            hash = label.hashCode();
        }

        hash += 31 * dumpDate;

        if (hostname != null) {
            hash = (31 * hostname.hashCode()) + 17;
        }

        if (devname != null) {
            hash = (31 * devname.hashCode()) + 17;
        }

        return hash;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }

        DumpArchiveSummary rhs = (DumpArchiveSummary) o;

        if (dumpDate != rhs.dumpDate) {
            return false;
        }

        if ((getHostname() == null) ||
                !getHostname().equals(rhs.getHostname())) {
            return false;
        }

        if ((getDevname() == null) || !getDevname().equals(rhs.getDevname())) {
            return false;
        }

        return true;
    }
}
