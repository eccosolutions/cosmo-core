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
package org.osaf.cosmo.model;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Trigger;

import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * Represents a stamp that contains event specific information.
 */
public interface BaseEventStamp extends Stamp {

    /**
     * Get the underlying VEvent
     * @return VEvent
     */
    VEvent getEvent();

    /**
     * Get the Calendar that contains the VEvent
     * @return Calendar containing VEvent
     */
    Calendar getEventCalendar();

    /**
     * Set the Calendar that contains the VEvent
     * @param calendar Calendar containing VEvent
     */
    void setEventCalendar(Calendar calendar);

    /**
     * Returns a copy of the the iCalendar UID property value of the
     * event .
     */
    String getIcalUid();

    /**
     * Sets the iCalendar UID property of the event.
     *
     * @param uid uid of VEVENT
     */
    void setIcalUid(String uid);

    /**
     * Sets the iCalendar SUMMARY property of the event.
     *
     * @param text a text string
     */
    void setSummary(String text);

    /**
     * Sets the iCalendar DESCRIPTION property of the event.
     *
     * @param text a text string
     */
    void setDescription(String text);

    /**
     * Returns a copy of the the iCalendar DTSTART property value of
     * the event (never null).
     */
    Date getStartDate();

    /**
     * Sets the iCalendar DTSTART property of the event.
     *
     * @param date a <code>Date</code>
     */
    void setStartDate(Date date);

    /**
     * Returns the end date of the event as calculated from the
     * iCalendar DTEND property value or the the iCalendar DTSTART +
     * DURATION.
     */
    Date getEndDate();

    /**
     * Sets the iCalendar DTEND property of the event.
     *
     * @param date a <code>Date</code>
     */
    void setEndDate(Date date);

    /**
     * Returns the duration of the event as calculated from the
     * iCalendar DURATION property value or the the iCalendar DTEND -
     * DTSTART.
     * @return
     */
    TemporalAmount getDuration();

    /**
     * Sets the iCalendar DURATION property of the event.
     *
     * @param dur a <code>Dur</code>
     */
    void setDuration(TemporalAmount dur);

    /**
     * Returns a copy of the the iCalendar LOCATION property value of
     * the event (can be null).
     */
    String getLocation();

    /**
     * Sets the iCalendar LOCATION property of the event.
     *
     * @param text a text string
     */
    void setLocation(String text);

    /**
     * Returns a list of copies of the iCalendar RRULE property values
     * of the event (can be empty).
     */
    List<Recur> getRecurrenceRules();

    /**
     * Sets the iCalendar RRULE properties of the event,
     * removing any RRULEs that were previously set.
     *
     * @param recurs a <code>List</code> of <code>Recur</code>s
     */
    void setRecurrenceRules(List<Recur> recurs);

    /**
     * Sets a single iCalendar RRULE property of the event,
     * removing any RRULEs that were previously set.
     *
     * @param recur a <code>Recur</code>
     */
    void setRecurrenceRule(Recur recur);

    /**
     * Returns a list of copies of the iCalendar EXRULE property values
     * of the event (can be empty).
     */
    List<Recur> getExceptionRules();

    /**
     * Sets the iCalendar EXRULE properties of the event,
     * removing any EXRULEs that were previously set.
     *
     * @param recurs a <code>List</code> of <code>Recur</code>s
     */
    void setExceptionRules(List<Recur> recurs);

    /**
     * Returns a list of copies of the iCalendar RDATE property values
     * of the event (can be empty).
     */
    DateList getRecurrenceDates();

    /**
     * Sets a single iCalendar RDATE property of the event,
     * removing any RDATEs that were previously set.
     *
     * @param dates a <code>DateList</code>
     */
    void setRecurrenceDates(DateList dates);

    /**
     * Returns a list of copies of the values of all iCalendar EXDATE
     * properties of the event (can be empty).
     */
    DateList getExceptionDates();

    /**
     * Return the first display alarm on the event
     * @return first display alarm on event
     */
    VAlarm getDisplayAlarm();

    /**
     * Remove first display alarm on the event
     */
    void removeDisplayAlarm();

    /**
     * Return the description of the first display alarm on the event.
     * @return alarm description
     */
    String getDisplayAlarmDescription();

    /**
     * Set the description of the first display alarm on the event.
     * @param newDescription display alarm description
     */
    void setDisplayAlarmDescription(String newDescription);

    /**
     * Return the Trigger of the first display alarm on the event
     * @return trigger of the first display alarm
     */
    Trigger getDisplayAlarmTrigger();

    /**
     * Set the trigger property of the first display alarm on the event.
     * @param newTrigger trigger
     */
    void setDisplayAlarmTrigger(Trigger newTrigger);

    /**
     * Set the trigger property of the first display alarm on the event
     * to be a absolute trigger.
     * @param triggerDate date display alarm triggers
     */
    void setDisplayAlarmTriggerDate(DateTime triggerDate);

    /**
     * Return the duration of the first display alarm on the event
     * @return duration of the first display alarm
     */
    TemporalAmount getDisplayAlarmDuration();

    /**
     * Set the durcation of the first display alarm on the event
     * @param dur duration
     */
    void setDisplayAlarmDuration(TemporalAmount dur);

    /**
     * Return the repeat count on the first display alarm on the event
     * @return repeat count of the first display alarm on the event
     */
    Integer getDisplayAlarmRepeat();

    /**
     * Set the repeat count on the first display alarm on the event.
     * @param count repeat count of the first display alarm.
     */
    void setDisplayAlarmRepeat(Integer count);

    /**
     * Sets a single iCalendar EXDATE property of the event,
     * removing any EXDATEs that were previously set.
     *
     * @param dates a <code>DateList</code>
     */
    void setExceptionDates(DateList dates);

    /**
     * Returns a copy of the the iCalendar RECURRENCE_ID property
     * value of the event (can be null).
     */
    Date getRecurrenceId();

    /**
     * Sets the iCalendar RECURRENCE_ID property of the event.
     *
     * @param date a <code>Date</code>
     */
    void setRecurrenceId(Date date);

    /**
     * Returns a copy of the the iCalendar STATUS property value of
     * the event (can be null).
     */
    String getStatus();

    /**
     * Sets the iCalendar STATUS property of the event.
     *
     * @param text a text string
     */
    void setStatus(String text);

    /**
     * Is the event marked as anytime.
     * @return true if the event is an anytime event
     */
    Boolean isAnyTime();

    /**
     * Same as isAnyTime()
     * @return true if the event is an anytime event
     */
    Boolean getAnyTime();

    /**
     * Toggle the event anytime parameter.
     * @param isAnyTime true if the event occurs anytime
     */
    void setAnyTime(Boolean isAnyTime);

    /**
     * Initializes the Calendar with a default master event.
     * Initializes the master event using the underlying item's
     * icalUid (if NoteItem) or uid.
     */
    void createCalendar();

    /**
     * Determine if an event is recurring
     * @return true if the underlying event is a recurring event
     */
    boolean isRecurring();

    /**
     * Create and add new display alarm on event.
     */
    void creatDisplayAlarm();

}