package org.xbib.syslog.server;

import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.xbib.syslog.server.impl.net.udp.UDPNetSyslogServerConfig;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a Singleton-based interface for
 * server implementations.
 */
public class SyslogServer implements SyslogConstants {

    private static boolean SUPPRESS_RUNTIME_EXCEPTIONS = false;

    protected static final Map instances = new Hashtable();

    static {
        initialize();
    }

    private SyslogServer() {
        //
    }

    /**
     * @param suppress - true to suppress throwing SyslogRuntimeException in many methods of this class, false to throw exceptions (default)
     */
    public static void setSuppressRuntimeExceptions(boolean suppress) {
        SUPPRESS_RUNTIME_EXCEPTIONS = suppress;
    }

    /**
     * @return Returns whether or not to suppress throwing SyslogRuntimeException in many methods of this class
     */
    public static boolean getSuppressRuntimeExceptions() {
        return SUPPRESS_RUNTIME_EXCEPTIONS;
    }

    /**
     * Throws SyslogRuntimeException unless it has been suppressed via setSuppressRuntimeException(boolean).
     *
     * @param message
     * @throws SyslogRuntimeException
     */
    private static void throwRuntimeException(String message) throws SyslogRuntimeException {
        if (!SUPPRESS_RUNTIME_EXCEPTIONS) {
            throw new SyslogRuntimeException(message);
        }
    }

    public static SyslogServerIF getInstance(String protocol) throws SyslogRuntimeException {
        String syslogProtocol = protocol.toLowerCase();

        if (instances.containsKey(syslogProtocol)) {
            return (SyslogServerIF) instances.get(syslogProtocol);

        } else {
            throwRuntimeException("SyslogServer instance \"" + syslogProtocol + "\" not defined; use \"tcp\" or \"udp\" or call SyslogServer.createInstance(protocol,config) first");
            return null;
        }
    }

    public static SyslogServerIF getThreadedInstance(String protocol) throws SyslogRuntimeException {
        SyslogServerIF server = getInstance(protocol);

        if (server.getThread() == null) {
            Thread thread = new Thread(server);
            thread.setName("SyslogServer: " + protocol);
            thread.setDaemon(server.getConfig().isUseDaemonThread());
            if (server.getConfig().getThreadPriority() > -1) {
                thread.setPriority(server.getConfig().getThreadPriority());
            }

            server.setThread(thread);
            thread.start();
        }

        return server;
    }

    public static boolean exists(String protocol) {
        return !(protocol == null || "".equals(protocol.trim())) && instances.containsKey(protocol.toLowerCase());

    }

    public static SyslogServerIF createInstance(String protocol, SyslogServerConfigIF config) throws SyslogRuntimeException {
        if (protocol == null || "".equals(protocol.trim())) {
            throwRuntimeException("Instance protocol cannot be null or empty");
            return null;
        }

        if (config == null) {
            throwRuntimeException("SyslogServerConfig cannot be null");
            return null;
        }

        String syslogProtocol = protocol.toLowerCase();

        SyslogServerIF syslogServer = null;

        synchronized (instances) {
            if (instances.containsKey(syslogProtocol)) {
                throwRuntimeException("SyslogServer instance \"" + syslogProtocol + "\" already defined.");
                return null;
            }

            try {
                Class syslogClass = config.getSyslogServerClass();

                syslogServer = (SyslogServerIF) syslogClass.newInstance();

            } catch (ClassCastException cse) {
                throw new SyslogRuntimeException(cse);

            } catch (IllegalAccessException iae) {
                throw new SyslogRuntimeException(iae);

            } catch (InstantiationException ie) {
                throw new SyslogRuntimeException(ie);
            }

            syslogServer.initialize(syslogProtocol, config);

            instances.put(syslogProtocol, syslogServer);
        }

        return syslogServer;
    }

    public static SyslogServerIF createThreadedInstance(String protocol, SyslogServerConfigIF config) throws SyslogRuntimeException {
        createInstance(protocol, config);

        return getThreadedInstance(protocol);
    }

    public synchronized static void destroyInstance(String protocol) {
        if (protocol == null || "".equals(protocol.trim())) {
            return;
        }

        String _protocol = protocol.toLowerCase();

        if (instances.containsKey(_protocol)) {
            try {
                Thread.sleep(SyslogConstants.THREAD_LOOP_INTERVAL_DEFAULT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            SyslogServerIF syslogServer = (SyslogServerIF) instances.get(_protocol);

            try {
                syslogServer.shutdown();

            } finally {
                instances.remove(_protocol);
            }

        } else {
            throwRuntimeException("Cannot destroy server protocol \"" + protocol + "\" instance; call shutdown instead");
        }
    }

    public synchronized static void destroyInstance(SyslogServerIF syslogServer) {
        if (syslogServer == null) {
            return;
        }

        String protocol = syslogServer.getProtocol().toLowerCase();

        if (instances.containsKey(protocol)) {
            try {
                Thread.sleep(SyslogConstants.THREAD_LOOP_INTERVAL_DEFAULT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                syslogServer.shutdown();

            } finally {
                instances.remove(protocol);
            }

        } else {
            throwRuntimeException("Cannot destroy server protocol \"" + protocol + "\" instance; call shutdown instead");
        }
    }

    public synchronized static void initialize() {
        createInstance(UDP, new UDPNetSyslogServerConfig());
        createInstance(TCP, new TCPNetSyslogServerConfig());
    }

    public synchronized static void shutdown() throws SyslogRuntimeException {
        Set protocols = instances.keySet();

        for (Object protocol1 : protocols) {
            String protocol = (String) protocol1;

            SyslogServerIF syslogServer = (SyslogServerIF) instances.get(protocol);

            syslogServer.shutdown();
        }

        instances.clear();
    }

}
