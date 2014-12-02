package org.xbib.syslog;

/**
 * SyslogBackLogHandlerIF provides a last-chance mechanism to log messages that fail
 * (for whatever reason) within the rest of Syslog.
 * <p>
 * <p>Implementing the down(SyslogIF) method is an excellent way to add some sort of notification to
 * your application when a Syslog service is unavailable.</p>
 * <p>
 * <p>Implementing the up(SyslogIF) method can be used to notify your application when a Syslog
 * service has returned.</p>
 */
public interface SyslogBackLogHandlerIF {
    /**
     * Implement initialize() to handle one-time set-up for this backLog handler.
     *
     * @throws SyslogRuntimeException
     */
    void initialize() throws SyslogRuntimeException;

    /**
     * Implement down(syslog,reason) to notify/log when the syslog protocol is unavailable.
     *
     * @param syslog - SyslogIF instance causing this down condition
     * @param reason - reason given for the down condition
     */
    void down(Syslogger syslog, String reason);

    /**
     * Implement up(syslog) to notify/log when the syslog protocol becomes available after a down condition.
     *
     * @param syslog - SyslogIF instance which is now available
     */
    void up(Syslogger syslog);

    /**
     * @param syslog  - SyslogIF instance which cannot handle this log event
     * @param level   - message level
     * @param message - message (in String form)
     * @param reason  - reason given for why this message could not be handled
     * @throws SyslogRuntimeException - throwing this Exception activates the next backlogHandler in the chain
     */
    void log(Syslogger syslog, int level, String message, String reason) throws SyslogRuntimeException;
}
