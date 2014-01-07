
package org.metastatic.net;

public abstract class RemoteProcess extends Process {
    protected String host;
    protected String[] argv;
    protected String[] envp;

    protected RemoteProcess(String host, String[] argv, String[] envp) {
        this.host = host;
        this.argv = (argv != null) ?  argv.clone() : null;
        this.envp = (envp != null) ?  envp.clone() : null;
    }

    public String getHost() {
        return host;
    }

    public String getCommand() {
        if (argv != null && argv.length != 0) {
            return argv[0];
        }
        return null;
    }

    public String[] getArgv() {
        if (argv != null) {
            return (String[]) argv.clone();
        }
        return null;
    }

    public String[] getEnvp() {
        if (envp != null) {
            return (String[]) envp.clone();
        }
        return null;
    }
}
