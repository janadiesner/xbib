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
package z3950.v3;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>Specification</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * Specification ::=
 * SEQUENCE {
 *   schema [1] IMPLICIT OBJECT IDENTIFIER OPTIONAL
 *   elementSpec [2] EXPLICIT Specification_elementSpec OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class Specification extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a Specification.
     */

    public Specification() {
    }



    /**
     * Constructor for a Specification from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Specification(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag);
    }



    /**
     * Initializing object from a BER encoding.
     * This method is for internal use only. You should use
     * the constructor that takes a BEREncoding.
     *
     * @param ber       the BER to decode.
     * @param check_tag if the tag should be checked.
     * @throws ASN1Exception if the BER encoding is bad.
     */

    public void
    ber_decode(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        // Specification should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun Specification: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_schema = null;
        s_elementSpec = null;

        // Decoding: schema [1] IMPLICIT OBJECT IDENTIFIER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_schema = new ASN1ObjectIdentifier(p, false);
            part++;
        }

        // Decoding: elementSpec [2] EXPLICIT Specification_elementSpec OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Specification: bad BER encoding: s_elementSpec tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Specification: bad BER encoding: s_elementSpec tag bad\n");
            }

            s_elementSpec = new Specification_elementSpec(tagged.elementAt(0), true);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun Specification: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the Specification.
     *
     * @exception ASN1Exception Invalid or cannot be encoded.
     * @return The BER encoding.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
    }



    /**
     * Returns a BER encoding of Specification, implicitly tagged.
     *
     * @param tag_type The type of the implicit tag.
     * @param tag      The implicit tag.
     * @return The BER encoding of the object.
     * @exception ASN1Exception When invalid or cannot be encoded.
     * @see asn1.BEREncoding#UNIVERSAL_TAG
     * @see asn1.BEREncoding#APPLICATION_TAG
     * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
     * @see asn1.BEREncoding#PRIVATE_TAG
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Calculate the number of fields in the encoding

        int num_fields = 0; // number of mandatories
        if (s_schema != null) {
            num_fields++;
        }
        if (s_elementSpec != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_schema: OBJECT IDENTIFIER OPTIONAL

        if (s_schema != null) {
            fields[x++] = s_schema.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_elementSpec: Specification_elementSpec OPTIONAL

        if (s_elementSpec != null) {
            enc = new BEREncoding[1];
            enc[0] = s_elementSpec.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the Specification.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_schema != null) {
            str.append("schema ");
            str.append(s_schema);
            outputted++;
        }

        if (s_elementSpec != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("elementSpec ");
            str.append(s_elementSpec);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1ObjectIdentifier s_schema; // optional
    public Specification_elementSpec s_elementSpec; // optional

} // Specification


//EOF
