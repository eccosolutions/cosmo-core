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
package org.osaf.cosmo.eim.eimml;

import org.osaf.cosmo.eim.EimException;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.EimRecordSetIterator;

/**
 * Iterator that reads EIM recordsets off of an EIMML stream.
 *
 * @see EimRecordSet
 */
public class EimmlStreamReaderIterator implements EimRecordSetIterator {

    private final EimmlStreamReader reader;

    public EimmlStreamReaderIterator(EimmlStreamReader reader) {
        this.reader = reader;
    }

    public boolean hasNext()
        throws EimException {
        return reader.hasNext();
    }

    public EimRecordSet next()
        throws EimException {
        return reader.nextRecordSet();
    }
}
