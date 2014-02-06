
package org.xbib.tools;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.pipeline.MetricQueuePipelineExecutor;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.PipelineElement;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public abstract class QueueConverter<T, R extends PipelineRequest, P extends Pipeline<T,R>, E extends PipelineElement>
    extends Converter<T,R,P> {

    private final Logger logger = LoggerFactory.getLogger(QueueConverter.class.getSimpleName());

    private static QueueConverter converter;

    protected MetricQueuePipelineExecutor<T,R,P,E> executor;

    protected QueueConverter<T,R,P,E> prepare() throws IOException {
        super.prepare();
        converter = this;
        return this;
    }

    public QueueConverter<T,R,P,E> run() throws Exception {
        try {
            logger.info("preparing");
            prepare();
            logger.info("executing");
            executor = new MetricQueuePipelineExecutor<T,R,P,E>()
                    .concurrency(settings.getAsInt("concurrency", 1))
                    .provider(pipelineProvider())
                    .prepare()
                    .execute()
                    .waitFor();
            logger.info("execution completed");
        } finally {
            cleanup();
            if (executor != null) {
                executor.shutdown(getPoisonElement()); // must be non null
                executor.shutdown();
                writeMetrics(writer);
            }
        }
        return this;
    }

    public BlockingQueue<E> queue() {
        return executor.queue();
    }

    public void countDown() {
        executor.countDown();
    }

    protected PipelineProvider<P> pipelineProvider() {
        return new PipelineProvider<P>() {
            int i = 0;

            @Override
            public P get() {
                return (P)new DefaultQueueConverterPipeline(converter, i++);
            }
        };
    }

    public class DefaultQueueConverterPipeline
            extends QueueConverterPipeline<T,DefaultPipelineRequest,Pipeline<T,DefaultPipelineRequest>,E> {

        public DefaultQueueConverterPipeline(QueueConverter converter, int num) {
            super(converter, num);
        }

        @Override
        public DefaultPipelineRequest next() {
            return new DefaultPipelineRequest(getElement());
        }

        @Override
        public void remove() {

        }

        @Override
        protected void process(DefaultPipelineRequest request) throws IOException {
            converter.process(request.object);
        }
    }

    public class DefaultPipelineRequest implements PipelineRequest {

        private final E object;

        public DefaultPipelineRequest(E object) {
            this.object = object;
        }

        public E get() {
            return object;
        }

        @Override
        public Long size() {
            return null;
        }
    }

    protected abstract E getPoisonElement();

    protected abstract void process(E element) throws IOException;

}
