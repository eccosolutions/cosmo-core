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
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.Dates;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author cyrusdaboo
 *
 * A list of instances . Instances are created by adding a component, either the
 * master recurrence component or an overridden instance of one. Its is the
 * responsibility of the caller to ensure all the components added are for the
 * same event (i.e. UIDs are all the same). Also, the master instance MUST be
 * added first.
 */

@SuppressWarnings({"JavaDoc"})
public class InstanceList extends TreeMap<String, Instance> {

    private static final long serialVersionUID = 1838360990532590681L;
    private boolean isUTC = false;
    private TimeZone timezone = null;

    public InstanceList() {
        super();
    }

    /**
     * Add a component (either master or override instance) if it falls within
     * the specified time range.
     *
     * @param comp
     * @param rangeStart
     * @param rangeEnd
     */
    public void addComponent(Component comp, Date rangeStart, Date rangeEnd) {

        // See if it contains a recurrence ID
        if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null) {
            addMaster(comp, rangeStart, rangeEnd);
        } else {
            addOverride(comp, rangeStart, rangeEnd);
        }
    }

    /**
     * @return if the InstanceList generates instances in UTC format.
     */
    public boolean isUTC() {
        return isUTC;
    }

    /**
     * Instruct the InstanceList to generate instances in UTC time periods.
     * If set to false, InstanceList will generate floating time instances
     * for events with floating date/times.
     * @param isUTC
     */
    public void setUTC(boolean isUTC) {
        this.isUTC = isUTC;
    }

    /**
     * @return timezone used to convert floating times to UTC.  Only
     * used if isUTC is set to true.
     */
    public TimeZone getTimezone() {
        return timezone;
    }

    /**
     * Set the timezone to use when converting floating times to
     * UTC.  Only used if isUTC is set to true.
     * @param timezone
     */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    /**
     * Add a master component if it falls within the specified time range.
     *
     * @param comp
     * @param rangeStart
     * @param rangeEnd
     */
    protected void addMaster(Component comp, Date rangeStart, Date rangeEnd) {

        Date start = getStartDate(comp);

        if (start == null) {
            return;
        }

        Value startValue = start instanceof DateTime ? Value.DATE_TIME : Value.DATE;

        start = convertToUTCIfNecessary(start);

        if(start instanceof DateTime) {
            // adjust floating time if timezone is present
            start = adjustFloatingDateIfNecessary(start);
        }

        Dur duration;
        Date end = getEndDate(comp);
        if (end == null) {
            if (startValue.equals(Value.DATE_TIME)) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            end = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
        } else {
            end = convertToUTCIfNecessary(end);
            if(startValue.equals(Value.DATE_TIME)) {
                // Adjust floating end time if timezone present
                end = adjustFloatingDateIfNecessary(end);
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

        // Always add first instance if included in range..
        if (dateBefore(start, rangeEnd) &&
                (dateAfter(end, rangeStart)||
                 dateEquals(end,  rangeStart))) {
            Instance instance = new Instance(comp, start, end);
            put(instance.getRid().toString(), instance);
        }


        // recurrence dates..
        PropertyList<RDate> rDates = comp.getProperties()
                .getProperties(Property.RDATE);
        for (RDate rdate : rDates) {
            // Both PERIOD and DATE/DATE-TIME values allowed
            if (Value.PERIOD.equals(rdate.getParameters().getParameter(
                    Parameter.VALUE))) {
                for (Period period : rdate.getPeriods()) {
                    Date periodStart = adjustFloatingDateIfNecessary(period.getStart());
                    Date periodEnd = adjustFloatingDateIfNecessary(period.getEnd());
                    // Add period if it overlaps rage
                    if (periodStart.before(rangeEnd)
                            && periodEnd.after(rangeStart)) {
                        Instance instance = new Instance(comp, periodStart, periodEnd);
                        put(instance.getRid().toString(), instance);
                    }
                }
            } else {
                for (Date startDate : rdate.getDates()) {
                    startDate = convertToUTCIfNecessary(startDate);
                    startDate = adjustFloatingDateIfNecessary(startDate);
                    Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration
                            .getTime(startDate), startDate);
                    // Add RDATE if it overlaps range
                    if (inRange(startDate, endDate, rangeStart, rangeEnd)) {
                        Instance instance = new Instance(comp, startDate, endDate);
                        put(instance.getRid().toString(), instance);
                    }
                }
            }
        }

        // recurrence rules..
        PropertyList<RRule> rRules = comp.getProperties()
                .getProperties(Property.RRULE);

        // Adjust startRange to account for instances that occur before
        // the startRange, and end after it
        Date adjustedRangeStart = null;
        Date ajustedRangeEnd = null;

        if(!rRules.isEmpty()) {
            adjustedRangeStart = adjustStartRangeIfNecessary(rangeStart, start, duration);
            ajustedRangeEnd = adjustEndRangeIfNecessary(rangeEnd, start);
        }

        for (RRule rrule : rRules) {
            DateList startDates = rrule.getRecur().getDates(start, adjustedRangeStart,
                    ajustedRangeEnd,
                    (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);
            for (Date sd : startDates) {
                Date startDate = org.osaf.cosmo.calendar.util.Dates.getInstance(sd, start);
                Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(sd), start);
                Instance instance = new Instance(comp, startDate, endDate);
                // Workaround for https://github.com/ical4j/ical4j/issues/603
                if (instance.getStart().before(ajustedRangeEnd)) {
                    put(instance.getRid().toString(), instance);
                }
            }
        }
        // exception dates..
        PropertyList<ExDate> exDates = comp.getProperties().getProperties(
                Property.EXDATE);
        for (ExDate exDate : exDates) {
            for (Date sd : exDate.getDates()) {
                sd = convertToUTCIfNecessary(sd);
                sd = adjustFloatingDateIfNecessary(sd);
                Instance instance = new Instance(comp, sd, sd);
                remove(instance.getRid().toString());
            }
        }
        // exception rules..
        PropertyList<ExRule> exRules = comp.getProperties().getProperties(
                Property.EXRULE);
        if(!exRules.isEmpty() && adjustedRangeStart==null) {
            adjustedRangeStart = adjustStartRangeIfNecessary(rangeStart, start, duration);
            ajustedRangeEnd = adjustEndRangeIfNecessary(rangeEnd, start);
        }

        for (ExRule exrule : exRules) {
            DateList startDates = exrule.getRecur().getDates(start, adjustedRangeStart,
                    ajustedRangeEnd,
                    (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);
            for (Date sd : startDates) {
                Instance instance = new Instance(comp, sd, sd);
                remove(instance.getRid().toString());
            }
        }
    }

    /**
     * Add an override component if it falls within the specified time range.
     *
     * @param comp
     * @param rangeStart
     * @param rangeEnd
     * @return true if the override component modifies instance list and false
     *         if the override component has no effect on instance list
     */
    public boolean addOverride(Component comp, Date rangeStart, Date rangeEnd) {

        boolean modified = false;

        // Verify if component is an override
        if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null)
            return false;

        // First check to see that the appropriate properties are present.

        // We need a DTSTART.
        Date dtstart = getStartDate(comp);
        if (dtstart == null)
            return false;

        Value startValue = dtstart instanceof DateTime ? Value.DATE_TIME : Value.DATE;

        dtstart = convertToUTCIfNecessary(dtstart);

        if(dtstart instanceof DateTime) {
            // adjust floating time if timezone is present
            dtstart = adjustFloatingDateIfNecessary(dtstart);
        }

        // We need either DTEND or DURATION.
        Date dtend = getEndDate(comp);
        if (dtend == null) {
            Dur duration;
            if (startValue.equals(Value.DATE_TIME)) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            dtend = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(dtstart), dtstart);
        } else {
            // Convert to UTC if needed
            dtend = convertToUTCIfNecessary(dtend);
            if(startValue.equals(Value.DATE_TIME)) {
                // Adjust floating end time if timezone present
                dtend = adjustFloatingDateIfNecessary(dtend);
                // Handle case where dtend is before dtstart, in which the duration
                // will be 0, since it is a timed event
                if(dtend.before(dtstart)) {
                    dtend = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(0, 0, 0, 0).getTime(dtstart), dtstart);
                }
            } else {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 1 day since its an all-day event
                if(dtend.before(dtstart)) {
                    dtend = org.osaf.cosmo.calendar.util.Dates.getInstance(
                            new Dur(1, 0, 0, 0).getTime(dtstart), dtstart);
                }
            }
        }

        // Now create the map entry
        Date riddt = getRecurrenceId(comp);
        riddt = convertToUTCIfNecessary(riddt);
        if(riddt instanceof DateTime)
            riddt = adjustFloatingDateIfNecessary(riddt);

        boolean future = getRange(comp);

        Instance instance = new Instance(comp, dtstart, dtend, riddt, true,
                future);
        String key = instance.getRid().toString();

        // Replace the master instance if it exists
        if(containsKey(key)) {
            remove(key);
            modified = true;
        }

        // Add modification instance if its in the range
        if (dtstart.before(rangeEnd)
                && dtend.after(rangeStart)) {
            put(key, instance);
            modified = true;
        }

        // Handle THISANDFUTURE if present
        Range range = comp.getProperties().<RecurrenceId>getProperty(Property.RECURRENCE_ID)
                .getParameters().getParameter(Parameter.RANGE);

        // TODO Ignoring THISANDPRIOR
        if (Range.THISANDFUTURE.equals(range)) {

            // Policy - iterate over all the instances after this one, replacing
            // the original instance withg a version adjusted to match the
            // override component

            // We need to account for a time shift in the overridden component
            // by applying the same shift to the future instances
            boolean timeShift = (dtstart.compareTo(riddt) != 0);
            Dur offsetTime = (timeShift ? new Dur(riddt, dtstart) : null);
            Dur newDuration = (timeShift ? new Dur(dtstart, dtend) : null);

            // Get a sorted list rids so we can identify the starting location
            // for the override.  The starting position will be the rid after
            // the current rid, or in the case of no matching rid, the first
            // rid that is greater than the current rid.
            boolean containsKey = containsKey(key);
            TreeSet<String> sortedKeys = new TreeSet<>(keySet());
            for (Iterator<String> iter = sortedKeys.iterator(); iter.hasNext();) {
                String ikey = iter.next();
                if (ikey.equals(key) || (!containsKey && ikey.compareTo(key)>0)) {

                    if(containsKey && !iter.hasNext())
                        continue;
                    else if(containsKey)
                        ikey = iter.next();

                    boolean moreKeys = true;
                    boolean firstMatch = true;
                    while(moreKeys) {

                        // The target key is already set for the first
                        // iteration, so for all other iterations
                        // get the next target key.
                        if(firstMatch)
                            firstMatch = false;
                        else
                            ikey = iter.next();

                        Instance oldinstance = get(ikey);

                        // Do not override an already overridden instance
                        if (oldinstance.isOverridden())
                            continue;

                        // Determine start/end for new instance which may need
                        // to be offset by the start/end offset and adjusted for
                        // a new duration from the overridden component
                        Date originalstart = oldinstance.getRid();
                        Value originalvalue =
                            originalstart instanceof DateTime ?
                            Value.DATE_TIME : Value.DATE;


                        Date start = oldinstance.getStart();
                        Date end = oldinstance.getEnd();

                        if (timeShift) {
                            // Handling of overlapping overridden THISANDFUTURE
                            // components is not defined in 2445. The policy
                            // here is that a THISANDFUTURE override should
                            // override any previous THISANDFUTURE overrides. So
                            // we need to use the original start time for the
                            // instance being adjusted as the time that is
                            // shifted, and the original start time is geiven by
                            // its recurrence-id.
                            start = Dates.
                                getInstance(offsetTime.getTime(originalstart),
                                            originalvalue);
                            end = Dates.
                                getInstance(newDuration.getTime(start),
                                            originalvalue);
                        }

                        // Replace with new instance
                        Instance newinstance = new Instance(comp, start, end,
                                originalstart, false, false);
                        remove(ikey);
                        put(newinstance.getRid().toString(), newinstance);
                        modified = true;

                        if(!iter.hasNext())
                            moreKeys = false;
                    }
                }
            }
        }

        return modified;
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
            Duration duration = comp.getProperties().getProperty(Property.DURATION);
            if (duration != null) {
                dtEnd = new DtEnd(org.osaf.cosmo.calendar.util.Dates.getDateFromDuration(dtStart, duration.getDuration()));
            }
        }
        return (dtEnd != null) ? dtEnd.getDate() : null;
    }

    private Date getRecurrenceId(Component comp) {
        RecurrenceId rid = comp.getProperties().getProperty(
                Property.RECURRENCE_ID);
        return (rid != null) ? rid.getDate() : null;
    }

    private boolean getRange(Component comp) {
        RecurrenceId rid = comp.getProperties().getProperty(
                Property.RECURRENCE_ID);
        if (rid == null)
            return false;
        Parameter range = rid.getParameters().getParameter(Parameter.RANGE);
        return (range != null) && "THISANDFUTURE".equals(range.getValue());
    }

    /**
     * If the InstanceList is configured to convert all date/times to UTC,
     * then convert the given Date instance into a UTC DateTime.
     */
    private Date convertToUTCIfNecessary(Date date) {
        if(!isUTC)
            return date;

        return ICalendarUtils.convertToUTC(date, timezone);
    }

    /**
     * Adjust startRange to account for instances that begin before the given
     * startRange, but end after. For example if you have a daily recurring event
     * at 8am lasting for an hour and your startRange is 8:01am, then you
     * want to adjust the range back an hour to catch the instance that is
     * already occurring.
     */
    private Date adjustStartRangeIfNecessary(Date startRange, Date start, Dur dur) {

        // If start is a Date, then we need to convert startRange to
        // a Date using the timezone present
        if ((!(start instanceof DateTime)) && timezone != null) {
            return ICalendarUtils.normalizeUTCDateTimeToDate(
                    (DateTime) startRange, timezone);
        }

        // Otherwise start is a DateTime

        // If startRange is not the event start, no adjustment necessary
        if(!startRange.after(start))
            return startRange;

        // Need to adjust startRange back one duration to account for instances
        // that occur before the startRange, but end after the startRange
        Dur negatedDur = dur.negate();

        Calendar cal = Dates.getCalendarInstance(startRange);
        cal.setTime(negatedDur.getTime(startRange));

        // Return new startRange only if it is before the original startRange
        if(cal.getTime().before(startRange))
            return org.osaf.cosmo.calendar.util.Dates.getInstance(cal.getTime(), startRange);

        return startRange;
    }

    /**
     * Adjust endRange for Date instances.  First convert the UTC endRange
     * into a Date instance, then add a second
     */
    private Date adjustEndRangeIfNecessary(Date endRange, Date start) {

        // If instance is DateTime or timezone is not present, then
        // do nothing
        if (start instanceof DateTime || timezone == null)
            return endRange;


        endRange = ICalendarUtils.normalizeUTCDateTimeToDefaultOffset(
                (DateTime) endRange, timezone);


        return endRange;
    }

    /**
     * Adjust a floating time if a timezone is present.  A floating time
     * is initially created with the default system timezone.  If a timezone
     * if present, we need to adjust the floating time to be in specified
     * timezone.  This allows a server in the US to return floating times for
     * a query made by someone whos timezone is in Australia.  If no timezone is
     * set for the InstanceList, then the system default timezone will be
     * used in floating time calculations.
     * <p>
     * What happens is a floating time will get converted into a
     * date/time with a timezone.  This is ok for comparison and recurrence
     * generation purposes.  Note that Instances will get indexed as a UTC
     * date/time and for floating DateTimes, the the recurrenceId associated
     * with the Instance loses its "floating" property.
     */
    private Date adjustFloatingDateIfNecessary(Date date) {
        if(timezone==null || ! (date instanceof DateTime))
            return date;

        DateTime dtDate = (DateTime) date;
        if(dtDate.isUtc() || dtDate.getTimeZone()!=null)
            return date;

        try {
            return new DateTime(dtDate.toString(), timezone);
        } catch (ParseException e) {
            throw new RuntimeException("error parsing date");
        }

    }

    private boolean dateBefore(Date date1, Date date2) {
        return ICalendarUtils.beforeDate(date1, date2, timezone);
    }

    private boolean dateAfter(Date date1, Date date2) {
        return ICalendarUtils.afterDate(date1, date2, timezone);
    }

    private boolean dateEquals(Date date1, Date date2) {
        return ICalendarUtils.equalsDate(date1, date2, timezone);
    }

    private boolean inRange(Date dateStart, Date dateEnd, Date rangeStart, Date rangeEnd) {
        return  dateBefore(dateStart, rangeEnd)
                && dateAfter(dateEnd, rangeStart);
    }

}
