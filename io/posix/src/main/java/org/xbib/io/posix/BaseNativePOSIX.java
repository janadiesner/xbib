package org.xbib.io.posix;

import com.sun.jna.FromNativeContext;
import com.sun.jna.FromNativeConverter;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseNativePOSIX implements POSIX {

    protected String libraryName;

    protected LibC libc;

    protected POSIXHandler handler;

    public BaseNativePOSIX(String libraryName, LibC libc, POSIXHandler handler) {
        this.libc = libc;
        this.handler = handler;
        this.libraryName = libraryName;
    }

    public int chmod(String filename, int mode) {
        return libc.chmod(filename, mode);
    }

    public int chown(String filename, int user, int group) {
        return libc.chown(filename, user, group);
    }

    public int getegid() {
        return libc.getegid();
    }

    public int geteuid() {
        return libc.geteuid();
    }

    public int getgid() {
        return libc.getgid();
    }

    public String getlogin() {
        return libc.getlogin();
    }

    public int getpgid() {
        return libc.getpgid();
    }

    public int getpgrp() {
        return libc.getpgrp();
    }

    public int getpid() {
        return libc.getpid();
    }

    public int getppid() {
        return libc.getppid();
    }

    public Passwd getpwent() {
        return libc.getpwent();
    }

    public Passwd getpwuid(int which) {
        return libc.getpwuid(which);
    }

    public Passwd getpwnam(String which) {
        return libc.getpwnam(which);
    }

    public Group getgrent() {
        return libc.getgrent();
    }

    public Group getgrgid(int which) {
        return libc.getgrgid(which);
    }

    public Group getgrnam(String which) {
        return libc.getgrnam(which);
    }

    public int setpwent() {
        return libc.setpwent();
    }

    public int endpwent() {
        return libc.endpwent();
    }

    public int setgrent() {
        return libc.setgrent();
    }

    public int endgrent() {
        return libc.endgrent();
    }

    public int getuid() {
        return libc.getuid();
    }

    public int setegid(int egid) {
        return libc.setegid(egid);
    }

    public int seteuid(int euid) {
        return libc.seteuid(euid);
    }

    public int setgid(int gid) {
        return libc.setgid(gid);
    }

    public int getpgid(int pid) {
        return libc.getpgid(pid);
    }

    public int setpgid(int pid, int pgid) {
        return libc.setpgid(pid, pgid);
    }

    public int setpgrp(int pid, int pgrp) {
        return libc.setpgrp(pid, pgrp);
    }

    public int setsid() {
        return libc.setsid();
    }

    public int setuid(int uid) {
        return libc.setuid(uid);
    }

    public int kill(int pid, int signal) {
        return libc.kill(pid, signal);
    }

    public int lchmod(String filename, int mode) {
        return libc.lchmod(filename, mode);
    }

    public int lchown(String filename, int user, int group) {
        return libc.lchown(filename, user, group);
    }

    public int link(String oldpath, String newpath) {
        return libc.link(oldpath, newpath);
    }

    public FileStat lstat(String path) {
        FileStat stat = allocateStat();

        if (libc.lstat(path, stat) < 0) {
            handler.error(ERRORS.ENOENT, path);
        }

        return stat;
    }

    public int mkdir(String path, int mode) {
        return libc.mkdir(path, mode);
    }

    public FileStat stat(String path) {
        FileStat stat = allocateStat();
        if (libc.stat(path, stat) < 0) {
            handler.error(ERRORS.ENOENT, path);
        }
        return stat;
    }

    public int symlink(String oldpath, String newpath) {
        return libc.symlink(oldpath, newpath);
    }

    public String readlink(String oldpath) throws IOException {
        // TODO: this should not be hardcoded to 256 bytes
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int result = libc.readlink(oldpath, buffer, buffer.capacity());
        if (result == -1) {
            return null;
        }
        buffer.position(0);
        buffer.limit(result);
        return Charset.forName("ASCII").decode(buffer).toString();
    }

    public int umask(int mask) {
        return libc.umask(mask);
    }

    public int utimes(String path, long[] atimeval, long[] mtimeval) {
        Timeval[] times = null;
        if (atimeval != null && mtimeval != null) {
            times = ((Timeval[]) new DefaultNativeTimeval().toArray(2));
            times[0].setTime(atimeval);
            times[1].setTime(mtimeval);
        }
        return libc.utimes(path, times);
    }

    public int fork() {
        return libc.fork();
    }

    public int waitpid(int pid, int[] status, int flags) {
        return libc.waitpid(pid, status, flags);
    }

    public int wait(int[] status) {
        return libc.wait(status);
    }

    public int getpriority(int which, int who) {
        return libc.getpriority(which, who);
    }

    public int setpriority(int which, int who, int prio) {
        return libc.setpriority(which, who, prio);
    }

    public int errno() {
        return Native.getLastError();
    }

    public void errno(int value) {
        Native.setLastError(value);
    }

    public abstract FileStat allocateStat();

    protected boolean hasMethod(String name) {
        try {
            NativeLibrary.getInstance(libraryName).getFunction(name);
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
        return true;
    }

    public static abstract class PointerConverter implements FromNativeConverter {
        public Class nativeType() {
            return Pointer.class;
        }
    }

    public static final PointerConverter GROUP = new PointerConverter() {
        public Object fromNative(Object arg, FromNativeContext ctx) {
            return arg != null ? new DefaultNativeGroup((Pointer) arg) : null;
        }
    };

    class DefaultNativeTimeval extends Structure implements Timeval {
        public NativeLong tv_sec;
        public NativeLong tv_usec;

        public DefaultNativeTimeval() {
        }

        @Override
        protected List getFieldOrder() {
            return null;
        }

        public void setTime(long[] timeval) {
            assert timeval.length == 2;
            tv_sec.setValue(timeval[0]);
            tv_usec.setValue(timeval[1]);
        }
    }

    static class DefaultNativeGroup extends Structure implements Group {
        public String gr_name;   // name
        public String gr_passwd; // group password (encrypted)
        public int gr_gid;       // group id
        public Pointer gr_mem;

        DefaultNativeGroup(Pointer memory) {
            useMemory(memory);
            read();
        }

        @Override
        protected List getFieldOrder() {
            return null;
        }

        public String getName() {
            return gr_name;
        }

        public String getPassword() {
            return gr_passwd;
        }

        public long getGID() {
            return gr_gid;
        }

        public String[] getMembers() {
            int size = Pointer.SIZE;
            int i = 0;
            List<String> lst = new ArrayList<String>();
            while (gr_mem.getPointer(i) != null) {
                lst.add(gr_mem.getPointer(i).getString(0));
                i += size;
            }
            return lst.toArray(new String[0]);
        }

    }

}
