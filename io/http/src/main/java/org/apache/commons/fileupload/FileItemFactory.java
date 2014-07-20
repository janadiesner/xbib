package org.apache.commons.fileupload;

/**
 * <p>A factory interface for creating {@link org.apache.commons.fileupload.FileItem} instances. Factories
 * can provide their own custom configuration, over and above that provided
 * by the default file upload implementation.</p>
 *
 */
public interface FileItemFactory {

    /**
     * Create a new {@link org.apache.commons.fileupload.FileItem} instance from the supplied parameters and
     * any local factory configuration.
     *
     * @param fieldName   The name of the form field.
     * @param contentType The content type of the form field.
     * @param isFormField <code>true</code> if this is a plain form field;
     *                    <code>false</code> otherwise.
     * @param fileName    The name of the uploaded file, if any, as supplied
     *                    by the browser or other client.
     *
     * @return The newly created file item.
     */
    FileItem createItem(
            String fieldName,
            String contentType,
            boolean isFormField,
            String fileName
    );

}
