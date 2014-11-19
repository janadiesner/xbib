package org.xbib.io.posix;

import com.sun.jna.Library;

import java.nio.ByteBuffer;

public interface LibC extends Library {
    int chmod(String filename, int mode);

    int chown(String filename, int user, int group);

    int fstat64(int fd, FileStat stat);

    int getegid();

    int setegid(int egid);

    int geteuid();

    int seteuid(int euid);

    int getgid();

    String getlogin();

    int setgid(int gid);

    int getpgid();

    int getpgid(int pid);

    int setpgid(int pid, int pgid);

    int getpgrp();

    int setpgrp(int pid, int pgrp);

    int getppid();

    int getpid();

    Passwd getpwent();

    Passwd getpwuid(int which);

    Passwd getpwnam(String which);

    Group getgrent();

    Group getgrgid(int which);

    Group getgrnam(String which);

    int setpwent();

    int endpwent();

    int setgrent();

    int endgrent();

    int getuid();

    int setsid();

    int setuid(int uid);

    int kill(int pid, int signal);

    int lchmod(String filename, int mode);

    int lchown(String filename, int user, int group);

    int link(String oldpath, String newpath);

    int lstat(String path, FileStat stat);

    int lstat64(String path, FileStat stat);

    int mkdir(String path, int mode);

    int stat(String path, FileStat stat);

    int stat64(String path, FileStat stat);

    int symlink(String oldpath, String newpath);

    int readlink(String oldpath, ByteBuffer buffer, int len);

    int umask(int mask);

    int utimes(String path, Timeval[] times);

    int fork();

    int waitpid(int pid, int[] status, int options);

    int wait(int[] status);

    int getpriority(int which, int who);

    int setpriority(int which, int who, int prio);

}
