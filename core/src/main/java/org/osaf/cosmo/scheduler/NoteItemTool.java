/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.scheduler;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.TimeZone;

import org.apache.velocity.tools.config.DefaultKey;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.util.NoteUtils;

/**
 * Velocity tool that provides helpful functions when dealing with NoteItems.
 * 
 */
@DefaultKey("noteTool")
public class NoteItemTool {

    public NoteItemTool() {
    }

    public boolean isEvent(Object note) {
        if (!isNote(note))
            return false;

        return NoteUtils.isEvent((NoteItem) note);
    }

    public boolean isTask(Object note) {
        if (!isNote(note))
            return false;

        return NoteUtils.isTask((NoteItem) note);
    }

    public Object getLocation(Object note) {
        if (!isNote(note))
            return null;

        String loc = NoteUtils.getLocation((NoteItem) note);
        return (loc != null) ? loc : "";
    }

    public boolean hasCustomAlarm(Object note) {
        if (!isNote(note))
            return false;

        return NoteUtils.hasCustomAlarm((NoteItem) note);
    }

    public Object getAlarmDate(Object note) {
        if (!isNote(note))
            return null;

        return NoteUtils.getCustomAlarm((NoteItem) note);
    }

    public Object getStartDate(Object note) {
        if (!isNote(note))
            return null;

        return NoteUtils.getStartDate((NoteItem) note);
    }

    public Object getEndDate(Object note) {
        if (!isNote(note))
            return null;

        return NoteUtils.getEndDate((NoteItem) note);
    }

    public Object normalizeDate(Object date, Object tz) {
        if (!isDate(date))
            return null;

        // need a net.fortuna.ical4j TimeZone, otherwise just return the date
        if (!(tz instanceof TimeZone))
            return date;

        return NoteUtils.getNormalizedDate((Date) date, (TimeZone) tz);
    }

    private boolean isNote(Object obj) {
        return (obj instanceof NoteItem);
    }

    private boolean isDate(Object obj) {
        return (obj instanceof Date);
    }
}
