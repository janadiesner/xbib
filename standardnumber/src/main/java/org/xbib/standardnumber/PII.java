package org.xbib.standardnumber;

/**
 * Publisher Item Identifier
 *
 * The Publisher Item Identifier (PII) was agreed in 1995 by a group of Scientific
 * and Technical Information publishers calling themselves the STI group and consisting of
 * the American Chemical Society, American Institute of Physics, American Physical
 * Society, Elsevier Science and IEEE. It was developed as an identifier for internal use and
 * exchange between consortia partners. It was closely modelled on the Elsevier Standard
 * Serial Document Identifier and the ADONIS number, both of which it has replaced.
 *
 * The PII is a 17 character string made up of
 *
 * - one character to indicate source publication type (S for serial, B for book)
 * - the identification code (ISSN or ISBN) of the publication type (serial or book)
 *   to which the publication item is primarily assigned
 * - (in the case of serials only) an additional two digit number. A suggested
 *   possibility to ensure uniqueness is the calendar year (final two digits) of the date of assignment (this is not necessarily identical to the cover date)
 * - a number unique to the publication item within the publication type
 * - a check digit
 *
 * Check digit algorithm
 *
 * To control for errors in transmission or keying, a check digit is incorporated in the PII.
 * The PII carries a check digit that is calculated over the entire number following the
 * declaration (15 digits), being modulo 11 of the sum of the weighted digits, the weights
 * being the first sixteen primes excluding 11 (the modulo check):
 *
 * Example: An article is assigned number 403 in the year 1996 in the journal with
 * ISSN 0165-3806 (Dev. Brain Res.). PII (without check digit) is:
 *
 *  S0   1   6   5   3   8   0   6   9   6   0   0   4   0   3
 *  The digits have weights:
 *
 *  53  47  43  41  37  31  29  23  19  17  13   7   5   3   2
 *  Multiplication gives:
 *
 *  0  47 258 205 111 248   0 138 171 102   0   0  20   0   6
 *  with sum 1306. Division by 11 gives remainder 8, which becomes the check character.
 *  The PII will therefore be: S0165-3806(96)00403-8
 *
 *  If the remainder is 10 the check digit will be 'X' (capital).
 *  If the ISSN or ISBN includes a character X this is calculated as the value 10.
 *  The use of prime number weights minimises the chance of self-cancelling double errors.
 *  The use of modulo 11 allows the use of a single digit for the check digit.
 *
 * @see <a href="http://www.bic.org.uk/files/pdfs/uniquid.pdf">Green, Bide: Unique Identifiers: a brief introduction, 1999</a>
 */
public class PII {
    // TODO
}
