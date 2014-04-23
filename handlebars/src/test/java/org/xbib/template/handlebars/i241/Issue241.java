package org.xbib.template.handlebars.i241;

import org.junit.Test;
import org.xbib.template.handlebars.AbstractTest;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.helper.StringHelpers;

import java.io.IOException;
import java.util.Calendar;

public class Issue241 extends AbstractTest {

    @Override
    protected void configure(final Handlebars handlebars) {
        handlebars.registerHelper("dateFormat", StringHelpers.dateFormat);
    }

    @Test
    public void formatAsHashInDateFormat() throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1999);
        calendar.set(Calendar.MONTH, 6);
        calendar.set(Calendar.DATE, 16);

        shouldCompileTo("{{dateFormat date format=\"dd-MM-yyyy\"}}", $("date", calendar.getTime()),
                "16-07-1999");
    }
}
