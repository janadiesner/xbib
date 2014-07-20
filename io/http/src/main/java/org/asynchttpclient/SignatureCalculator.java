package org.asynchttpclient;

/**
 * Interface that allows injecting signature calculator into
 * {@link org.asynchttpclient.RequestBuilder} so that signature calculation and inclusion can
 * be added as a pluggable component.
 */
public interface SignatureCalculator {
    /**
     * Method called when {@link org.asynchttpclient.RequestBuilder#build} method is called.
     * Should first calculate signature information and then modify request
     * (using passed {@link org.asynchttpclient.RequestBuilder}) to add signature (usually as
     * an HTTP header).
     *
     * @param requestBuilder builder that can be used to modify request, usually
     *                       by adding header that includes calculated signature. Be sure NOT to
     *                       call {@link org.asynchttpclient.RequestBuilder#build} since this will cause infinite recursion
     * @param request        Request that is being built; needed to access content to
     *                       be signed
     */
    public void calculateAndAddSignature(String url, Request request,
                                         RequestBuilderBase<?> requestBuilder);
}
