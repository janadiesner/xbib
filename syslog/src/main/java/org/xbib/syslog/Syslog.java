package org.xbib.syslog;

import org.xbib.syslog.impl.net.tcp.TCPNetSyslogConfig;
import org.xbib.syslog.impl.net.udp.UDPNetSyslogConfig;
import org.xbib.syslog.impl.unix.UnixSyslogConfig;
import org.xbib.syslog.impl.unix.socket.UnixSocketSyslogConfig;
import org.xbib.syslog.util.OSDetectUtility;
import org.xbib.syslog.util.SyslogUtility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a Singleton interface for client implementations.
 * <p>Usage examples:</p>
 * <p>
 * <b>Direct</b>
 * <pre>
 * Syslog.getInstance("udp").info("log message");
 * </pre>
 * <p>
 * <b>Via Instance</b>
 * <pre>
 * SyslogIF syslog = Syslog.getInstance("udp");
 * syslog.info();
 * </pre>
 */
public final class Syslog implements SyslogConstants {

    protected static final Map instances = new HashMap();

    static {
        initialize();
    }

    /**
     * Syslog is a singleton.
     */
    private Syslog() {
        //
    }

    /**
     * Throws SyslogRuntimeException unless it has been suppressed via setSuppressRuntimeException(boolean).
     *
     * @param message message
     * @throws SyslogRuntimeException
     */
    private static void throwRuntimeException(String message) throws SyslogRuntimeException {
        throw new SyslogRuntimeException(message);
    }

    /**
     * Use getInstance(protocol) as the starting point.
     *
     * @param protocol - the Syslog protocol to use, e.g. "udp", "tcp", "unix_syslog", "unix_socket", or a custom protocol
     * @return Returns an instance of SyslogIF.
     * @throws SyslogRuntimeException
     */
    public static SyslogIF getInstance(String protocol) throws SyslogRuntimeException {
        String _protocol = protocol.toLowerCase();
        if (instances.containsKey(_protocol)) {
            return (SyslogIF) instances.get(_protocol);
        } else {
            StringBuilder message = new StringBuilder("Syslog protocol \"" + protocol + "\" not defined; call Syslogger.createSyslogInstance(protocol,config) first");
            if (instances.size() > 0) {
                message.append(" or use one of the following instances: ");
                Iterator i = instances.keySet().iterator();
                while (i.hasNext()) {
                    String k = (String) i.next();
                    message.append(k);
                    if (i.hasNext()) {
                        message.append(' ');
                    }
                }
            }
            throwRuntimeException(message.toString());
            return null;
        }
    }

    /**
     * Use createInstance(protocol,config) to create your own Syslog instance.
     * <p>
     * <p>First, create an implementation of SyslogConfigIF, such as UdpNetSyslogConfig.</p>
     * <p>
     * <p>Second, configure that configuration instance.</p>
     * <p>
     * <p>Third, call createInstance(protocol,config) using a short &amp; simple
     * String for the protocol argument.</p>
     * <p>
     * <p>Fourth, either use the returned instance of SyslogIF, or in later code
     * call getInstance(protocol) with the protocol chosen in the previous step.</p>
     *
     * @param protocol
     * @param config
     * @return Returns an instance of SyslogIF.
     * @throws SyslogRuntimeException
     */
    public static final SyslogIF createInstance(String protocol, SyslogConfigIF config) throws SyslogRuntimeException {
        if (protocol == null || "".equals(protocol.trim())) {
            throwRuntimeException("Instance protocol cannot be null or empty");
            return null;
        }
        if (config == null) {
            throwRuntimeException("SyslogConfig cannot be null");
            return null;
        }
        String syslogProtocol = protocol.toLowerCase();
        SyslogIF syslog;
        synchronized (instances) {
            if (instances.containsKey(syslogProtocol)) {
                throwRuntimeException("Syslog protocol \"" + protocol + "\" already defined");
                return null;
            }
            try {
                Class syslogClass = config.getSyslogClass();
                syslog = (SyslogIF) syslogClass.newInstance();
            } catch (ClassCastException cse) {
                if (!config.isThrowExceptionOnInitialize()) {
                    throw new SyslogRuntimeException(cse);

                } else {
                    return null;
                }
            } catch (IllegalAccessException iae) {
                if (!config.isThrowExceptionOnInitialize()) {
                    throw new SyslogRuntimeException(iae);
                } else {
                    return null;
                }
            } catch (InstantiationException ie) {
                if (!config.isThrowExceptionOnInitialize()) {
                    throw new SyslogRuntimeException(ie);
                } else {
                    return null;
                }
            }
            syslog.initialize(syslogProtocol, config);
            instances.put(syslogProtocol, syslog);
        }
        return syslog;
    }

    /**
     * initialize() sets up the default TCP and UDP Syslog protocols, as
     * well as UNIX_SYSLOG and UNIX_SOCKET (if running on a Unix-based system).
     */
    public synchronized static final void initialize() {
        createInstance(UDP, new UDPNetSyslogConfig());
        createInstance(TCP, new TCPNetSyslogConfig());

        if (OSDetectUtility.isUnix() && SyslogUtility.isClassExists(JNA_NATIVE_CLASS)) {
            createInstance(UNIX_SYSLOG, new UnixSyslogConfig());
            createInstance(UNIX_SOCKET, new UnixSocketSyslogConfig());
        }
    }

    /**
     * @param protocol - Syslog protocol
     * @return Returns whether the protocol has been previously defined.
     */
    public static boolean exists(String protocol) {
        return !(protocol == null || "".equals(protocol.trim())) && instances.containsKey(protocol.toLowerCase());
    }

    /**
     * shutdown() gracefully shuts down all defined Syslog protocols,
     * which includes flushing all queues and connections and finally
     * clearing all instances (including those initialized by default).
     */
    public synchronized static void shutdown() {
        Set protocols = instances.keySet();
        if (protocols.size() > 0) {
            Iterator i = protocols.iterator();
            try {
                Thread.sleep(SyslogConstants.THREAD_LOOP_INTERVAL_DEFAULT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            while (i.hasNext()) {
                String protocol = (String) i.next();
                SyslogIF syslog = (SyslogIF) instances.get(protocol);
                syslog.shutdown();
            }
            instances.clear();
        }
    }

    /**
     * destroyInstance() gracefully shuts down the specified Syslog protocol and
     * removes the instance
     *
     * @param protocol - the Syslog protocol to destroy
     * @throws SyslogRuntimeException
     */
    public synchronized static void destroyInstance(String protocol) throws SyslogRuntimeException {
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
            SyslogIF syslog = (SyslogIF) instances.get(_protocol);
            try {
                syslog.shutdown();
            } finally {
                instances.remove(_protocol);
            }
        } else {
            throwRuntimeException("Cannot destroy protocol \"" + protocol + "\" instance; call shutdown instead");
        }
    }

    /**
     * destroyInstance() gracefully shuts down the specified Syslog instance and
     * removes it.
     *
     * @param syslog - the Syslog instance to destroy
     * @throws SyslogRuntimeException
     */
    public synchronized static void destroyInstance(SyslogIF syslog) throws SyslogRuntimeException {
        if (syslog == null) {
            return;
        }
        String protocol = syslog.getProtocol().toLowerCase();
        if (instances.containsKey(protocol)) {
            try {
                syslog.shutdown();
            } finally {
                instances.remove(protocol);
            }
        } else {
            throwRuntimeException("Cannot destroy protocol \"" + protocol + "\" instance; call shutdown instead");
        }
    }

}
