/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
import java.util.Properties;

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.osaf.cosmo.calendar.util.TimeZoneUtils;

/**
 * Provides methods for translating an arbitrary timezone into
 * an equivalent Olson timezone.  Translation relies on a set of
 * known timezone aliases, plus timezone rule comparison to
 * the set of Olson timezones.
 */
public class TimeZoneTranslator {

    // mapping of known timezone aliases to Olson tzids
    private final Properties ALIASES = new Properties();

    private final TimeZoneRegistry REGISTRY = TimeZoneRegistryFactory.getInstance()
            .createRegistry();

    private static final TimeZoneTranslator instance = new TimeZoneTranslator();

    private TimeZoneTranslator() {
        try {
            ALIASES.load(TimeZoneTranslator.class
                    .getResourceAsStream("/timezone.alias"));
        } catch (IOException e) {
            throw new RuntimeException("Error parsing tz aliases");
        }
    }

    public static TimeZoneTranslator getInstance() {
        return instance;
    }

    /**
     * Given a timezone and date, return the equivalent Olson timezone.
     * @param timezone timezone
     * @return equivalent Olson timezone
     */
    public TimeZone translateToOlsonTz(TimeZone timezone) {

        // First use registry to find Olson tz
        TimeZone translatedTz = REGISTRY.getTimeZone(timezone.getID());
        if(translatedTz!=null)
            return translatedTz;

        // Next check for known aliases
        String aliasedTzId = ALIASES.getProperty(timezone.getID());

        // If an aliased id was found, return the Olson tz from the registry
        if(aliasedTzId!=null)
            return REGISTRY.getTimeZone(aliasedTzId);

        // Try to find a substring match
        translatedTz = findSubStringMatch(timezone.getID());
        if(translatedTz!=null)
            return translatedTz;

        // Try to find equivalent timezone based on rule match
        // TODO: fix as this really doesn't work
        //String equivalentId = TimeZoneUtils.getEquivalentTimeZoneId(timezone.getVTimeZone());
        //if(equivalentId!=null)
            //return REGISTRY.getTimeZone(equivalentId);

        // failed to find match
        return null;
    }

    /**
     * Given a timezone id, return the equivalent Olson timezone.
     * @param tzId timezone id to translate
     * @return equivalent Olson timezone
     */
    public TimeZone translateToOlsonTz(String tzId) {

        // First use registry to find Olson tz
        TimeZone translatedTz = REGISTRY.getTimeZone(tzId);
        if(translatedTz!=null)
            return translatedTz;

        // Next check for known aliases
        String aliasedTzId = ALIASES.getProperty(tzId);

        // If an aliased id was found, return the Olson tz from the registry
        if(aliasedTzId!=null)
            return REGISTRY.getTimeZone(aliasedTzId);

        // Try to find a substring match
        translatedTz = findSubStringMatch(tzId);
        if(translatedTz!=null)
            return translatedTz;

        // failed to find match
        return null;
    }


    /**
     * Attempt to find a matching Olson timezone by searching for
     * a valid Olson TZID in the specified string at the end.  This
     * happens to be what Lightning does as its timezones look
     * like: TZID=/mozilla.org/20050126_1/America/Los_Angeles
     * @param tzname
     * @return matching Olson timezone, null if no match found
     */
    protected TimeZone findSubStringMatch(String tzname) {
        for(String id: TimeZoneUtils.getTimeZoneIds())
            if(tzname.endsWith(id))
                return REGISTRY.getTimeZone(id);

        return null;
    }
}
