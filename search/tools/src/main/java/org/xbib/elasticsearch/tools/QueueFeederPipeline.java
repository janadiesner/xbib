
package org.xbib.elasticsearch.tools;

import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineRequest;
import org.xbib.pipeline.element.PipelineElement;
import org.xbib.tools.QueueConverterPipeline;

public abstract class QueueFeederPipeline<T, R extends PipelineRequest, P extends Pipeline<T,R>, E extends PipelineElement>
     extends QueueConverterPipeline<T,R,P,E> {

    public QueueFeederPipeline(QueueFeeder<T, R, P, E> feeder, int num) {
        super(feeder, num);
    }
}
