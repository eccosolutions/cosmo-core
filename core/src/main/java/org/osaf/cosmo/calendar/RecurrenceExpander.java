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

import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.Dates;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that contains apis that that involve
 * expanding recurring components.
 * <p>
 * Expanding recurrences shouldn't be used to solve every quirk...
 * <p>
 * truncate solution over changing a recurring day (better than RANGE):
 *      <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.4">rfc5545#section-3.8.4.4</a>
 *          Assuming an unbounded recurring calendar
 *          component scheduled to occur on Mondays and Wednesdays, the
 *          "RANGE" parameter could not be used to reschedule only the
 *          future Monday instances to occur on Tuesday instead.  In such
 *          cases, the calendar application could simply truncate the
 *          unbounded recurring calendar component (i.e., with the "COUNT"
 *          or "UNTIL" rule parts), and create two new unbounded recurring
 *          calendar components for the future instances.
 * <p>
 * truncate solution over THISANDFUTURE (better than RANGE):
 *      <a href="https://stackoverflow.com/questions/11456406/recurrence-id-in-icalendar-rfc-5545">...</a>
 *          the difficulty of rescheduling using THISANDFUTURE and interoperability has been documented in calconnect interop oct 2010.
 *          If you can, it would propably be easier / safer for interop to follow the note in the RFC5545 § 3.8.4.4.
 *          The "RANGE" parameter may not be appropriate to reschedule specific subsequent instances [...] . In such cases, the calendar application could simply truncate the unbounded recurring calendar component (i.e., with the "COUNT" or "UNTIL" rule parts), and create two new unbounded recurring calendar components for the future instances.
 * <p>
 * truncate solution over THISANDFUTURE - bring in a SERIES ID:
 *      <a href="https://www.calconnect.org/pubdocs/CD1014%20October%202010%20CalConnect%20Interoperability%20Test%20Event%20Report.pdf">...</a>
 *      A series is split, with a new UID being sent for a second half of the series: Truncation/Expansion
 *      (essentially capturing THISANDFUTURE behavior)
 *      We would like to explore the concept of adding a SERIES ID to the iCalendar spec so that multiple
 *      UIDs could still be considered part of the same series. This would help in the implementation of
 *      THISANDFUTURE. Currently, the only ways to truly represent THISANDFUTURE changes are:
 *       -Modification of each effected instance as an exception
 *       -Dynamic processing of data
 *       -Splitting the meeting into multiple UIDs for each split. At this point, a subsequent
 *      THISANDFUTURE call across the two modified sets becomes problematic and there remains
 *      problems linking the two separate UIDs as they are actually representing the same meeting.
 * <p>
 *      series-id to link uid's together
 *          <a href="https://www.ietf.org/id/draft-ietf-calext-icalendar-series-02.html#name-series-id">...</a>
 */
public class RecurrenceExpander {

    // By default, expand to 1 year in the future. Anything more should require that we provide a date range of interest
    private static final Date MAX_EXPAND_DATE = new Date(LocalDateTime.now()
            .plusMonths(Integer.getInteger("cosmo.RecurrenceExpander.monthsFromToday", 12))
            .toInstant(ZoneOffset.UTC).toEpochMilli());


    public RecurrenceExpander() {
        super();
    }

    /**
     * Return start and end Date that represent the start of the first
     * occurrence of a recurring component and the end of the last
     * occurence.  If the recurring component has no end(infinite recurring event),
     * then no end date will be returned.
     * @param calendar Calendar containing master and modification components
     * @return array containing start (located at index 0) and end (index 1) of
     *         recurring component.
     */
    public Date[] calculateRecurrenceRange(Calendar calendar) {
        ComponentList<VEvent> vevents = calendar.getComponents().getComponents(
                Component.VEVENT);

        List<Component> exceptions = new ArrayList<>();
        Component masterComp = null;

        // get list of exceptions (VEVENT with RECURRENCEID)
        for (VEvent event : vevents) {
            if (event.getRecurrenceId() != null)
                exceptions.add(event);
            else
                masterComp = event;

        }

        return calculateRecurrenceRange(masterComp, exceptions);
    }

    /**
     * Return a start and end Date that represents the start of the first
     * occurence of a recurring component and the end of the last occurence.  If
     * the recurring component has no end(infinite recurring event),
     * then no end date will be returned.
     *
     * @param comp Component to analyze
     * @return array containing start (located at index 0) and end (index 1) of
     *         recurring component.
     */
    public Date[] calculateRecurrenceRange(Component comp) {
        return calculateRecurrenceRange(comp, new ArrayList<>(0));

    }
    /**
     * Return a start and end Date that represents the start of the first
     * occurence of a recurring component and the end of the last occurence.  If
     * the recurring component has no end(infinite recurring event),
     * then no end date will be returned.
     *
     * @param comp Component to analyze
     * @param modifications modifications to component
     * @return array containing start (located at index 0) and end (index 1) of
     *         recurring component.
     */
    public Date[] calculateRecurrenceRange(Component comp, List<Component> modifications) {

        Date[] dateRange = new Date[2];
        Date start = getStartDate(comp);

        // must have start date
        if (start == null) {
            return null;
        }

        Dur duration;
        Date end = getEndDate(comp);
        if (end == null) {
            if (start instanceof DateTime) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            end = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
        } else {
            if(end instanceof DateTime) {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 0, since it is a timed event
                if(end.before(start)) {
                    end = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(0, 0, 0, 0).getTime(start), start);
                }
            } else {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 1 day since its an all-day event
                if(end.before(start)) {
                    end = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(1, 0, 0, 0).getTime(start), start);
                }
            }
            duration = new Dur(start, end);
        }

        // Always add master's occurence
        dateRange[0] = start;
        dateRange[1] = end;

        // Now tweak range based on RDATE, RRULE, and component modifications
        // For now, ignore EXDATE and EXRULE because RDATE and RRULE will
        // give us the broader range.

        // recurrence dates..
        PropertyList<RDate> rDates = comp.getProperties()
                .getProperties(Property.RDATE);
        for (RDate rdate : rDates) {
            // Both PERIOD and DATE/DATE-TIME values allowed
            if (Value.PERIOD.equals(rdate.getParameters().getParameter(
                    Parameter.VALUE))) {
                for (Period period : rdate.getPeriods()) {
                    if (period.getStart().before(dateRange[0]))
                        dateRange[0] = period.getStart();
                    if (period.getEnd().after(dateRange[1]))
                        dateRange[1] = period.getEnd();

                }
            } else {
                for (Date startDate : rdate.getDates()) {
                    Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration
                            .getTime(startDate), startDate);
                    if (startDate.before(dateRange[0]))
                        dateRange[0] = startDate;
                    if (endDate.after(dateRange[1]))
                        dateRange[1] = endDate;
                }
            }
        }

        // recurrence rules..
        PropertyList<RRule> rRules = comp.getProperties()
                .getProperties(Property.RRULE);
        for (RRule rrule : rRules) {
            Recur recur = rrule.getRecur();

            // If this is an infinite recurring event, we are done processing
            // the rules
            if (recur.getCount() == -1 && recur.getUntil() == null) {
                dateRange[1] = null;
                break;
            }

            // DateList startDates = rrule.getRecur().getDates(start.getDate(),
            // adjustedRangeStart, rangeEnd, (Value)
            // start.getParameters().getParameter(Parameter.VALUE));
            DateList startDates = rrule.getRecur().getDates(start, start,
                    MAX_EXPAND_DATE,
                    (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);

            // Dates are sorted, so get the last occurence, and calculate the end
            // date and update dateRange if necessary
            if (!startDates.isEmpty()) {
                Date lastStart = startDates.get(startDates.size() - 1);
                Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(lastStart), start);

                if (endDate.after(dateRange[1]))
                    dateRange[1] = endDate;
            }
        }

        // event modifications....
        // NB Modifications only obey start/end, not recur rules
        for(Component modComp : modifications) {
            Date startMod = getStartDate(modComp);
            Date endMod = getEndDate(modComp);
            if (startMod.before(dateRange[0]))
                dateRange[0] = startMod;
            if (dateRange[1] != null && endMod != null &&
                endMod.after(dateRange[1]))
                dateRange[1] = endMod;

            // TODO: handle THISANDFUTURE/THISANDPRIOR edge cases
        }

        // make sure timezones are consistent with original timezone
        if(start instanceof DateTime) {
            ((DateTime) dateRange[0]).setTimeZone(((DateTime) start).getTimeZone());
            if(dateRange[1]!=null)
                ((DateTime) dateRange[0]).setTimeZone(((DateTime) start).getTimeZone());
        }

        return dateRange;
    }

    /**
     * Expand recurring event for given time-range.
     * @param calendar calendar containing recurring event and modifications
     * @param rangeStart expand start
     * @param rangeEnd expand end
     * @param timezone Optional timezone to use for floating dates.  If null, the
     *        system default is used.
     * @return InstanceList containing all occurrences of recurring event during
     *         time range
     */
    public InstanceList getOcurrences(Calendar calendar, Date rangeStart, Date rangeEnd, TimeZone timezone) {
        ComponentList<VEvent> vevents = calendar.getComponents().getComponents(
                Component.VEVENT);

        List<Component> exceptions = new ArrayList<>();
        Component masterComp = null;

        // get list of exceptions (VEVENT with RECURRENCEID)
        for (VEvent event : vevents) {
            if (event.getRecurrenceId() != null)
                exceptions.add(event);
            else
                masterComp = event;

        }

        return getOcurrences(masterComp, exceptions, rangeStart, rangeEnd, timezone);
    }

    /**
     * Expand recurring compnent for given time-range.
     * @param component recurring component to expand
     * @param rangeStart expand start date
     * @param rangeEnd expand end date
     * @param timezone Optional timezone to use for floating dates.  If null, the
     *        system default is used.
     * @return InstanceList containing all occurences of recurring event during
     *         time range
     */
    public InstanceList getOcurrences(Component component, Date rangeStart, Date rangeEnd, TimeZone timezone) {
        return getOcurrences(component, new ArrayList<>(0), rangeStart, rangeEnd, timezone);
    }

    /**
     * Expand recurring compnent for given time-range.
     * @param component recurring component to expand
     * @param modifications modifications to recurring component
     * @param rangeStart expand start date
     * @param rangeEnd expand end date
     * @param timezone Optional timezone to use for floating dates.  If null, the
     *        system default is used.
     * @return InstanceList containing all occurences of recurring event during
     *         time range
     */
    public InstanceList getOcurrences(Component component, List<Component> modifications, Date rangeStart, Date rangeEnd, TimeZone timezone) {
        InstanceList instances = new InstanceList();
        instances.setTimezone(timezone);
        instances.addMaster(component, rangeStart, rangeEnd);
        for(Component mod: modifications)
            instances.addOverride(mod, rangeStart, rangeEnd);

        return instances;
    }


    /**
     * Determine if date is a valid occurence in recurring calendar component
     * @param calendar recurring calendar component
     * @param occurrence occurrence date
     * @return true if the occurrence date is a valid occurrence, otherwise false
     */
    public boolean isOccurrence(Calendar calendar, Date occurrence) {
        java.util.Calendar cal = Dates.getCalendarInstance(occurrence);
        cal.setTime(occurrence);

        // Add a second or day (one unit forward) so we can set a range for
        // finding instances.  This is required because ical4j's Recur apis
        // only calculate recurring dates up until but not including the
        // end date of the range.
        if(occurrence instanceof DateTime)
            cal.add(java.util.Calendar.SECOND, 1);
        else
            cal.add(java.util.Calendar.DAY_OF_WEEK, 1);

        Date rangeEnd =
            org.osaf.cosmo.calendar.util.Dates.getInstance(cal.getTime(), occurrence);

        InstanceList instances = getOcurrences(calendar, occurrence, rangeEnd, null);

        for (Instance instance : instances.values()) {
            if (instance.getRid().getTime() == occurrence.getTime())
                return true;
        }

        return false;
    }

    private Date getStartDate(Component comp) {
        DtStart prop = comp.getProperties().getProperty(
                Property.DTSTART);
        return (prop != null) ? prop.getDate() : null;
    }

    private Date getEndDate(Component comp) {
        DtEnd dtEnd = comp.getProperties().getProperty(Property.DTEND);
        // No DTEND? No problem, we'll use the DURATION if present.
        if (dtEnd == null) {
            Date dtStart = getStartDate(comp);
            Duration duration = comp.getProperties().getProperty(
                    Property.DURATION);
            if (duration != null) {
                return org.osaf.cosmo.calendar.util.Dates.getDateFromDuration(dtStart, duration.getDuration());
            }
        }
        return (dtEnd != null) ? dtEnd.getDate() : null;
    }
}
