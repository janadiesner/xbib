package org.xbib.syslog.impl.backlog.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.xbib.syslog.SyslogConstants;
import org.xbib.syslog.SyslogIF;
import org.xbib.syslog.SyslogRuntimeException;
import org.xbib.syslog.impl.backlog.AbstractSyslogBackLogHandler;

/**
 * Log4jSyslogBackLogHandler is used to send Syslog backLog messages to
 * Log4j whenever the Syslog protocol fails.
 */
public class Log4jSyslogBackLogHandler extends AbstractSyslogBackLogHandler {
    protected Logger logger = null;
    protected Level downLevel = Level.WARN;
    protected Level upLevel = Level.WARN;

    public Log4jSyslogBackLogHandler(Logger logger) throws SyslogRuntimeException {
        this.logger = logger;

        initialize();
    }

    public Log4jSyslogBackLogHandler(Logger logger, boolean appendReason) {
        this.logger = logger;
        this.appendReason = appendReason;

        initialize();
    }

    public Log4jSyslogBackLogHandler(Class loggerClass) {
        if (loggerClass == null) {
            throw new SyslogRuntimeException("loggerClass cannot be null");
        }

        this.logger = Logger.getLogger(loggerClass);

        initialize();
    }

    public Log4jSyslogBackLogHandler(Class loggerClass, boolean appendReason) {
        if (loggerClass == null) {
            throw new SyslogRuntimeException("loggerClass cannot be null");
        }

        this.logger = Logger.getLogger(loggerClass);
        this.appendReason = appendReason;

        initialize();
    }

    public Log4jSyslogBackLogHandler(String loggerName) {
        if (loggerName == null) {
            throw new SyslogRuntimeException("loggerName cannot be null");
        }

        this.logger = Logger.getLogger(loggerName);

        initialize();
    }

    public Log4jSyslogBackLogHandler(String loggerName, boolean appendReason) {
        if (loggerName == null) {
            throw new SyslogRuntimeException("loggerName cannot be null");
        }

        this.logger = Logger.getLogger(loggerName);
        this.appendReason = appendReason;

        initialize();
    }

    public Log4jSyslogBackLogHandler(String loggerName, LoggerFactory loggerFactory) {
        if (loggerName == null) {
            throw new SyslogRuntimeException("loggerName cannot be null");
        }

        if (loggerFactory == null) {
            throw new SyslogRuntimeException("loggerFactory cannot be null");
        }

        this.logger = Logger.getLogger(loggerName, loggerFactory);

        initialize();
    }

    public Log4jSyslogBackLogHandler(String loggerName, LoggerFactory loggerFactory, boolean appendReason) {
        if (loggerName == null) {
            throw new SyslogRuntimeException("loggerName cannot be null");
        }

        if (loggerFactory == null) {
            throw new SyslogRuntimeException("loggerFactory cannot be null");
        }

        this.logger = Logger.getLogger(loggerName, loggerFactory);
        this.appendReason = appendReason;

        initialize();
    }

    public void initialize() throws SyslogRuntimeException {
        if (this.logger == null) {
            throw new SyslogRuntimeException("logger cannot be null");
        }
    }

    protected static Level getLog4jLevel(int level) {
        switch (level) {
            case SyslogConstants.LEVEL_DEBUG:
                return Level.DEBUG;
            case SyslogConstants.LEVEL_INFO:
                return Level.INFO;
            case SyslogConstants.LEVEL_NOTICE:
                return Level.INFO;
            case SyslogConstants.LEVEL_WARN:
                return Level.WARN;
            case SyslogConstants.LEVEL_ERROR:
                return Level.ERROR;
            case SyslogConstants.LEVEL_CRITICAL:
                return Level.ERROR;
            case SyslogConstants.LEVEL_ALERT:
                return Level.ERROR;
            case SyslogConstants.LEVEL_EMERGENCY:
                return Level.FATAL;

            default:
                return Level.WARN;
        }
    }

    public void down(SyslogIF syslog, String reason) {
        this.logger.log(this.downLevel, "Syslog protocol \"" + syslog.getProtocol() + "\" is down: " + reason);
    }

    public void up(SyslogIF syslog) {
        this.logger.log(this.upLevel, "Syslog protocol \"" + syslog.getProtocol() + "\" is up");
    }

    public void log(SyslogIF syslog, int level, String message, String reason) throws SyslogRuntimeException {
        Level log4jLevel = getLog4jLevel(level);

        String combinedMessage = combine(syslog, level, message, reason);

        this.logger.log(log4jLevel, combinedMessage);
    }
}
