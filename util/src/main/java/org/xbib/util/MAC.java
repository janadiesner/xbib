package org.xbib.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This implementation tries to obtain the MAC address
 * of the network card. Under Microsoft Windows, the <code>ifconfig</code>
 * command is used which may pop up a command window in Java Virtual Machines
 * prior to 1.4 once this class is initialized. The command window is closed
 * automatically.
 * <p>
 * The MAC address code has been tested extensively in Microsoft Windows,
 * Linux, Solaris 8, HP-UX 11, but should work in MacOS X and BSDs, too.
 * <p>
 * If you use JDK 6 or later, the code in {@link java.net.InterfaceAddress} will be used.
 */
public class MAC {

    /**
     * The cached MAC address.
     */
    private static String macAddress = null;

    /**
     * The current clock and node value.
     */
    private static long clockSeqAndNode = 0x8000000000000000L;


    /**
     * Returns the current clockSeqAndNode value.
     *
     * @return the clockSeqAndNode value
     * @see UUID#getClockSeqAndNode()
     */
    public static long getClockSeqAndNode() {
        return clockSeqAndNode;
    }


    /**
     * Returns the MAC address. Not guaranteed to return anything.
     *
     * @return the MAC address, may be <code>null</code>
     */
    public static String getMACAddress() {
        return macAddress;
    }

    static {

        try {
            Class.forName("java.net.InterfaceAddress");
            macAddress = Class.forName(
                    "com.eaio.uuid.UUIDGen$HardwareAddressLookup").newInstance().toString();
        } catch (ExceptionInInitializerError err) {
            // Ignored.
        } catch (ClassNotFoundException ex) {
            // Ignored.
        } catch (LinkageError err) {
            // Ignored.
        } catch (IllegalAccessException ex) {
            // Ignored.
        } catch (InstantiationException ex) {
            // Ignored.
        } catch (SecurityException ex) {
            // Ignored.
        }

        if (macAddress == null) {

            Process p = null;
            BufferedReader in = null;

            try {
                String osname = System.getProperty("os.name", ""), osver = System.getProperty("os.version", "");

                if (osname.startsWith("Windows")) {
                    p = Runtime.getRuntime().exec(
                            new String[]{"ipconfig", "/all"}, null);
                }

                // Solaris code must appear before the generic code
                else if (osname.startsWith("Solaris")
                        || osname.startsWith("SunOS")) {
                    if (osver.startsWith("5.11")) {
                        p = Runtime.getRuntime().exec(
                                new String[]{"dladm", "show-phys", "-m"}, null);
                    } else {
                        String hostName = getFirstLineOfCommand("uname", "-n");
                        if (hostName != null) {
                            p = Runtime.getRuntime().exec(
                                    new String[]{"/usr/sbin/arp", hostName},
                                    null);
                        }
                    }
                } else if (new File("/usr/sbin/lanscan").exists()) {
                    p = Runtime.getRuntime().exec(
                            new String[]{"/usr/sbin/lanscan"}, null);
                } else if (new File("/sbin/ifconfig").exists()) {
                    p = Runtime.getRuntime().exec(
                            new String[]{"/sbin/ifconfig", "-a"}, null);
                }

                if (p != null) {
                    in = new BufferedReader(new InputStreamReader(
                            p.getInputStream()), 128);
                    String l = null;
                    while ((l = in.readLine()) != null) {
                        macAddress = parse(l);
                        if (macAddress != null
                                && Hex.parseShort(macAddress) != 0xff) {
                            break;
                        }
                    }
                }

            } catch (SecurityException ex) {
                // Ignore it.
            } catch (IOException ex) {
                // Ignore it.
            } finally {
                if (p != null) {
                    try {
                        in.close();
                        p.getErrorStream().close();
                        p.getOutputStream().close();
                    } catch (IOException e) {
                        // ignore
                    }
                    p.destroy();
                }
            }

        }

        if (macAddress != null) {
            clockSeqAndNode |= Hex.parseLong(macAddress);
        } else {
            try {
                byte[] local = InetAddress.getLocalHost().getAddress();
                clockSeqAndNode |= (local[0] << 24) & 0xFF000000L;
                clockSeqAndNode |= (local[1] << 16) & 0xFF0000;
                clockSeqAndNode |= (local[2] << 8) & 0xFF00;
                clockSeqAndNode |= local[3] & 0xFF;
            } catch (UnknownHostException ex) {
                clockSeqAndNode |= (long) (Math.random() * 0x7FFFFFFF);
            }
        }

        // Skip the clock sequence generation process and use random instead.

        clockSeqAndNode |= (long) (Math.random() * 0x3FFF) << 48;

    }

    /**
     * Scans MAC addresses for good ones.
     */
    static class HardwareAddressLookup {

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            String out = null;
            try {
                Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
                if (ifs != null) {
                    while (ifs.hasMoreElements()) {
                        NetworkInterface iface = ifs.nextElement();
                        byte[] hardware = iface.getHardwareAddress();
                        if (hardware != null && hardware.length == 6
                                && hardware[1] != (byte) 0xff) {
                            out = Hex.append(new StringBuilder(36), hardware).toString();
                            break;
                        }
                    }
                }
            } catch (SocketException ex) {
                // Ignore it.
            }
            return out;
        }

    }


    /**
     * Returns the first line of the shell command.
     *
     * @param commands the commands to run
     * @return the first line of the command
     * @throws java.io.IOException
     */
    static String getFirstLineOfCommand(String... commands) throws IOException {

        Process p = null;
        BufferedReader reader = null;

        try {
            p = Runtime.getRuntime().exec(commands);
            reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()), 128);

            return reader.readLine();
        } finally {
            if (p != null) {
                reader.close();
                p.getErrorStream().close();
                p.getOutputStream().close();
                p.destroy();
            }
        }

    }

    private static final Pattern MAC_ADDRESS = Pattern.compile("((?:[A-F0-9]{1,2}[:-]){5}[A-F0-9]{1,2})|(?:0x)(\\d{12})(?:.+ETHER)", Pattern.CASE_INSENSITIVE);

    /**
     * Attempts to find a pattern in the given String.
     *
     * @param in the String, may not be <code>null</code>
     * @return the substring that matches this pattern or <code>null</code>
     */
    static String parse(String in) {
        Matcher m = MAC_ADDRESS.matcher(in);
        if (m.find()) {
            String g = m.group(2);
            if (g == null) {
                g = m.group(1);
            }
            return g == null ? g : g.replace('-', ':');
        }
        return null;
    }

}
