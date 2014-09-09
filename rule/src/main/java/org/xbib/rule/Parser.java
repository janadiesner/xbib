
package org.xbib.rule;

import org.xbib.rule.io.RuleSource;

import java.io.IOException;

public interface Parser {

    Rule parse(RuleSource source) throws IOException;
}
