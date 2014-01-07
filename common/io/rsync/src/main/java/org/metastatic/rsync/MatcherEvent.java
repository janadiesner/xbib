
package org.metastatic.rsync;

import java.util.EventObject;

public class MatcherEvent extends EventObject {

    public MatcherEvent(Delta delta) {
        super(delta);
    }

    public Delta getDelta() {
        return (Delta) source;
    }
}
