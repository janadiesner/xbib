package org.apache.commons.fileupload.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;

/**
 * <p>High level API for processing file uploads.</p>
 *
 * This class handles multiple files per single HTML widget, sent using
 * <code>multipart/mixed</code> encoding type, as specified by
 * <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 *
 * How the data for individual parts is stored is determined by the factory
 * used to create them; a given part may be in memory, on disk, or somewhere
 * else.
 *
 */
public class ServletFileUpload extends FileUpload {

    /**
     * Constant for HTTP POST method.
     */
    private static final String POST_METHOD = "POST";

    /**
     * Utility method that determines whether the request contains multipart
     * content.
     *
     * @param request The servlet request to be evaluated. Must be non-null.
     *
     * @return <code>true</code> if the request is multipart;
     *         <code>false</code> otherwise.
     */
    public static boolean isMultipartContent(
            HttpServletRequest request) {
        return POST_METHOD.equalsIgnoreCase(request.getMethod()) && FileUploadBase.isMultipartContent(new ServletRequestContext(request));
    }

    /**
     * Constructs an uninitialised instance of this class. A factory must be
     * configured, using <code>setFileItemFactory()</code>, before attempting
     * to parse requests.
     *
     * @see FileUpload#FileUpload(FileItemFactory)
     */
    public ServletFileUpload() {
        super();
    }

    /**
     * Constructs an instance of this class which uses the supplied factory to
     * create <code>FileItem</code> instances.
     *
     * @see FileUpload#FileUpload()
     * @param fileItemFactory The factory to use for creating file items.
     */
    public ServletFileUpload(FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param request The servlet request to be parsed.
     *
     * @return A map of <code>FileItem</code> instances parsed from the request.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     */
    public Map<String, List<FileItem>> parseParameterMap(HttpServletRequest request)
            throws FileUploadException {
        return parseParameterMap(new ServletRequestContext(request));
    }

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param request The servlet request to be parsed.
     *
     * @return An iterator to instances of <code>FileItemStream</code>
     *         parsed from the request, in the order that they were
     *         transmitted.
     *
     * @throws FileUploadException if there are problems reading/parsing
     *                             the request or storing files.
     * @throws java.io.IOException An I/O error occurred. This may be a network
     *   error while communicating with the client or a problem while
     *   storing the uploaded content.
     */
    public FileItemIterator getItemIterator(HttpServletRequest request)
    throws FileUploadException, IOException {
        return super.getItemIterator(new ServletRequestContext(request));
    }

}
