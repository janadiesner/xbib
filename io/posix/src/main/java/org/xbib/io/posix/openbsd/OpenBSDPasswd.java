package org.xbib.io.posix.openbsd;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.xbib.io.posix.Passwd;

import java.util.List;

public class OpenBSDPasswd extends Structure implements Passwd {
    public String pw_name;   // user name
    public String pw_passwd; // password (encrypted)
    public int pw_uid;       // user id
    public int pw_gid;       // user id
    public NativeLong pw_change;    // password change time
    public String pw_class;  // user access class
    public String pw_gecos;  // login info
    public String pw_dir;    // home directory
    public String pw_shell;  // default shell
    public NativeLong pw_expire;    // account expiration

    OpenBSDPasswd(Pointer memory) {
        useMemory(memory);
        read();
    }

    @Override
    protected List getFieldOrder() {
        return null;
    }

    public String getAccessClass() {
        return pw_class;
    }

    public String getGECOS() {
        return pw_gecos;
    }

    public long getGID() {
        return pw_gid;
    }

    public String getHome() {
        return pw_dir;
    }

    public String getLoginName() {
        return pw_name;
    }

    public int getPasswdChangeTime() {
        return pw_change.intValue();
    }

    public String getPassword() {
        return pw_passwd;
    }

    public String getShell() {
        return pw_shell;
    }

    public long getUID() {
        return pw_uid;
    }

    public int getExpire() {
        return pw_expire.intValue();
    }
}
