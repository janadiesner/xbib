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
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:13 UTC
 */

//----------------------------------------------------------------

package z3950.RS_Explain;
import asn1.*;
import z3950.v3.AttributeElement;
import z3950.v3.AttributeList;
import z3950.v3.AttributeSetId;
import z3950.v3.DatabaseName;
import z3950.v3.ElementSetName;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;
import z3950.v3.OtherInformation;
import z3950.v3.Specification;
import z3950.v3.StringOrNumeric;
import z3950.v3.Term;
import z3950.v3.Unit;

//================================================================
/**
 * Class for representing a <code>AccessInfo</code> from <code>RecordSyntax-explain</code>
 *
 * <pre>
 * AccessInfo ::=
 * SEQUENCE {
 *   queryTypesSupported [0] IMPLICIT SEQUENCE OF QueryTypeDetails OPTIONAL
 *   diagnosticsSets [1] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL
 *   attributeSetIds [2] IMPLICIT SEQUENCE OF AttributeSetId OPTIONAL
 *   schemas [3] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL
 *   recordSyntaxes [4] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL
 *   resourceChallenges [5] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL
 *   restrictedAccess [6] IMPLICIT AccessRestrictions OPTIONAL
 *   costInfo [8] IMPLICIT Costs OPTIONAL
 *   variantSets [9] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL
 *   elementSetNames [10] IMPLICIT SEQUENCE OF ElementSetName OPTIONAL
 *   unitSystems [11] IMPLICIT SEQUENCE OF InternationalString
 * }
 * </pre>
 *
 * @version	$Release$ $Date$
 */

//----------------------------------------------------------------

public final class AccessInfo extends ASN1Any
{

  public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";

//----------------------------------------------------------------
/**
 * Default constructor for a AccessInfo.
 */

public
AccessInfo()
{
}

//----------------------------------------------------------------
/**
 * Constructor for a AccessInfo from a BER encoding.
 * <p>
 *
 * @param ber the BER encoding.
 * @param check_tag will check tag if true, use false
 *         if the BER has been implicitly tagged. You should
 *         usually be passing true.
 * @exception	ASN1Exception if the BER encoding is bad.
 */

public
AccessInfo(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  super(ber, check_tag);
}

//----------------------------------------------------------------
/**
 * Initializing object from a BER encoding.
 * This method is for internal use only. You should use
 * the constructor that takes a BEREncoding.
 *
 * @param ber the BER to decode.
 * @param check_tag if the tag should be checked.
 * @exception ASN1Exception if the BER encoding is bad.
 */

public void
ber_decode(BEREncoding ber, boolean check_tag)
       throws ASN1Exception
{
  // AccessInfo should be encoded by a constructed BER

  BERConstructed ber_cons;
  try {
    ber_cons = (BERConstructed) ber;
  } catch (ClassCastException e) {
    throw new ASN1EncodingException
      ("Zebulun AccessInfo: bad BER form\n");
  }

  // Prepare to decode the components

  int num_parts = ber_cons.number_components();
  int part = 0;
  BEREncoding p;

  // Decoding: queryTypesSupported [0] IMPLICIT SEQUENCE OF QueryTypeDetails OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 0 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_queryTypesSupported = new QueryTypeDetails[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_queryTypesSupported[n] = new QueryTypeDetails(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: diagnosticsSets [1] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 1 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_diagnosticsSets = new ASN1ObjectIdentifier[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_diagnosticsSets[n] = new ASN1ObjectIdentifier(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: attributeSetIds [2] IMPLICIT SEQUENCE OF AttributeSetId OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 2 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_attributeSetIds = new AttributeSetId[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_attributeSetIds[n] = new AttributeSetId(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: schemas [3] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 3 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_schemas = new ASN1ObjectIdentifier[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_schemas[n] = new ASN1ObjectIdentifier(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: recordSyntaxes [4] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 4 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_recordSyntaxes = new ASN1ObjectIdentifier[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_recordSyntaxes[n] = new ASN1ObjectIdentifier(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: resourceChallenges [5] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 5 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_resourceChallenges = new ASN1ObjectIdentifier[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_resourceChallenges[n] = new ASN1ObjectIdentifier(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: restrictedAccess [6] IMPLICIT AccessRestrictions OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 6 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_restrictedAccess = new AccessRestrictions(p, false);
    part++;
  }

  // Decoding: costInfo [8] IMPLICIT Costs OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 8 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    s_costInfo = new Costs(p, false);
    part++;
  }

  // Decoding: variantSets [9] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 9 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_variantSets = new ASN1ObjectIdentifier[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_variantSets[n] = new ASN1ObjectIdentifier(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: elementSetNames [10] IMPLICIT SEQUENCE OF ElementSetName OPTIONAL

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() == 10 &&
      p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
    try {
      BERConstructed cons = (BERConstructed) p;
      int parts = cons.number_components();
      s_elementSetNames = new ElementSetName[parts];
      int n;
      for (n = 0; n < parts; n++) {
        s_elementSetNames[n] = new ElementSetName(cons.elementAt(n), true);
      }
    } catch (ClassCastException e) {
      throw new ASN1EncodingException("Bad BER");
    }
    part++;
  }

  // Decoding: unitSystems [11] IMPLICIT SEQUENCE OF InternationalString

  if (num_parts <= part) {
    // End of record, but still more elements to get
    throw new ASN1Exception("Zebulun AccessInfo: incomplete");
  }
  p = ber_cons.elementAt(part);

  if (p.tag_get() != 11 ||
      p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG)
    throw new ASN1EncodingException
      ("Zebulun AccessInfo: bad tag in s_unitSystems\n");

  try {
    BERConstructed cons = (BERConstructed) p;
    int parts = cons.number_components();
    s_unitSystems = new InternationalString[parts];
    int n;
    for (n = 0; n < parts; n++) {
      s_unitSystems[n] = new InternationalString(cons.elementAt(n), true);
    }
  } catch (ClassCastException e) {
    throw new ASN1EncodingException("Bad BER");
  }
  part++;

  // Should not be any more parts

  if (part < num_parts) {
    throw new ASN1Exception("Zebulun AccessInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
  }
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of the AccessInfo.
 *
 * @exception	ASN1Exception Invalid or cannot be encoded.
 * @return	The BER encoding.
 */

public BEREncoding
ber_encode()
       throws ASN1Exception
{
  return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
}

//----------------------------------------------------------------
/**
 * Returns a BER encoding of AccessInfo, implicitly tagged.
 *
 * @param tag_type	The type of the implicit tag.
 * @param tag	The implicit tag.
 * @return	The BER encoding of the object.
 * @exception	ASN1Exception When invalid or cannot be encoded.
 * @see asn1.BEREncoding#UNIVERSAL_TAG
 * @see asn1.BEREncoding#APPLICATION_TAG
 * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
 * @see asn1.BEREncoding#PRIVATE_TAG
 */

public BEREncoding
ber_encode(int tag_type, int tag)
       throws ASN1Exception
{
  // Calculate the number of fields in the encoding

  int num_fields = 1; // number of mandatories
  if (s_queryTypesSupported != null)
    num_fields++;
  if (s_diagnosticsSets != null)
    num_fields++;
  if (s_attributeSetIds != null)
    num_fields++;
  if (s_schemas != null)
    num_fields++;
  if (s_recordSyntaxes != null)
    num_fields++;
  if (s_resourceChallenges != null)
    num_fields++;
  if (s_restrictedAccess != null)
    num_fields++;
  if (s_costInfo != null)
    num_fields++;
  if (s_variantSets != null)
    num_fields++;
  if (s_elementSetNames != null)
    num_fields++;

  // Encode it

  BEREncoding fields[] = new BEREncoding[num_fields];
  int x = 0;
  BEREncoding f2[];
  int p;

  // Encoding s_queryTypesSupported: SEQUENCE OF OPTIONAL

  if (s_queryTypesSupported != null) {
    f2 = new BEREncoding[s_queryTypesSupported.length];

    for (p = 0; p < s_queryTypesSupported.length; p++) {
      f2[p] = s_queryTypesSupported[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 0, f2);
  }

  // Encoding s_diagnosticsSets: SEQUENCE OF OPTIONAL

  if (s_diagnosticsSets != null) {
    f2 = new BEREncoding[s_diagnosticsSets.length];

    for (p = 0; p < s_diagnosticsSets.length; p++) {
      f2[p] = s_diagnosticsSets[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);
  }

  // Encoding s_attributeSetIds: SEQUENCE OF OPTIONAL

  if (s_attributeSetIds != null) {
    f2 = new BEREncoding[s_attributeSetIds.length];

    for (p = 0; p < s_attributeSetIds.length; p++) {
      f2[p] = s_attributeSetIds[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
  }

  // Encoding s_schemas: SEQUENCE OF OPTIONAL

  if (s_schemas != null) {
    f2 = new BEREncoding[s_schemas.length];

    for (p = 0; p < s_schemas.length; p++) {
      f2[p] = s_schemas[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);
  }

  // Encoding s_recordSyntaxes: SEQUENCE OF OPTIONAL

  if (s_recordSyntaxes != null) {
    f2 = new BEREncoding[s_recordSyntaxes.length];

    for (p = 0; p < s_recordSyntaxes.length; p++) {
      f2[p] = s_recordSyntaxes[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, f2);
  }

  // Encoding s_resourceChallenges: SEQUENCE OF OPTIONAL

  if (s_resourceChallenges != null) {
    f2 = new BEREncoding[s_resourceChallenges.length];

    for (p = 0; p < s_resourceChallenges.length; p++) {
      f2[p] = s_resourceChallenges[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, f2);
  }

  // Encoding s_restrictedAccess: AccessRestrictions OPTIONAL

  if (s_restrictedAccess != null) {
    fields[x++] = s_restrictedAccess.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
  }

  // Encoding s_costInfo: Costs OPTIONAL

  if (s_costInfo != null) {
    fields[x++] = s_costInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
  }

  // Encoding s_variantSets: SEQUENCE OF OPTIONAL

  if (s_variantSets != null) {
    f2 = new BEREncoding[s_variantSets.length];

    for (p = 0; p < s_variantSets.length; p++) {
      f2[p] = s_variantSets[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 9, f2);
  }

  // Encoding s_elementSetNames: SEQUENCE OF OPTIONAL

  if (s_elementSetNames != null) {
    f2 = new BEREncoding[s_elementSetNames.length];

    for (p = 0; p < s_elementSetNames.length; p++) {
      f2[p] = s_elementSetNames[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 10, f2);
  }

  // Encoding s_unitSystems: SEQUENCE OF 

    f2 = new BEREncoding[s_unitSystems.length];

    for (p = 0; p < s_unitSystems.length; p++) {
      f2[p] = s_unitSystems[p].ber_encode();
    }

    fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 11, f2);

  return new BERConstructed(tag_type, tag, fields);
}

//----------------------------------------------------------------
/**
 * Returns a new String object containing a text representing
 * of the AccessInfo. 
 */

public String
toString()
{
  int p;
  StringBuffer str = new StringBuffer("{");
  int outputted = 0;

  if (s_queryTypesSupported != null) {
    str.append("queryTypesSupported ");
    str.append("{");
    for (p = 0; p < s_queryTypesSupported.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_queryTypesSupported[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_diagnosticsSets != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("diagnosticsSets ");
    str.append("{");
    for (p = 0; p < s_diagnosticsSets.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_diagnosticsSets[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_attributeSetIds != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("attributeSetIds ");
    str.append("{");
    for (p = 0; p < s_attributeSetIds.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_attributeSetIds[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_schemas != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("schemas ");
    str.append("{");
    for (p = 0; p < s_schemas.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_schemas[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_recordSyntaxes != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("recordSyntaxes ");
    str.append("{");
    for (p = 0; p < s_recordSyntaxes.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_recordSyntaxes[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_resourceChallenges != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("resourceChallenges ");
    str.append("{");
    for (p = 0; p < s_resourceChallenges.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_resourceChallenges[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_restrictedAccess != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("restrictedAccess ");
    str.append(s_restrictedAccess);
    outputted++;
  }

  if (s_costInfo != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("costInfo ");
    str.append(s_costInfo);
    outputted++;
  }

  if (s_variantSets != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("variantSets ");
    str.append("{");
    for (p = 0; p < s_variantSets.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_variantSets[p]);
    }
    str.append("}");
    outputted++;
  }

  if (s_elementSetNames != null) {
    if (0 < outputted)
    str.append(", ");
    str.append("elementSetNames ");
    str.append("{");
    for (p = 0; p < s_elementSetNames.length; p++) {
      if (p != 0)
        str.append(", ");
      str.append(s_elementSetNames[p]);
    }
    str.append("}");
    outputted++;
  }

  if (0 < outputted)
    str.append(", ");
  str.append("unitSystems ");
  str.append("{");
  for (p = 0; p < s_unitSystems.length; p++) {
    if (p != 0)
      str.append(", ");
    str.append(s_unitSystems[p]);
  }
  str.append("}");
  outputted++;

  str.append("}");

  return str.toString();
}

//----------------------------------------------------------------
/*
 * Internal variables for class.
 */

public QueryTypeDetails s_queryTypesSupported[]; // optional
public ASN1ObjectIdentifier s_diagnosticsSets[]; // optional
public AttributeSetId s_attributeSetIds[]; // optional
public ASN1ObjectIdentifier s_schemas[]; // optional
public ASN1ObjectIdentifier s_recordSyntaxes[]; // optional
public ASN1ObjectIdentifier s_resourceChallenges[]; // optional
public AccessRestrictions s_restrictedAccess; // optional
public Costs s_costInfo; // optional
public ASN1ObjectIdentifier s_variantSets[]; // optional
public ElementSetName s_elementSetNames[]; // optional
public InternationalString s_unitSystems[];

} // AccessInfo

//----------------------------------------------------------------
//EOF