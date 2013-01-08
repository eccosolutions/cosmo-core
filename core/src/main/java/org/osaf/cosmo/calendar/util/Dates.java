/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar.util;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for dealing with dates.
 *
 */
public class Dates {
    
    private static final Log log =
        LogFactory.getLog(Dates.class);
    
    
    /**
     * Returns a new date instance matching the type and timezone of the other
     * date. If no type is specified a DateTime instance is returned.
     * 
     * @param date
     *            a seed Java date instance
     * @param type
     *            the type of date instance
     * @return an instance of <code>net.fortuna.ical4j.model.Date</code>
     */
    public static Date getInstance(final java.util.Date date, final Date type) {
        if ((type == null) || (type instanceof DateTime)) {
            DateTime dt = new DateTime(date);
            if (((DateTime) type).isUtc()) {
                dt.setUtc(true);
            } else {
                dt.setTimeZone(((DateTime) type).getTimeZone());
            }
            return dt;
        } else
            return new Date(date);
    }
}
