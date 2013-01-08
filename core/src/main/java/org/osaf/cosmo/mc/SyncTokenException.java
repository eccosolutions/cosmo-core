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
package org.osaf.cosmo.mc;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An exception related to sync tokens (signifying, for example, a
 * non-well-formed token).
 */
public class SyncTokenException extends MorseCodeException {

    private String token = null;
    
    /** */
    public SyncTokenException(String token) {
        super(400, "invalid sync token: " + token);
        this.token = token;
    }
    
    protected void writeContent(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeStartElement(NS_MC, "invalid-synctoken");
        writer.writeStartElement(NS_MC, "token");
        writer.writeCharacters(token);
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
