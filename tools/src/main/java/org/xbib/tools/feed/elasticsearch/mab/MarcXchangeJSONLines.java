package org.xbib.tools.feed.elasticsearch.mab;

import org.xbib.entities.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.json.MarcXchangeJSONLinesReader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;

import java.io.IOException;
import java.io.InputStream;

public class MarcXchangeJSONLines extends TitleHoldingsFeeder {

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return MarcXchangeJSONLines::new;
    }

    @Override
    public void process(InputStream in, MABEntityQueue queue) throws IOException {
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(queue);
        MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, kv);
        reader.parse();
        in.close();
    }

}
