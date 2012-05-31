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
package org.xbib.rdf.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.simple.SimpleStatement;

/**
 * Parser for NTriple RDF serialization format
 *
 * See also the <a href="http://www.w3.org/TR/rdf-testcases/#ntriples">NTriple
 * specification</a>
 */
public class NTripleReader<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> 
    implements Triplifier<S,P,O> {

    private static final String resourceExpression = "(<[^<]+?>)";
    private static final String literalValueExpression = "(\"([^\"]|\\\")*\")";
    private static final String anonymousExpression = "(_:[^\\s]+?)";
    private static final String datatypeExpression = "(\\^\\^" + resourceExpression + ")";
    private static final String languageExpression = "(@([a-z]{2}?))";
    private static final String literalExpression = "(" + literalValueExpression + "(" + datatypeExpression + "|" + languageExpression + ")??" + ")";
    private static final String subjectExpression = "(" + anonymousExpression + "|" + resourceExpression + ")";
    private static final String predicateExpression = "(" + resourceExpression + ")";
    private static final String objectExpression = "(" + anonymousExpression + "|" + resourceExpression + "|" + literalExpression + ")";
    public static final String tripleExpression = subjectExpression + "\\s+" + predicateExpression + "\\s+" + objectExpression + "\\s*\\.";
    public static final Pattern NTRIPLE_PATTERN = Pattern.compile(tripleExpression);
    private BufferedReader reader;
    private boolean eof;
    private SimpleResource<S, P, O> resource;
    /**
     * An optional statement listener
     */
    private StatementListener<S, P, O> listener;

    @Override
    public NTripleReader setListener(StatementListener<S, P, O> listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public StatementListener<S,P,O> getListener() {
        return listener;
    }    

    @Override
    public NTripleReader parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in, "UTF-8"));
    }

    @Override
    public NTripleReader parse(Reader reader) throws IOException {
        this.reader = new BufferedReader(reader);
        this.eof = false;
        try {
            while (!eof) {
                parseLine(this.reader.readLine());
                if (eof) {
                    break;
                }
            }
        } finally {
            this.reader.close();
        }
        return this;
    }

    /*
     * Groups in the regular expression are identified by round brackets. There
     * are actually 21 groups in the regex. They are defined as follows:
     *
     * 0	the whole triple 1	subject 2	anonymous subject 3	resource subject 4
     * predicate 5	resource predicate 6	object 7	anonymous subject 8	resource
     * object 9	literal object 10	literal value 11	string with quotes in literal
     * value 12	string without quotes in literal value 13	last character in
     * string 14	string with apostrophes in literal value 15	string without
     * apostrophes in literal value 16	last character in string 17	datatype or
     * language 18	datatype with ^^ 19	datatype without ^^ (resource) 20
     * language with @ 21	language without @
     */
    private void parseLine(String line) throws IOException {
        if (line == null) {
            eof = true;
            return;
        }
        String s = line.trim();
        if (s.length() == 0 || s.startsWith("#")) {
            return;
        }
        Matcher matcher = NTRIPLE_PATTERN.matcher(s);
        S subject;
        P predicate;
        O object;
        if (!matcher.matches()) {
            throw new PatternSyntaxException("The given pattern " + tripleExpression + " doesn't match the expression:", s, -1);
        }
        // subject
        if (matcher.group(2) != null) {
            subject = (S) resource.createBlankNode(matcher.group(1));
        } else {
            // resource node
            String subj = matcher.group(1);
            URI subjURI = URI.create(subj.substring(1, subj.length() - 1));
            resource = new SimpleResource<>(subjURI);
            resource.setSubject(resource.createSubject(subjURI.toASCIIString()));
            subject = resource.getSubject();
        }
        // predicate
        String p = matcher.group(4);
        predicate = resource.createPredicate(p.substring(1, p.length() - 1));
        // object
        if (matcher.group(7) != null) {
            // anonymous node
            object = (O) resource.createBlankNode(matcher.group(6));
        } else if (matcher.group(8) != null) {
            // resource node
            String obj = matcher.group(6);
            object = resource.createObject(URI.create(obj.substring(1, obj.length() - 1)));
        } else {
            // literal node
            // 10 is without quotes or apostrophs
            // with quotes or apostrophes. to have the value without them you need to look at groups 12 and 15
            String literal = matcher.group(10);
            object = (O) resource.createLiteral(literal);
        }
        if (listener != null) {
            Statement stmt = new SimpleStatement<>(subject, predicate, object);
            listener.statement(stmt);
        }
    }
}
