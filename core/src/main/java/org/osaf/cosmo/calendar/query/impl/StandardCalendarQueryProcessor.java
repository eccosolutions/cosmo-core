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
package org.osaf.cosmo.calendar.query.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.calendar.Instance;
import org.osaf.cosmo.calendar.InstanceList;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.CalendarFilterEvaluater;
import org.osaf.cosmo.calendar.query.CalendarQueryProcessor;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.User;

/**
 * CalendarQueryProcessor implementation that uses CalendarDao.
 */
public class StandardCalendarQueryProcessor implements CalendarQueryProcessor {

    private static final Log log =
        LogFactory.getLog(StandardCalendarQueryProcessor.class);


    private CalendarDao calendarDao = null;
    private ContentDao contentDao = null;
    private final EntityConverter entityConverter = new EntityConverter(null);

    /* (non-Javadoc)
     * @see org.osaf.cosmo.calendar.query.CalendarQueryProcessor#filterQuery(org.osaf.cosmo.model.CollectionItem, org.osaf.cosmo.calendar.query.CalendarFilter)
     */
    public Set<ICalendarItem> filterQuery(CollectionItem collection,
            CalendarFilter filter) {
        if (log.isDebugEnabled()) {
            log.debug("finding events in collection " + collection.getUid()
                    + " by filter " + filter);
        }

        return new HashSet<>(calendarDao
            .findCalendarItems(collection, filter));
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.calendar.query.CalendarQueryProcessor#filterQuery(org.osaf.cosmo.model.ICalendarItem, org.osaf.cosmo.calendar.query.CalendarFilter)
     */
    public boolean filterQuery(ICalendarItem item, CalendarFilter filter) {
        if (log.isDebugEnabled())
            log.debug("matching item " + item.getUid() + " to filter " + filter);

        Calendar calendar = entityConverter.convertContent(item);
        if(calendar!=null)
            return new CalendarFilterEvaluater().evaluate(calendar, filter);
        else
            return false;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.calendar.query.CalendarQueryProcessor#freeBusyQuery(org.osaf.cosmo.model.User, net.fortuna.ical4j.model.Period)
     */
    public VFreeBusy freeBusyQuery(User user, Period period) {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();

        HomeCollectionItem home = contentDao.getRootItem(user);
        for(Item item: home.getChildren()) {
            if(! (item instanceof CollectionItem))
                continue;

            CollectionItem collection = (CollectionItem) item;
            if(StampUtils.getCalendarCollectionStamp(collection)==null || collection.isExcludeFreeBusyRollup())
                continue;

            doFreeBusyQuery(busyPeriods, busyTentativePeriods, busyUnavailablePeriods,
                    collection, period);
        }

        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.calendar.query.CalendarQueryProcessor#freeBusyQuery(org.osaf.cosmo.model.CollectionItem, net.fortuna.ical4j.model.Period)
     */
    public VFreeBusy freeBusyQuery(CollectionItem collection, Period period) {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();

        doFreeBusyQuery(busyPeriods, busyTentativePeriods, busyUnavailablePeriods,
                collection, period);

        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.calendar.query.CalendarQueryProcessor#freeBusyQuery(org.osaf.cosmo.model.ICalendarItem, net.fortuna.ical4j.model.Period)
     */
    public VFreeBusy freeBusyQuery(ICalendarItem item, Period period) {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();

        Calendar calendar = entityConverter.convertContent(item);

        // Add busy details from the calendar data
        addBusyPeriods(calendar, null, period, busyPeriods,
                busyTentativePeriods, busyUnavailablePeriods);

        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }

    protected void doFreeBusyQuery(PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods,
            CollectionItem collection, Period period) {

        CalendarCollectionStamp ccs = StampUtils.getCalendarCollectionStamp(collection);
        if(ccs==null)
            return;

        HashSet<ContentItem> results = new HashSet<>();
        TimeZone tz = ccs.getTimezone();

        // For the time being, use CalendarFilters to get relevant
        // items.
        CalendarFilter[] filters = createQueryFilters(collection, period);
        for(CalendarFilter filter: filters)
            results.addAll(calendarDao.findCalendarItems(collection, filter));

        for(ContentItem content: results) {
            Calendar calendar = entityConverter.convertContent(content);
            if(calendar==null)
                continue;
            // Add busy details from the calendar data
            addBusyPeriods(calendar, tz, period, busyPeriods,
                    busyTentativePeriods, busyUnavailablePeriods);
        }
    }

    protected void addBusyPeriods(Calendar calendar, TimeZone timezone,
            Period freeBusyRange, PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods) {

        // Create list of instances within the specified time-range
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(timezone);

        // Look at each VEVENT/VFREEBUSY component only
        ComponentList overrides = new ComponentList();
        for (CalendarComponent comp : calendar.getComponents()) {
            if (comp instanceof VEvent) {
                VEvent vcomp = (VEvent) comp;
                // See if this is the master instance
                if (vcomp.getRecurrenceId() == null) {
                    instances.addComponent(vcomp, freeBusyRange.getStart(),
                            freeBusyRange.getEnd());
                } else {
                    overrides.add(vcomp);
                }
            } else if (comp instanceof VFreeBusy) {
                // Add all FREEBUSY BUSY/BUSY-TENTATIVE/BUSY-UNAVAILABLE to the
                // periods
                PropertyList<FreeBusy> fbs = comp.getProperties().getProperties(Property.FREEBUSY);
                for (FreeBusy fb :  fbs) {
                    FbType fbt = fb.getParameters().getParameter(
                            Parameter.FBTYPE);
                    if ((fbt == null) || FbType.BUSY.equals(fbt)) {
                        addRelevantPeriods(busyPeriods, fb.getPeriods(),
                                freeBusyRange);
                    } else if (FbType.BUSY_TENTATIVE.equals(fbt)) {
                        addRelevantPeriods(busyTentativePeriods, fb
                                .getPeriods(), freeBusyRange);
                    } else if (FbType.BUSY_UNAVAILABLE.equals(fbt)) {
                        addRelevantPeriods(busyUnavailablePeriods, fb
                                .getPeriods(), freeBusyRange);
                    }
                }
            }
        }

        for (Component comp : (Iterable<Component>) overrides) {
            instances.addComponent(comp, freeBusyRange.getStart(),
                    freeBusyRange.getEnd());
        }

        // See if there is nothing to do (should not really happen)
        if (instances.isEmpty()) {
            return;
        }

        // Add start/end period for each instance
        for (String ikey : (Iterable<String>) instances.keySet()) {
            Instance instance = (Instance) instances.get(ikey);

            // Check that the VEVENT has the proper busy status
            if (Transp.TRANSPARENT.equals(instance.getComp().getProperties()
                    .getProperty(Property.TRANSP))) {
                continue;
            }
            if (Status.VEVENT_CANCELLED.equals(instance.getComp()
                    .getProperties().getProperty(Property.STATUS))) {
                continue;
            }

            // Can only have DATE-TIME values in PERIODs
            DateTime start, end = null;

            start = (DateTime) instance.getStart();
            end = (DateTime) instance.getEnd();

            if (start.compareTo(freeBusyRange.getStart()) < 0) {
                start = (DateTime) org.osaf.cosmo.calendar.util.Dates.getInstance(freeBusyRange
                        .getStart(), start);
            }
            if (end.compareTo(freeBusyRange.getEnd()) > 0) {
                end = (DateTime) org.osaf.cosmo.calendar.util.Dates.getInstance(freeBusyRange.getEnd(),
                        end);
            }
            if (Status.VEVENT_TENTATIVE.equals(instance.getComp()
                    .getProperties().getProperty(Property.STATUS))) {
                busyTentativePeriods.add(new Period(start, end));
            } else {
                busyPeriods.add(new Period(start, end));
            }

        }
    }

    /**
     * Add all periods that intersect a given period to the result PeriodList.
     */
    private void addRelevantPeriods(PeriodList results, PeriodList periods,
            Period range) {

        for (Period p : periods) {
            if (p.intersects(range))
                results.add(p);
        }
    }

    private CalendarFilter[] createQueryFilters(CollectionItem collection, Period period) {
        DateTime start = period.getStart();
        DateTime end = period.getEnd();
        CalendarFilter[] filters = new CalendarFilter[2];
        TimeZone tz = null;

        // Create calendar-filter elements designed to match
        // VEVENTs/VFREEBUSYs within the specified time range.
        //
        // <C:filter>
        // <C:comp-filter name="VCALENDAR">
        // <C:comp-filter name="VEVENT">
        // <C:time-range start="20051124T000000Z"
        // end="20051125T000000Z"/>
        // </C:comp-filter>
        // <C:comp-filter name="VFREEBUSY">
        // <C:time-range start="20051124T000000Z"
        // end="20051125T000000Z"/>
        // </C:comp-filter>
        // </C:comp-filter>
        // </C:filter>

        // If the calendar collection has a timezone attribute,
        // then use that to convert floating date/times to UTC
        CalendarCollectionStamp ccs = StampUtils.getCalendarCollectionStamp(collection);
        if (ccs!=null) {
            tz = ccs.getTimezone();
        }

        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        if(tz!=null)
            eventFilter.getTimeRangeFilter().setTimezone(tz.getVTimeZone());

        ComponentFilter calFilter = new ComponentFilter(
                net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);

        filters[0] = filter;

        ComponentFilter freebusyFilter = new ComponentFilter(
                Component.VFREEBUSY);
        freebusyFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        if(tz!=null)
            freebusyFilter.getTimeRangeFilter().setTimezone(tz.getVTimeZone());

        calFilter = new ComponentFilter(
                net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(freebusyFilter);

        filter = new CalendarFilter();
        filter.setFilter(calFilter);

        filters[1] = filter;

        return filters;
    }

    protected VFreeBusy createVFreeBusy(PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods,
            Period period) {
        // Merge periods
        busyPeriods = busyPeriods.normalise();
        busyTentativePeriods = busyTentativePeriods.normalise();
        busyUnavailablePeriods = busyUnavailablePeriods.normalise();

        // Now create a VFREEBUSY
        VFreeBusy vfb = new VFreeBusy(period.getStart(), period.getEnd());
        String uid = UUID.randomUUID().toString();
        vfb.getProperties().add(new Uid(uid));

        // Add all periods to the VFREEBUSY
        if (!busyPeriods.isEmpty()) {
            FreeBusy fb = new FreeBusy(busyPeriods);
            fb.getParameters().add(FbType.BUSY);
            vfb.getProperties().add(fb);
        }
        if (!busyTentativePeriods.isEmpty()) {
            FreeBusy fb = new FreeBusy(busyTentativePeriods);
            fb.getParameters().add(FbType.BUSY_TENTATIVE);
            vfb.getProperties().add(fb);
        }
        if (!busyUnavailablePeriods.isEmpty()) {
            FreeBusy fb = new FreeBusy(busyUnavailablePeriods);
            fb.getParameters().add(FbType.BUSY_UNAVAILABLE);
            vfb.getProperties().add(fb);
        }

        return vfb;
    }

    public void setCalendarDao(CalendarDao calendarDao) {
        this.calendarDao = calendarDao;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

}
