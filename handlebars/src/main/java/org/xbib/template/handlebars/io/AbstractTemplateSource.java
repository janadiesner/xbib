
package org.xbib.template.handlebars.io;

/**
 * Base class for {@link org.xbib.template.handlebars.io.TemplateSource} with default implementation of {@link #equals(Object)} and
 * {@link #hashCode()}.
 */
public abstract class AbstractTemplateSource implements TemplateSource {

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 17;
        result = prime * result + filename().hashCode();
        result = prime * result + Long.toString(lastModified()).hashCode();
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TemplateSource) {
            TemplateSource that = (TemplateSource) obj;
            return filename().equals(that.filename()) && lastModified() == that.lastModified();
        }
        return false;
    }

    @Override
    public String toString() {
        return filename();
    }
}
