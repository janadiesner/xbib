package org.semarglproject.jsonld;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semarglproject.sink.AbstractPipe;
import org.semarglproject.sink.CharSink;
import org.semarglproject.sink.QuadSink;
import org.semarglproject.sink.TripleSink;

import java.io.EOFException;
import java.io.IOException;
import java.util.BitSet;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Implementation of streaming <a href="http://www.w3.org/TR/2013/WD-json-ld-20130411/">JSON-LD</a> parser.
 * Parser requires @id properties to be declared before predicates for each non-blank JSON-LD node.
 */
public final class JsonLdReader extends AbstractPipe<TripleSink> implements CharSink {

    private final static Logger logger = LogManager.getLogger(JsonLdReader.class);

    private static final short PARSING_ARRAY_BEFORE_VALUE = 1;
    private static final short PARSING_OBJECT_BEFORE_KEY = 2;
    private static final short PARSING_OBJECT_BEFORE_VALUE = 3;
    private static final short PARSING_STRING = 4;
    private static final short PARSING_NUMBER = 5;
    private static final short PARSING_NAMED_LITERAL = 6;
    private static final short PARSING_OBJECT_BEFORE_COLON = 7;
    private static final short PARSING_OBJECT_BEFORE_COMMA = 8;
    private static final short PARSING_ARRAY_BEFORE_COMMA = 9;

    private static final BitSet WHITESPACE = new BitSet();

    private static final BitSet NAMED_LITERAL_CHAR = new BitSet();

    static {
        WHITESPACE.set('\t');
        WHITESPACE.set(' ');
        WHITESPACE.set('\r');
        WHITESPACE.set('\n');

        NAMED_LITERAL_CHAR.set('t');
        NAMED_LITERAL_CHAR.set('r');
        NAMED_LITERAL_CHAR.set('u');
        NAMED_LITERAL_CHAR.set('e');
        NAMED_LITERAL_CHAR.set('f');
        NAMED_LITERAL_CHAR.set('a');
        NAMED_LITERAL_CHAR.set('l');
        NAMED_LITERAL_CHAR.set('s');
        NAMED_LITERAL_CHAR.set('n');
        NAMED_LITERAL_CHAR.set('0');
        NAMED_LITERAL_CHAR.set('1');
        NAMED_LITERAL_CHAR.set('2');
        NAMED_LITERAL_CHAR.set('3');
        NAMED_LITERAL_CHAR.set('4');
        NAMED_LITERAL_CHAR.set('5');
        NAMED_LITERAL_CHAR.set('6');
        NAMED_LITERAL_CHAR.set('7');
        NAMED_LITERAL_CHAR.set('8');
        NAMED_LITERAL_CHAR.set('9');
        NAMED_LITERAL_CHAR.set('.');
        NAMED_LITERAL_CHAR.set('-');
        NAMED_LITERAL_CHAR.set('E');
        NAMED_LITERAL_CHAR.set('+');
    }

    private JsonLdContentHandler contentHandler;

    private Deque<Short> stateStack = new LinkedList<Short>();

    private short parsingState;

    private int tokenStartPos;

    private short charsToEscape = 0;

    private StringBuilder addBuffer = null;

    private JsonLdReader(QuadSink sink) {
        super(sink);
        contentHandler = new JsonLdContentHandler(sink);
    }

    /**
     * Creates instance of JsonLdParser connected to specified sink.
     *
     * @param sink sink to be connected to
     * @return instance of JsonLdParser
     */
    public static CharSink connect(QuadSink sink) {
        return new JsonLdReader(sink);
    }

    @Override
    public JsonLdReader process(String str) throws IOException {
        return process(str.toCharArray(), 0, str.length());
    }

    @Override
    public JsonLdReader process(char ch) throws IOException {
        char[] buffer = new char[1];
        buffer[0] = ch;
        return process(buffer, 0, 1);
    }

    @Override
    public JsonLdReader process(char[] buffer, int start, int count) throws IOException {
        if (tokenStartPos != -1) {
            tokenStartPos = start;
        }
        int end = start + count;
        for (int pos = start; pos < end; pos++) {
            if (parsingState == PARSING_ARRAY_BEFORE_VALUE || parsingState == PARSING_OBJECT_BEFORE_VALUE
                    || parsingState == PARSING_OBJECT_BEFORE_KEY) {
                processValueChar(buffer, pos);
            } else if (parsingState == PARSING_STRING) {
                processStringChar(buffer, pos);
            } else if (parsingState == PARSING_OBJECT_BEFORE_COMMA) {
                if (buffer[pos] == ',') {
                    parsingState = PARSING_OBJECT_BEFORE_KEY;
                } else if (buffer[pos] == '}') {
                    parsingState = stateStack.pop();
                    contentHandler.onObjectEnd();
                    onValue();
                } else if (!WHITESPACE.get(buffer[pos])) {
                    logger.error("unexpected character '" + buffer[pos] + "'");
                }
            } else if (parsingState == PARSING_ARRAY_BEFORE_COMMA) {
                if (buffer[pos] == ',') {
                    parsingState = PARSING_ARRAY_BEFORE_VALUE;
                } else if (buffer[pos] == ']') {
                    parsingState = stateStack.pop();
                    contentHandler.onArrayEnd();
                    onValue();
                } else if (!WHITESPACE.get(buffer[pos])) {
                    logger.error("unexpected character '" + buffer[pos] + "'");
                }
            } else if (parsingState == PARSING_OBJECT_BEFORE_COLON) {
                if (buffer[pos] == ':') {
                    parsingState = PARSING_OBJECT_BEFORE_VALUE;
                } else if (!WHITESPACE.get(buffer[pos])) {
                    logger.error("unexpected character '" + buffer[pos] + "'");
                }
            } else if (parsingState == PARSING_NAMED_LITERAL || parsingState == PARSING_NUMBER) {
                if (!NAMED_LITERAL_CHAR.get(buffer[pos])) {
                    String value = unescape(extractToken(buffer, pos - 1, 0));
                    if (parsingState == PARSING_NAMED_LITERAL) {
                        if ("true".equals(value)) {
                            contentHandler.onBoolean(true);
                        } else if ("false".equals(value)) {
                            contentHandler.onBoolean(false);
                        } else if ("null".equals(value)) {
                            contentHandler.onNull();
                        } else {
                            logger.error("unexpected value '" + value + "'");
                        }
                    } else {
                        if (value.contains(".") || value.contains("E") || value.contains("e")) {
                            contentHandler.onNumber(Double.valueOf(value));
                        } else {
                            contentHandler.onNumber(Integer.valueOf(value));
                        }
                    }
                    parsingState = stateStack.pop();
                    if (parsingState == PARSING_ARRAY_BEFORE_VALUE) {
                        parsingState = PARSING_ARRAY_BEFORE_COMMA;
                    } else if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
                        parsingState = PARSING_OBJECT_BEFORE_COMMA;
                    }
                    pos--;
                }
            }
        }
        if (tokenStartPos != -1) {
            if (addBuffer == null) {
                addBuffer = new StringBuilder();
            }
            addBuffer.append(buffer, tokenStartPos, end - tokenStartPos);
        }
        return this;
    }

    private void processStringChar(char[] buffer, int pos) throws IOException {
        if (charsToEscape > 0) {
            charsToEscape--;
        } else {
            if (buffer[pos] == '\"') {
                parsingState = stateStack.pop();
                String value = unescape(extractToken(buffer, pos, 1));
                if (parsingState == PARSING_OBJECT_BEFORE_KEY) {
                    contentHandler.onKey(value);
                    parsingState = PARSING_OBJECT_BEFORE_COLON;
                } else if (parsingState == PARSING_ARRAY_BEFORE_VALUE) {
                    contentHandler.onString(value);
                    parsingState = PARSING_ARRAY_BEFORE_COMMA;
                } else if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
                    contentHandler.onString(value);
                    parsingState = PARSING_OBJECT_BEFORE_COMMA;
                }
            } else if (buffer[pos] == '\\') {
                charsToEscape = 1;
            }
        }
    }

    private void processValueChar(char[] buffer, int pos) throws IOException {
        switch (buffer[pos]) {
            case '{':
                stateStack.push(parsingState);
                parsingState = PARSING_OBJECT_BEFORE_KEY;
                contentHandler.onObjectStart();
                break;
            case '}':
                if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
                    logger.error("unexpected object end");
                }
                parsingState = stateStack.pop();
                contentHandler.onObjectEnd();
                onValue();
                break;
            case '[':
                stateStack.push(parsingState);
                parsingState = PARSING_ARRAY_BEFORE_VALUE;
                contentHandler.onArrayStart();
                break;
            case ']':
                parsingState = stateStack.pop();
                contentHandler.onArrayEnd();
                onValue();
                break;
            case 't':
            case 'f':
            case 'n':
                stateStack.push(parsingState);
                parsingState = PARSING_NAMED_LITERAL;
                tokenStartPos = pos;
                break;
            case '\"':
                stateStack.push(parsingState);
                parsingState = PARSING_STRING;
                tokenStartPos = pos;
                break;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                stateStack.push(parsingState);
                parsingState = PARSING_NUMBER;
                tokenStartPos = pos;
                break;
            default:
                if (!WHITESPACE.get(buffer[pos])) {
                    logger.error("Unexpected character '" + buffer[pos] + "'");
                }
        }
    }

    private void onValue() {
        if (parsingState == PARSING_ARRAY_BEFORE_VALUE) {
            parsingState = PARSING_ARRAY_BEFORE_COMMA;
        } else if (parsingState == PARSING_OBJECT_BEFORE_VALUE) {
            parsingState = PARSING_OBJECT_BEFORE_COMMA;
        }
    }

    @Override
    public void setBaseUri(String baseUri) {
        contentHandler.setBaseUri(baseUri);
    }

    private String extractToken(char[] buffer, int tokenEndPos, int trimSize) throws IOException {
        String saved;
        if (addBuffer != null) {
            if (tokenEndPos - trimSize >= tokenStartPos) {
                addBuffer.append(buffer, tokenStartPos, tokenEndPos - tokenStartPos - trimSize + 1);
            }
            addBuffer.delete(0, trimSize);
            saved = addBuffer.toString();
            addBuffer = null;
        } else {
            saved = String.valueOf(buffer, tokenStartPos + trimSize, tokenEndPos - tokenStartPos + 1 - 2 * trimSize);
        }
        tokenStartPos = -1;
        return saved;
    }

    @Override
    public void startStream() throws IOException {
        super.startStream();
        parsingState = PARSING_ARRAY_BEFORE_VALUE;
        contentHandler.onDocumentStart();
    }

    @Override
    public void endStream() throws IOException {
        super.endStream();
        contentHandler.onDocumentEnd();
        if (tokenStartPos != -1 || !stateStack.isEmpty()) {
            logger.error("unexpected end of stream");
            throw new EOFException("unexpected end of stream");
        }
    }

    private String unescape(String str) throws IOException {
        int limit = str.length();
        StringBuilder result = new StringBuilder(limit);

        for (int i = 0; i < limit; i++) {
            char ch = str.charAt(i);
            if (ch != '\\') {
                result.append(ch);
                continue;
            }
            i++;
            if (i == limit) {
                break;
            }
            ch = str.charAt(i);
            switch (ch) {
                case '\\':
                case '/':
                case '\"':
                    result.append(ch);
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    int sequenceLength = 4;
                    if (i + sequenceLength >= limit) {
                        logger.error("error parsing escape sequence '\\" + ch + "'");
                    }
                    String code = str.substring(i + 1, i + 1 + sequenceLength);
                    i += sequenceLength;

                    try {
                        int value = Integer.parseInt(code, 16);
                        result.append((char) value);
                    } catch (NumberFormatException nfe) {
                        logger.error("error parsing escape sequence '\\" + ch + "'");
                    }
                    break;
                default:
                    result.append(ch);
                    break;
            }
        }
        return result.toString();
    }

}