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
package org.osaf.cosmo.calendar;

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.*;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.calendar.util.Dates;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains utility methods for creating/updating net.fortuna.ical4j
 * objects.
 */
public class ICalendarUtils {

    /**
     * Create a base Calendar containing a single component.
     * @param comp Component to add to the base Calendar
     * @param icalUid uid of component, if null no UID
     *                property will be added to the component
     * @return base Calendar
     */
    public static Calendar createBaseCalendar(CalendarComponent comp, String icalUid) {
        Uid uid = new Uid(icalUid);
        comp.getProperties().add(uid);
        return createBaseCalendar(comp);
    }

    /**
     * Create a base Calendar containing a single component.
     * @param comp Component to add to the base Calendar
     * @return base Calendar
     */
    public static Calendar createBaseCalendar(CalendarComponent comp) {
        Calendar cal = new Calendar();
        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);

        cal.getComponents().add(comp);
        return cal;
    }

    /**
     * Create a base Calendar containing no components.
     * @return base Calendar
     */
    public static Calendar createBaseCalendar() {
        Calendar cal = new Calendar();
        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);

        return cal;
    }

    /**
     * Update the SUMMARY property on a component.
     * @param text SUMMARY value to update.  If null, the SUMMARY property
     *        will be removed
     * @param comp component to update
     */
    public static void setSummary(String text, Component comp) {
        Summary summary = comp.getProperties().getProperty(Property.SUMMARY);
        if (text == null) {
            if (summary != null)
                comp.getProperties().remove(summary);
            return;
        }
        if (summary == null) {
            summary = new Summary();
            comp.getProperties().add(summary);
        }
        summary.setValue(text);
    }

    /**
     * Update the X property on a component.
     * @param property the property to update
     * @param value the value to set
     * @param comp component to update
     */
    public static void setXProperty(String property, String value, Component comp) {
        Property prop = comp.getProperties().getProperty(property);
        if(prop!=null)
            comp.getProperties().remove(prop);

        if (value != null) {
            prop = new XProperty(property, value);
            comp.getProperties().add(prop);
        }
    }

    /**
     * Get X property value from component;
     * @param property x property to get
     * @param comp component
     * @return value of xproperty, null if property does not exist
     */
    public static String getXProperty(String property, Component comp) {
        Property prop = comp.getProperties().getProperty(property);
        if(prop!=null)
            return prop.getValue();
        else
            return null;
    }

    /**
     * Update the DESCRIPTION property on a component.
     * @param text DESCRIPTION value to update.  If null, the DESCRIPTION property
     *        will be removed
     * @param comp component to update
     */
    public static void setDescription(String text, Component comp) {
        Description description = comp.getProperties().getProperty(Property.DESCRIPTION);

        if (text == null) {
            if (description != null)
                comp.getProperties().remove(description);
            return;
        }
        if (description == null) {
            description = new Description();
            comp.getProperties().add(description);
        }
        description.setValue(text);
    }

    /**
     * Update the LOCATION property on a component.
     * @param text LOCATION value to update.  If null, the LOCATION property
     *        will be removed
     * @param comp component to update
     */
    public static void setLocation(String text, Component comp) {
        Location location = comp.getProperties().getProperty(Property.LOCATION);

        if (text == null) {
            if (location != null)
                comp.getProperties().remove(location);
            return;
        }
        if (location == null) {
            location = new Location();
            comp.getProperties().add(location);
        }
        location.setValue(text);
    }

    /**
     * Update the COMPLETED property on a VTODO component.
     * @param date completion date.  If null, the COMPLETED property
     *        will be removed
     * @param vtodo vtodo component to update
     */
    public static void setCompleted(DateTime date, VToDo vtodo) {
        Completed completed = vtodo.getDateCompleted();
        if (completed != null)
            vtodo.getProperties().remove(completed);

        if (date != null) {
            completed = new Completed(date);
            vtodo.getProperties().add(completed);
        }

    }

    /**
     * Update the STATUS property on a VTODO component.
     * @param status status to set.  If null, the STATUS property
     *        will be removed
     * @param vtodo vtodo component to update
     */
    public static void setStatus(Status status, VToDo vtodo) {
        Status currStatus = vtodo.getStatus();
        if (currStatus != null)
            vtodo.getProperties().remove(currStatus);

        if (status != null) {
            vtodo.getProperties().add(status);
        }
    }

    /**
     * Update the DTSTAMP property on a component.
     * @param date DTSTAMP value to update.  If null, the DTSTAMP property
     *        will be removed
     * @param comp component to update
     */
    public static void setDtStamp(java.util.Date date, Component comp) {
        DtStamp dtStamp = comp.getProperties().getProperty(Property.DTSTAMP);

        if (date == null) {
            if (dtStamp != null)
                comp.getProperties().remove(dtStamp);
            return;
        }
        if (dtStamp == null) {
            dtStamp = new DtStamp();
            comp.getProperties().add(dtStamp);
        }

        dtStamp.getDate().setTime(date.getTime());
    }

    /**
     * Update the UID property on a component.
     * @param text UID value to update.  If null, the UID property
     *        will be removed
     * @param comp component to update
     */
    public static void setUid(String text, Component comp) {
        Uid uid = comp.getProperties().getProperty(Property.UID);

        if (text == null) {
            if (uid != null)
                comp.getProperties().remove(uid);
            return;
        }
        if (uid == null) {
            uid = new Uid();
            comp.getProperties().add(uid);
        }
        uid.setValue(text);
    }

    /**
     * Get the duration for an event.  If the DURATION property
     * exist, use that.  Else, calculate duration from DTSTART and
     * DTEND.
     * @param event
     * @return duration for event
     */
    @Nullable
    public static TemporalAmount getDuration(VEvent event) {
        Duration duration = event.getProperties().getProperty(Property.DURATION);
        if (duration != null)
            return duration.getDuration();
        DtStart dtstart = event.getStartDate();
        if (dtstart == null)
            return null;
        DtEnd dtend = event.getEndDate();
        // dtEnd is never null now from getEndDate since Duration can't - we get ZERO duration (= start date)
        // therefore we compare the start=end and if they are the same, reinstate original behaviour - return no duration
        if (dtend == null || dtstart.getValue().equals(dtend.getValue()))
            return null;
        return new Duration(dtstart.getDate(), dtend.getDate()).getDuration();
    }

    /**
     * Set the duration for an event.  If DTEND is present, remove it.
     * @param event
     * @param dur
     */
    public static void setDuration(VEvent event, TemporalAmount dur) {
        Duration duration = event.getProperties().getProperty(Property.DURATION);


        // remove DURATION if dur is null
        if(dur==null) {
            if(duration != null)
                event.getProperties().remove(duration);
            return;
        }

        // update dur on existing DURATION
        if (duration != null)
            duration.setDuration(dur);
        else {
            // remove the dtend if there was one
            DtEnd dtend = event.getEndDate();
            if (dtend != null)
                event.getProperties().remove(dtend);
            duration = new Duration(dur);
            event.getProperties().add(duration);
        }
    }

    /**
     * Construct a new DateTime instance for floating times (no timezone).
     * If the specified date is not floating, then the instance is returned.
     *
     * This allows a floating time to be converted to an instant in time
     * depending on the specified timezone.
     *
     * @param date floating date
     * @param tz timezone
     * @return new DateTime instance representing floating time pinned to
     *         the specified timezone
     */
    public static DateTime pinFloatingTime(Date date, TimeZone tz) {

        try {
            if(date instanceof DateTime) {
                DateTime dt = (DateTime) date;
                if(dt.isUtc() || dt.getTimeZone()!=null)
                    return dt;
                else
                    return new DateTime(date.toString(), tz);
            }
            else
                return new DateTime(date.toString() + "T000000", tz);
        } catch (ParseException e) {
            throw new RuntimeException("error parsing date");
        }
    }

    /**
     * Return a Date instance that represents the day that a point in
     * time translates into local time given a timezone.
     * @param utcDateTime point in time
     * @param tz timezone
     */
    public static Date normalizeUTCDateTimeToDate(DateTime utcDateTime, TimeZone tz) {
        if(!utcDateTime.isUtc())
            throw new IllegalArgumentException("datetime must be utc");

        // if no timezone, use default
        if(tz==null)
            return new Date(utcDateTime);

        DateTime copy = (DateTime) Dates.getInstance(utcDateTime, utcDateTime);
        copy.setTimeZone(tz);

        try {
            return new Date(copy.toString().substring(0, 8));
        } catch (ParseException e) {
            throw new RuntimeException("error creating Date instance");
        }
    }

    /**
     * Return a DateTime instance that is normalized according to the
     * offset of the specified timezone as compared to the default
     * system timezone.
     *
     * @param utcDateTime point in time
     * @param tz timezone
     */
    public static Date normalizeUTCDateTimeToDefaultOffset(DateTime utcDateTime, TimeZone tz) {
        if(!utcDateTime.isUtc())
            throw new IllegalArgumentException("datetime must be utc");

        // if no timezone nothing to do
        if(tz==null)
            return utcDateTime;

        // create copy, and set timezone
        DateTime copy = (DateTime) Dates.getInstance(utcDateTime, utcDateTime);
        copy.setTimeZone(tz);


        // Create floating instance of local time, which will give
        // us the correct offset
        try {
            return new DateTime(copy.toString());
        } catch (ParseException e) {
            throw new RuntimeException("error creating Date instance");
        }
    }

    /**
     * Convert a Date instance to a utc DateTime instance for a
     * specified timezone.
     * @param date date to convert
     * @param tz timezone
     * @return UTC DateTime instance
     */
    public static DateTime convertToUTC(Date date, TimeZone tz) {

        // handle DateTime
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;

            // if utc already, then nothing to do
            if(dt.isUtc())
                return dt;

            // if DateTime has timezone, then create copy, set to utc
            if(dt.getTimeZone()!=null) {
                dt = (DateTime) Dates.getInstance(date, date);
                dt.setUtc(true);
                return dt;
            }

            // otherwise DateTime is floating

            // If timezone specified, use it to pin the floating DateTime
            // to an instant
            if(tz!=null) {
                dt = pinFloatingTime(date, tz);
                dt.setUtc(true);
                return dt;
            }

            // Otherwise use default timezone for utc instant
            dt = (DateTime) Dates.getInstance(date, date);
            dt.setUtc(true);
            return dt;
        }


        // handle Date instances

        // If timezone specified, use it to pin the floating Date
        // to an instant
        if(tz!=null) {
            DateTime dt = pinFloatingTime(date, tz);
            dt.setUtc(true);
            return dt;
        }

        // Otherwise use default timezone for utc instant
        DateTime dt = new DateTime(date);
        dt.setUtc(true);
        return dt;
    }

    /**
     * Compare Date instances using a timezone to pin floating Date and
     * DateTimes.
     * @param date1
     * @param date2
     * @param tz timezone to use when interpreting floating Date and DateTime
     */
    public static int compareDates(Date date1, Date date2, TimeZone tz) {

        if(tz!=null) {
            if(isFloating(date1))
                date1 = pinFloatingTime(date1, tz);
            if(isFloating(date2))
                date2 = pinFloatingTime(date2, tz);
        }

        return date1.compareTo(date2);
    }

    /**
     * Determine if a Date is floating.  A floating Date is a Date
     * instance or a DateTime that is not utc and does not have a timezone.
     * @param date
     * @return true if the date is floating, otherwise false
     */
    public static boolean isFloating(Date date) {
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            return !dt.isUtc() && dt.getTimeZone()==null;
        } else
            return true;
    }

    public static boolean beforeDate(Date test, Date date, TimeZone tz) {
        return (compareDates(test, date, tz) < 0);
    }

    public static boolean afterDate(Date test, Date date, TimeZone tz) {
        return (compareDates(test, date, tz) > 0);
    }

    public static boolean equalsDate(Date test, Date date, TimeZone tz) {
        return (compareDates(test, date, tz)==0);
    }

    /**
     * Return list of subcomponents for a component.  Ica4j doesn't have
     * a generic way to do this.
     * @param component
     * @return list of subcomponents
     */
    public static ComponentList getSubComponents(Component component) {
        if(component instanceof VEvent)
            return ((VEvent) component).getAlarms();
        else if(component instanceof VTimeZone)
            return ((VTimeZone) component).getObservances();
        else if(component instanceof VToDo)
            return ((VToDo) component).getAlarms();

        return new ComponentList();
    }

    /**
     * Return the list of dates that an alarm will trigger.
     * @param alarm alarm component
     * @param parent parent compoennt (VEvent,VToDo)
     * @return dates that alarm is configured to trigger
     */
    public static List<Date> getTriggerDates(VAlarm alarm, Component parent) {
        ArrayList<Date> dates = new ArrayList<>();
        Trigger trigger = alarm.getTrigger();
        if(trigger==null)
            return dates;

        Date initialTriggerDate = getTriggerDate(trigger, parent);
        if(initialTriggerDate==null)
            return dates;

        dates.add(initialTriggerDate);

        Duration dur = alarm.getDuration();
        if(dur==null)
            return dates;
        Repeat repeat = alarm.getRepeat();
        if(repeat==null)
            return dates;

        Date nextTriggerDate = initialTriggerDate;
        for(int i=0;i<repeat.getCount();i++) {
            nextTriggerDate = Dates.getDateFromDuration(nextTriggerDate, dur.getDuration());
            dates.add(nextTriggerDate);
        }

        return dates;
    }

    /**
     * Return the date that a trigger refers to, which can be an absolute
     * date or a date relative to the start or end time of a parent
     * component (VEVENT/VTODO).
     * @param trigger
     * @param parent
     * @return date of trigger
     */
    public static Date getTriggerDate(Trigger trigger, Component parent) {

        if(trigger==null)
            return null;

        // if its absolute then we are done
        if(trigger.getDateTime()!=null)
            return trigger.getDateTime();

        // otherwise we need a start date if VEVENT
        DtStart start = parent.getProperty(Property.DTSTART);
        if(start==null && parent instanceof VEvent)
            return null;

        // is trigger relative to start or end
        Related related = trigger.getParameter(Parameter.RELATED);
        if(related==null || related.equals(Related.START)) {
            // must have start date
            if(start==null)
                return null;

            // relative to start
            return Dates.getDateFromDuration(start.getDate(), trigger.getDuration());
        } else {
            // relative to end
            Date endDate = null;

            // need an end date or duration or due
            DtEnd end = parent.getProperty(Property.DTEND);
            if(end!=null)
                endDate = end.getDate();

            if(endDate==null) {
                Duration dur = parent.getProperty(Property.DURATION);
                if (dur!=null && start!=null) {
                    endDate = Dates.getDateFromDuration(start.getDate(), dur.getDuration());
                }
            }

            if(endDate==null) {
                Due due = parent.getProperty(Property.DUE);
                if(due!=null)
                    endDate = due.getDate();
            }

            // require end date
            if(endDate==null)
                return null;

            return Dates.getDateFromDuration(endDate, trigger.getDuration());
        }
    }

    /**
     * Find and return the first DISPLAY VALARM in a comoponent
     * @param component VEVENT or VTODO
     * @return first DISPLAY VALARM, null if there is none
     */
    public static VAlarm getDisplayAlarm(Component component) {
        ComponentList alarms = null;

        if(component instanceof VEvent)
            alarms = ((VEvent) component).getAlarms();
        else if(component instanceof VToDo)
            alarms = ((VToDo) component).getAlarms();

        if(alarms==null || alarms.isEmpty())
            return null;

        for(Iterator<VAlarm> it = alarms.iterator();it.hasNext();) {
            VAlarm alarm = it.next();
            if(Action.DISPLAY.equals(alarm.getAction()))
                return alarm;
        }

        return null;
    }
}
