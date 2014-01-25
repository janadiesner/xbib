/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, 
 * Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * The interactive user interfaces in modified source and object code 
 * versions of this program must display Appropriate Legal Notices, 
 * as required under Section 5 of the GNU Affero General Public License.
 * 
 * In accordance with Section 7(b) of the GNU Affero General Public 
 * License, these Appropriate Legal Notices must retain the display of the 
 * "Powered by xbib" logo. If the display of the logo is not reasonably 
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.tools.aleph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Normalizer;

import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archivers.tar.TarConnectionFactory;
import org.xbib.io.archivers.tar.TarSession;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.pipeline.Pipeline;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.tools.Converter;

public class AlephPublish2Tar extends Converter {

    private final static Logger logger = LoggerFactory.getLogger(AlephPublish2Tar.class.getName());

    private final static DecimalFormat df = new DecimalFormat("000000000");

    private static TarSession session;

    private static Connection connection;

    private static AlephPublishIterator it;

    public static void main(String[] args) {
        try {
            new AlephPublish2Tar()
                    .reader(new InputStreamReader(System.in, "UTF-8"))
                    .writer(new OutputStreamWriter(System.out, "UTF-8"))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public AlephPublish2Tar() {
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return new PipelineProvider<Pipeline>() {
            @Override
            public Pipeline get() {
                return new AlephPublish2Tar();
            }
        };
    }

    @Override
    protected Converter prepare() throws IOException {
        super.prepare();
        try {
            connection = DriverManager.getConnection(settings.get("uri"), settings.get("user"), settings.get("pass"));
            it = new AlephPublishIterator();
            it.configure(settings, connection);
        } catch (SQLException e) {
            throw new IOException(e);
        }
        String output = settings.get("output");
        try {
            TarConnectionFactory factory = new TarConnectionFactory();
            org.xbib.io.Connection<TarSession> connection = factory.getConnection(URI.create(output));
            session = connection.createSession();
            session.open(Session.Mode.WRITE);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    @Override
    protected Converter cleanup() {
        super.cleanup();
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (session != null) {
                session.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    @Override
    public void process(URI uri) throws Exception {
        logger.debug("process starts, uri = {}", uri);
        while (it.hasNext()) {
            Object object = it.next();
            Integer n = resolveDocNumber(connection, object);
            String docNumber = df.format(n);
            Reader reader = getRecord(connection, docNumber);
            if (reader != null) {
                String s = getClob(reader);
                Packet p = session.newPacket();
                p.name(Integer.toString(n));
                // for Unicode in non-canonical form, normalize it here
                s = Normalizer.normalize(s, Normalizer.Form.NFC);
                p.packet(s);
                session.write(p);
                if (logger.isTraceEnabled()) {
                    logger.trace("{}", n);
                }
                process(s);
            }
        }
        it.close();
        logger.debug("process ends, uri = {}", uri);
    }

    protected void process(String s) {

    }

    private Reader getRecord(Connection connection, final String docNumber) {
        if (docNumber == null) {
            return null;
        }
        final String query = "select z00p_str, z00p_ptr from "
                + settings.get("library") + ".z00p where z00p_set = '"
                + settings.get("set") + "' and z00p_doc_number = ?";
        PreparedStatement stmt = null;
        ResultSet results = null;
        try {
            stmt = connection.prepareStatement(query);
            stmt.setString(1, docNumber);
            results = stmt.executeQuery();
            if ((results != null) && results.next()) {
                return results.getBytes(1) != null ? new InputStreamReader(
                        new ByteArrayInputStream(results.getBytes(1))) : results.getCharacterStream(2);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // skip
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    // skip
                }
            }
        }
        return null;
    }

    private Integer resolveDocNumber(Connection connection, Object object) {
        if (object instanceof Integer) {
            return (Integer) object;
        }
        final String query = "select z11_doc_number from " + settings.get("library") + ".z11 where z11_rec_key like ?";
        PreparedStatement stmt = null;
        ResultSet results = null;
        try {
            stmt = connection.prepareStatement(query);
            stmt.setString(1, "IDN  " + object.toString().toLowerCase() + "%");
            results = stmt.executeQuery();
            return ((results != null) && results.next()) ? Integer.parseInt(results.getString(1)) : null;
        } catch (SQLException e) {
            logger.error(e.getMessage() + query, e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException e) {
                    //
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    //
                }
            }
        }
        return null;
    }

    private String getClob(Reader reader) throws IOException {
        if (reader == null)
            return null;
        char[] buffer = new char[8192];
        StringBuilder out = new StringBuilder();
        int read;
        do {
            read = reader.read(buffer, 0, buffer.length);
            if (read > 0) {
                out.append(buffer, 0, read);
            }
        } while (read >= 0);
        return out.toString();
    }

}
