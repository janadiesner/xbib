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
package org.xbib.oai.listrecords;

import org.xbib.io.http.HttpResponse;
import org.xbib.util.DateUtil;
import org.xbib.oai.DefaultOAIResponse;
import org.xbib.oai.exceptions.BadArgumentException;
import org.xbib.oai.exceptions.BadResumptionTokenException;
import org.xbib.oai.exceptions.NoRecordsMatchException;
import org.xbib.oai.exceptions.OAIException;
import org.xbib.xml.transform.StylesheetTransformer;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class ListRecordsResponse extends DefaultOAIResponse {

    private ListRecordsRequest request;

    private HttpResponse httpResponse;

    private String error;

    private Date date;

    private long expire;

    private StylesheetTransformer transformer;

    public ListRecordsResponse(ListRecordsRequest request) {
        super(request);
        this.request = request;
    }

    public ListRecordsResponse(ListRecordsServerRequest request) {
        super(request);
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public void setTransformer(StylesheetTransformer transformer) {
        this.transformer = transformer;
    }

    public StylesheetTransformer getTransformer() {
        return transformer;
    }

    public boolean isEmpty() {
        return httpResponse != null && httpResponse.notfound();
    }

    @Override
    public ListRecordsResponse to(Writer writer) throws IOException {
        if ("noRecordsMatch".equals(error)) {
            throw new NoRecordsMatchException("metadataPrefix=" + request.getMetadataPrefix()
                    + ",set=" + request.getSet()
                    + ",from=" + DateUtil.formatDateISO(request.getFrom())
                    + ",until=" + DateUtil.formatDateISO(request.getUntil()));
        } else if ("badResumptionToken".equals(error)) {
            throw new BadResumptionTokenException(request.getResumptionToken());
        } else if ("badArgument".equals(error)) {
            throw new BadArgumentException();
        } else if (error != null) {
            throw new OAIException(error);
        }
        if (transformer != null) {
            try {
                StreamResult streamResult = new StreamResult(writer);
                transformer.setResult(streamResult).transform();
            } catch (TransformerException e) {
                throw new IOException(e);
            }
        }
        return this;
    }

}
