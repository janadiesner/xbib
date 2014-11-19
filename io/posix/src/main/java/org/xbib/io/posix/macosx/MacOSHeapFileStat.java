package org.xbib.io.posix.macosx;

import org.xbib.io.posix.BaseHeapFileStat;
import org.xbib.io.posix.POSIX;

public final class MacOSHeapFileStat extends BaseHeapFileStat {
    public final class time_t extends Long {
    }

    public final Int32 st_dev = new Int32();
    public final Int32 st_ino = new Int32();
    public final Int16 st_mode = new Int16();
    public final Int16 st_nlink = new Int16();
    public final Int32 st_uid = new Int32();
    public final Int32 st_gid = new Int32();
    public final Int32 st_rdev = new Int32();
    public final time_t st_atime = new time_t();
    public final Long st_atimensec = new Long();
    public final time_t st_mtime = new time_t();
    public final Long st_mtimensec = new Long();
    public final time_t st_ctime = new time_t();
    public final Long st_ctimensec = new Long();
    public final Int64 st_size = new Int64();
    public final Int64 st_blocks = new Int64();
    public final Int32 st_blksize = new Int32();
    public final Int32 st_flags = new Int32();
    public final Int32 st_gen = new Int32();
    public final Int32 st_lspare = new Int32();
    public final Int64 st_qspare0 = new Int64();
    public final Int64 st_qspare1 = new Int64();

    public MacOSHeapFileStat() {
        this(null);
    }

    public MacOSHeapFileStat(POSIX posix) {
        super(posix);
    }

    public long atime() {
        return st_atime.get();
    }

    public long blocks() {
        return st_blocks.get();
    }

    public long blockSize() {
        return st_blksize.get();
    }

    public long ctime() {
        return st_ctime.get();
    }

    public long dev() {
        return st_dev.get();
    }

    public int gid() {
        return st_gid.get();
    }

    public long ino() {
        return st_ino.get();
    }

    public int mode() {
        return st_mode.get() & 0xffff;
    }

    public long mtime() {
        return st_mtime.get();
    }

    public int nlink() {
        return st_nlink.get();
    }

    public long rdev() {
        return st_rdev.get();
    }

    public long st_size() {
        return st_size.get();
    }

    public int uid() {
        return st_uid.get();
    }
}
