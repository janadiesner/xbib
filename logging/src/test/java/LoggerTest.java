import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class LoggerTest {

    @Test
    public void test() {
        Logger logger = LoggerFactory.getLogger("test");
        logger.info("Hello World");
    }
}
