package org.xbib.iri.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class AntlrIRITest {

    private final static Logger logger = LoggerFactory.getLogger(AntlrIRITest.class.getName());

    @Test
    public void test() throws Exception {
        IRILexer lexer = new IRILexer(new ANTLRInputStream("http://localhost:9200/this/is/a/path?query=test#frag"));
        IRIParser parser = new IRIParser(new CommonTokenStream(lexer));
        // Use the demo listener which will print some info about the input.
        ParseTreeWalker walker = new ParseTreeWalker();
        ParseTree tree = parser.parse();
        walker.walk(new DemoListener(), tree);
    }
    class DemoListener extends IRIBaseListener {

        @Override
        public void enterScheme(IRIParser.SchemeContext ctx) {
            logger.info("> scheme : " + ctx.getText());
        }

        @Override
        public void enterIpath(IRIParser.IpathContext ctx) {
            logger.info("> path : " + ctx.getText());
        }

        @Override
        public void enterIquery(IRIParser.IqueryContext ctx) {
            logger.info("> query : " + ctx.getText());
        }

        @Override
        public void enterIfragment(IRIParser.IfragmentContext ctx) {
            logger.info("> fragment : " + ctx.getText());
        }

        @Override
        public void enterIhier_part(IRIParser.Ihier_partContext ctx) {
            logger.info("> hier-part : " + ctx.getText());
        }
    }
}
