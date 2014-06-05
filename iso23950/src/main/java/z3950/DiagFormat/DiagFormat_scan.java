/*
 * $Source$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1998, Hoylen Sue.  All Rights Reserved.
 * <h.sue@ieee.org>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 *
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:12 UTC
 */



package z3950.DiagFormat;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.AttributeList;



/**
 * Class for representing a <code>DiagFormat_scan</code> from <code>DiagnosticFormatDiag1</code>
 * <p/>
 * <pre>
 * DiagFormat_scan ::=
 * CHOICE {
 *   nonZeroStepSize [0] IMPLICIT NULL
 *   specifiedStepSize [1] IMPLICIT NULL
 *   termList1 [3] IMPLICIT NULL
 *   termList2 [4] IMPLICIT SEQUENCE OF AttributeList
 *   posInResponse [5] IMPLICIT INTEGER
 *   resources [6] IMPLICIT NULL
 *   endOfList [7] IMPLICIT NULL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class DiagFormat_scan extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a DiagFormat_scan.
     */

    public DiagFormat_scan() {
    }



    /**
     * Constructor for a DiagFormat_scan from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagFormat_scan(BEREncoding ber, boolean check_tag)
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
        // Null out all choices

        c_nonZeroStepSize = null;
        c_specifiedStepSize = null;
        c_termList1 = null;
        c_termList2 = null;
        c_posInResponse = null;
        c_resources = null;
        c_endOfList = null;

        // Try choice nonZeroStepSize
        if (ber.tag_get() == 0 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_nonZeroStepSize = new ASN1Null(ber, false);
            return;
        }

        // Try choice specifiedStepSize
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_specifiedStepSize = new ASN1Null(ber, false);
            return;
        }

        // Try choice termList1
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_termList1 = new ASN1Null(ber, false);
            return;
        }

        // Try choice termList2
        if (ber.tag_get() == 4 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun DiagFormat_scan: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_termList2 = new AttributeList[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_termList2[p] = new AttributeList(ber_cons.elementAt(p), true);
            }
            return;
        }

        // Try choice posInResponse
        if (ber.tag_get() == 5 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_posInResponse = new ASN1Integer(ber, false);
            return;
        }

        // Try choice resources
        if (ber.tag_get() == 6 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_resources = new ASN1Null(ber, false);
            return;
        }

        // Try choice endOfList
        if (ber.tag_get() == 7 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_endOfList = new ASN1Null(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun DiagFormat_scan: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of DiagFormat_scan.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        BEREncoding f2[];
        int p;
        // Encoding choice: c_nonZeroStepSize
        if (c_nonZeroStepSize != null) {
            chosen = c_nonZeroStepSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding choice: c_specifiedStepSize
        if (c_specifiedStepSize != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_specifiedStepSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_termList1
        if (c_termList1 != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_termList1.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding choice: c_termList2
        if (c_termList2 != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            f2 = new BEREncoding[c_termList2.length];

            for (p = 0; p < c_termList2.length; p++) {
                f2[p] = c_termList2[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, f2);
        }

        // Encoding choice: c_posInResponse
        if (c_posInResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_posInResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding choice: c_resources
        if (c_resources != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_resources.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding choice: c_endOfList
        if (c_endOfList != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_endOfList.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Check for error of having none of the choices set
        if (chosen == null) {
            throw new ASN1Exception("CHOICE not set");
        }

        return chosen;
    }



    /**
     * Generating a BER encoding of the object
     * and implicitly tagging it.
     * <p/>
     * This method is for internal use only. You should use
     * the ber_encode method that does not take a parameter.
     * <p/>
     * This function should never be used, because this
     * production is a CHOICE.
     * It must never have an implicit tag.
     * <p/>
     * An exception will be thrown if it is called.
     *
     * @param tag_type the type of the tag.
     * @param tag      the tag.
     * @throws ASN1Exception if it cannot be BER encoded.
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // This method must not be called!

        // Method is not available because this is a basic CHOICE
        // which does not have an explicit tag on it. So it is not
        // permitted to allow something else to apply an implicit
        // tag on it, otherwise the tag identifying which CHOICE
        // it is will be overwritten and lost.

        throw new ASN1EncodingException("Zebulun DiagFormat_scan: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the DiagFormat_scan.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_nonZeroStepSize != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: nonZeroStepSize> ");
            }
            found = true;
            str.append("nonZeroStepSize ");
            str.append(c_nonZeroStepSize);
        }

        if (c_specifiedStepSize != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: specifiedStepSize> ");
            }
            found = true;
            str.append("specifiedStepSize ");
            str.append(c_specifiedStepSize);
        }

        if (c_termList1 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: termList1> ");
            }
            found = true;
            str.append("termList1 ");
            str.append(c_termList1);
        }

        if (c_termList2 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: termList2> ");
            }
            found = true;
            str.append("termList2 ");
            str.append("{");
            for (p = 0; p < c_termList2.length; p++) {
                str.append(c_termList2[p]);
            }
            str.append("}");
        }

        if (c_posInResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: posInResponse> ");
            }
            found = true;
            str.append("posInResponse ");
            str.append(c_posInResponse);
        }

        if (c_resources != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: resources> ");
            }
            found = true;
            str.append("resources ");
            str.append(c_resources);
        }

        if (c_endOfList != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: endOfList> ");
            }
            found = true;
            str.append("endOfList ");
            str.append(c_endOfList);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Null c_nonZeroStepSize;
    public ASN1Null c_specifiedStepSize;
    public ASN1Null c_termList1;
    public AttributeList c_termList2[];
    public ASN1Integer c_posInResponse;
    public ASN1Null c_resources;
    public ASN1Null c_endOfList;


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for posInResponse
    public static final int E_mustBeOne = 1;
    public static final int E_mustBePositive = 2;
    public static final int E_mustBeNonNegative = 3;
    public static final int E_other = 4;

} // DiagFormat_scan


//EOF