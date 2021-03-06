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
package org.osaf.cosmo.eim.schema.note;

import org.osaf.cosmo.model.NoteItem;

/**
 * Constants related to the note schema.
 *
 * @see NoteItem
 */
public interface NoteConstants {
    /** */
    public static final String FIELD_BODY = "body";
    /** */
    public static final String FIELD_ICALUID = "icalUid";
    /** */
    public static final String FIELD_PARENTUUID = "parentUuid";
    /** */
    public static final int MAXLEN_ICALUID = 256;
    /** */
    public static final int MAXLEN_PARENTUUID = 256;
}
