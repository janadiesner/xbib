package org.asynchttpclient.extra;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterException;
import org.asynchttpclient.filter.RequestFilter;

/**
 * A {@link org.asynchttpclient.filter.RequestFilter} throttles requests and block when the number of permits is reached, waiting for
 * the response to arrives before executing the next request.
 */
public class ThrottleRequestFilter implements RequestFilter {

    private final static Logger logger = LogManager.getLogger(ThrottleRequestFilter.class.getName());

    private final Semaphore available;

    private final int maxWait;

	public ThrottleRequestFilter(int maxConnections) {
		this(maxConnections, Integer.MAX_VALUE);
	}

	public ThrottleRequestFilter(int maxConnections, int maxWait) {
		this.maxWait = maxWait;
		available = new Semaphore(maxConnections, true);
	}

	@Override
	public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Current Throttling Status {}", available.availablePermits());
			}
			if (!available.tryAcquire(maxWait, TimeUnit.MILLISECONDS)) {
				throw new FilterException(String.format(
						"No slot available for processing Request %s with AsyncHandler %s", ctx.getRequest(),
						ctx.getAsyncHandler()));
			}
		} catch (InterruptedException e) {
			throw new FilterException(String.format("Interrupted Request %s with AsyncHandler %s", ctx.getRequest(),
					ctx.getAsyncHandler()));
		}

		return new FilterContext.FilterContextBuilder<T>(ctx).asyncHandler(
				new AsyncHandlerWrapper<T>(ctx.getAsyncHandler(), available)).build();
	}
}