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
package org.osaf.cosmo.dao;

import java.util.Set;

import net.fortuna.ical4j.model.DateTime;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ICalendarItem;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for DAO that provides query apis for finding
 * ContentItems with EventStamps matching certain criteria.
 *
 */
public interface CalendarDao {

    /**
     * Find calendar event with a specified icalendar uid. The icalendar format
     * requires that an event's uid is unique within a calendar.
     *
     * @param uid
     *            icalendar uid of calendar event
     * @param collection
     *            collection to search
     * @return calendar event represented by uid and calendar
     */
    @Transactional(readOnly = true)
    ContentItem findEventByIcalUid(String uid,
            CollectionItem collection);


    /**
     * Find calendar items by calendar filter.  Calendar filter is
     * based on the CalDAV filter element.
     *
     * @param collection
     *            collection to search
     * @param filter
     *            filter to use in search
     * @return set ICalendar objects that match specified
     *         filter.
     */
    @Transactional(readOnly = true)
    Set<ICalendarItem> findCalendarItems(CollectionItem collection,
                                             CalendarFilter filter);

    /**
     * Find calendar events by time range.
     *
     * @param collection
     *            collection to search
     * @param rangeStart time range start
     * @param rangeEnd time range end
     * @param expandRecurringEvents if true, recurring events will be expanded
     *        and each occurrence will be returned as a NoteItemOccurrence.
     * @return set ContentItem objects that contain EventStamps that occur
     *         int the given timeRange.
     */
    @Transactional(readOnly = true)
    Set<ContentItem> findEvents(CollectionItem collection,
                                             DateTime rangeStart, DateTime rangeEnd,
                                             boolean expandRecurringEvents);

}
