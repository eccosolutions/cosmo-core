/*
 * Copyright 2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.calendar;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parser for a simplified iCalendar property value serialization
 * scheme used in circumstances where iCalendar itself is too
 * heavyweight.
 * <p>
 * When parameters are included with a property value, they are
 * serialized using iCalendar syntax. The resulting string looks like
 * the equivalent iCalendar property without the leading property
 * name; the string begins with a semicolon, each property
 * name/value pair is separated by semicolons, and the last property
 * is separated from the text value by a colon. Parameter values may
 * optionally be surrounded with double quotes.
 * <p>
 * When no parameters are included, the serialized string is simply
 * the property value itself.
 * <p>
 * quotes. Parameter and property values are <em>not</em> wrapped
 * according to iCalendar; any whitespace in these values is
 * considered to be significant.
 * <p>
 * Examples:
 * <dl>
 * <dt><code>:TZID="America/Los_Angeles";VALUE=DATE_TIME:20060120T120000</code></dt>
 * <dd>A value for an event record's <code>dtstart</code> field
 * including properties, one with a double quoted value.</dd>
 * <dt><code>20060120T120000</code></dt>
 * <dd>A value for an event record's <code>dtstart</code> field
 * without properties.
 * </dl>
 */
public class ICalValueParser {
    private static final Log log = LogFactory.getLog(ICalValueParser.class);

    private final StreamTokenizer tokenizer;
    private String value;
    private final HashMap<String, String> params;

    // tokenizer code based on ical4j's CalendarParserImpl

    /**
     * Constructs a parser instance for the given EIM text value.
     */
    public ICalValueParser(String text) {
        tokenizer = new StreamTokenizer(new StringReader(text));
        value = text;
        params = new HashMap<>();

        tokenizer.resetSyntax();
        tokenizer.wordChars(32, 126);
        tokenizer.whitespaceChars(0, 20);
        tokenizer.ordinaryChar(':');
        tokenizer.ordinaryChar(';');
        tokenizer.ordinaryChar('=');
        tokenizer.eolIsSignificant(false);
        tokenizer.whitespaceChars(0, 0);
        tokenizer.quoteChar('"');
    }

    /**
     * Parses the text value.
     */
    public void parse()
        throws ParseException {
        int nextToken = nextToken();
        // log.debug("starting token: " + tokenizer);
        if (nextToken != ';')
            return;

        nextToken = nextToken();
        while (nextToken != ':' &&
               nextToken != StreamTokenizer.TT_EOF) {
            // log.debug("param name token: " + tokenizer);
            if (nextToken != StreamTokenizer.TT_WORD)
                throw new ParseException("expected word, read " + tokenizer.ttype, 1);
            String name = tokenizer.sval;

            nextToken = nextToken();
            // log.debug("param = token: " + tokenizer);
            if (nextToken != '=')
                throw new ParseException("expected =, read " + tokenizer.ttype, 1);

            nextToken = nextToken();
            // log.debug("param val token: " + tokenizer);
            if (! (nextToken == StreamTokenizer.TT_WORD ||
                   nextToken == '"'))
                throw new ParseException("expected word, read " + tokenizer.ttype, 1);
            String value = tokenizer.sval;

            // log.debug("parameter " + name + ": " + value);

            params.put(name, value);

            nextToken = nextToken();
            // log.debug("post param token: " + tokenizer);

            if (nextToken == ':')
                break;
            else if (nextToken == ';')
                nextToken = nextToken();
            else
                throw new ParseException("expected either : or ;, read " + tokenizer.ttype, 1);
        }

        nextToken = nextToken();
        // log.debug("prop val token: " + tokenizer);
        if (nextToken != StreamTokenizer.TT_WORD)
            throw new ParseException("expected " + StreamTokenizer.TT_WORD + ", read " + tokenizer.ttype, 1);
        value = tokenizer.sval;

        // log.debug("property: " + value + ", params: " + params);
    }

    public String getValue() {
        return value;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    private int nextToken() {
        try {
            return tokenizer.nextToken();
        } catch (IOException e) {
            // StringReader only throws IOException in extremely
            // bizarre situations
            throw new RuntimeException("Failure reading ical string", e);
        }
    }
}
