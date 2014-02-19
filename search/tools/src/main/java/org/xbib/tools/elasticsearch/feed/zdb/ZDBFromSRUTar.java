
package org.xbib.tools.elasticsearch.feed.zdb;

import org.xbib.elasticsearch.support.client.Ingest;
import org.xbib.elements.CountableElementOutput;
import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElementBuilderFactory;
import org.xbib.elements.marc.MARCElementMapper;
import org.xbib.io.keyvalue.KeyValueStreamAdapter;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.marc.dialects.MarcXmlTarReader;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.tools.elasticsearch.Feeder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.text.Normalizer;

public class ZDBFromSRUTar extends Feeder {

    private final static Logger logger = LoggerFactory.getLogger(ZDBFromSRUTar.class.getName());

    public static void main(String[] args) {
        try {
            new ZDBFromSRUTar()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private ZDBFromSRUTar() {
    }


    @Override
    protected ZDBFromSRUTar prepare(Ingest output) {
        return this;
    }

    protected ZDBFromSRUTar prepare() throws IOException {
        super.prepare();
        return this;
    }

    @Override
    protected PipelineProvider pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new ZDBFromSRUTar();
            }
        };
    }

    @Override
    protected void process(URI uri) throws Exception {
        final MarcXmlTarReader reader = new MarcXmlTarReader()
                .setURI(uri)
                .setListener(marcHandler);
        while (reader.hasNext()) {
            reader.next();
        }
        reader.close();
    }

    protected ZDBFromSRUTar cleanup() {
        bibmapper.close();
        holmapper.close();
        if (settings.getAsBoolean("detect", false)) {
            logger.info("unknown bib keys={}", bibmapper.unknownKeys());
            logger.info("unknown hol keys={}", holmapper.unknownKeys());
        }
        return this;
    }

    private String index;

    private String type;

    private String id;

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    private final OurElementOutput out = new OurElementOutput();

    private final class OurElementOutput extends CountableElementOutput<ResourceContext, Resource> {

        @Override
        public void output(ResourceContext context, ContentBuilder contentBuilder) throws IOException {
            String index = getIndex();
            String type = getType();
            String id = getID();
            logger.debug("got output = {}/{}/{}", index, type, id);
            context.resource().id(IRI.builder().scheme("http")
                    .host(index)
                    .query(type)
                    .fragment(id).build());
            sink.output(context, contentBuilder);
        }
    }

    final MARCElementMapper bibmapper = new MARCElementMapper("marc/zdb/bib")
            .pipelines(settings.getAsInt("pipelines", 1))
            .detectUnknownKeys(settings.getAsBoolean("detect", false))
            .start(new MARCElementBuilderFactory() {
                public MARCElementBuilder newBuilder() {
                    return new MARCElementBuilder().addOutput(out);
                }
            });

    final MARCElementMapper holmapper = new MARCElementMapper("marc/zdb/hol")
            .pipelines(settings.getAsInt("pipelines", 1))
            .detectUnknownKeys(settings.getAsBoolean("detect", false))
            .start(new MARCElementBuilderFactory() {
                public MARCElementBuilder newBuilder() {
                    return new MARCElementBuilder().addOutput(out);
                }
            });

    final MarcXchange2KeyValue bib = new MarcXchange2KeyValue()
            .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                @Override
                public String transform(String value) {
                    return Normalizer.normalize(value, Normalizer.Form.NFC);
                }
            })
            .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                @Override
                public KeyValueStreamAdapter<FieldCollection, String> begin() {
                    logger.debug("bib begin object");
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("bib begin, spec={}", key.toSpec());
                        for (Field f : key) {
                            logger.debug("bib tag='{}' ind='{}' subf='{}' data='{}'",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        if (value != null) {
                            logger.debug("bib value='{}'", value);

                        }
                        logger.debug("bib end");
                    }
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> end() {
                    logger.debug("bib end object");
                    return this;
                }
            })
            .addListener(bibmapper);

    final MarcXchange2KeyValue hol = new MarcXchange2KeyValue()
            .transformer(new MarcXchange2KeyValue.FieldDataTransformer() {
                @Override
                public String transform(String value) {
                    return Normalizer.normalize(value, Normalizer.Form.NFC);
                }
            })
            .addListener(new KeyValueStreamAdapter<FieldCollection, String>() {
                @Override
                public KeyValueStreamAdapter<FieldCollection, String> begin() {
                    logger.debug("hol begin object");
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> keyValue(FieldCollection key, String value) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("hol begin, spec = {}", key.toSpec());
                        for (Field f : key) {
                            logger.debug("hol tag='{}' ind='{}' subf='{}' data='{}'",
                                    f.tag(), f.indicator(), f.subfieldId(), f.data());
                        }
                        if (value != null) {
                            logger.debug("hol value='{}'", value);

                        }
                        logger.debug("hol end");
                    }
                    return this;
                }

                @Override
                public KeyValueStreamAdapter<FieldCollection, String> end() {
                    logger.debug("hol end object");
                    return this;
                }
            })
            .addListener(holmapper);

    final MarcXchangeContentHandler marcHandler = new MyMarcXchangeContentHandler()
            .addListener("Bibliographic", bib)
            .addListener("Holdings", hol);

    class MyMarcXchangeContentHandler extends MarcXchangeContentHandler {
        @Override
        public void endRecord() {
            super.endRecord();
            setIndex(settings.get("index"));
            setType(getType());
            setID(getRecordNumber());
            logger.info("end record: {}/{}/{}",
                    getFormat(), getType(), getRecordNumber());
        }
    }

}
