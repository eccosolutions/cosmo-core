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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.fortuna.ical4j.model.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.util.TimeZoneUtils;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.TriageStatus;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.filter.Restrictions;
import org.osaf.cosmo.model.util.NoteUtils;
import org.osaf.cosmo.scheduler.ForwardLookingReport.NowResult;
import org.osaf.cosmo.scheduler.ForwardLookingReport.UpcomingResult;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Notification job that queries a set of collections, returns a
 * ForwardLookingReport with the results. The time-range is computed to be the
 * fire time of the job plus either 1 day or 1 week depending on the
 * report type (daily or weekly).
 */
public class ForwardLookingNotificationJob extends MultipleCollectionJob {

    public static final String REPORT_TYPE_DAILY = "daily";
    public static final String REPORT_TYPE_WEEKLY = "weekly";

    private static final Log log = LogFactory
            .getLog(ForwardLookingNotificationJob.class);

    private String timezone;
    private String locale;
    private String reportType;

    @Override
    protected Report generateReport(JobExecutionContext context)
            throws JobExecutionException {

        TimeZone tz = timezone == null ? null : TimeZoneUtils
                .getTimeZone(timezone);
        Locale loc = locale==null ? Locale.getDefault() : new Locale(locale);

        // start date is the start of the day of the fire time

        Calendar cal = new GregorianCalendar();
        if (tz != null)
            cal.setTimeZone(tz);

        cal.setTime(context.getFireTime());
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Date startDate = new Date(cal.getTime().getTime());

        // End date is either 1 day or 1 week later
        // depending on report type
        if (REPORT_TYPE_DAILY.equals(reportType))
            cal.add(Calendar.DAY_OF_YEAR, 1);
        else
            cal.add(Calendar.WEEK_OF_YEAR, 1);

        cal.add(Calendar.SECOND, -1);

        Date endDate = cal.getTime();
        
        if(log.isDebugEnabled()) {
            log.debug("startDate=" + startDate);
            log.debug("endDate = " + endDate);
        }

        // get results
        List<UpcomingResult> upcomingItems = new ArrayList<UpcomingResult>();
        List<NowResult> nowItems = new ArrayList<NowResult>();

        Iterator<String> it = getCollectionUids().iterator();
        while (it.hasNext()) {
            String colUid = it.next();

            // We have to handle changes to underlying collections.
            // A collection could have been deleted, or access to the
            // collection has been removed. In both cases, remove the
            // collection from the list so that the next job run
            // won't attempt to access it.
            try {
                getCollectionResults(colUid, startDate, endDate, tz,
                        upcomingItems, nowItems);
            } catch (ItemSecurityException ise) {
                // user is not authorized for collection,
                // remove collection for next execution
                log.info("user " + getUsername()
                        + " not authorized for collection " + colUid
                        + ", removing from schedule "
                        + context.getJobDetail().getName());
                it.remove();
            } catch (ItemNotFoundException infe) {
                // collection not found, remove collection for next execution
                log.info("collection " + colUid
                        + " not found removing from schedule " + getUsername()
                        + ":" + context.getJobDetail().getName());
                it.remove();
            }
        }

        // sort results
        Comparator<UpcomingResult> comparator = new UpcomingResultComparator(false, tz);
        Collections.sort(upcomingItems, comparator);

        // return results
        ForwardLookingReport report = new ForwardLookingReport(getUser());
        report.setStartDate(startDate);
        report.setTimezone(tz == null ? TimeZone.getDefault() : tz);
        report.setLocale(loc);
        report.setReportType(reportType);
        report.setUpcomingItems(upcomingItems);
        report.setNowItems(nowItems);
        return report;
    }

    private void getCollectionResults(String collectionUid, Date startDate,
            Date endDate, TimeZone tz, List<UpcomingResult> upcomingItems,
            List<NowResult> nowItems) {
        Item item = getContentService().findItemByUid(collectionUid);

        // ensure item exists and is a collection
        if (item == null || !(item instanceof CollectionItem))
            throw new ItemNotFoundException("collection " + collectionUid
                    + " not found");

        CollectionItem collection = (CollectionItem) item;

        findUpcomingNotes(upcomingItems, collection, startDate, endDate,tz);
        findNowNotes(nowItems, collection);
    }

    private void findUpcomingNotes(List<UpcomingResult> upcomingItems, CollectionItem collection,
            Date startDate, Date endDate, TimeZone tz) {
        
        NoteItemFilter eventNoteFilter = new NoteItemFilter();
        eventNoteFilter.setFilterProperty(
                EventStampFilter.PROPERTY_INCLUDE_MASTER_ITEMS, "false");
        EventStampFilter eventFilter = new EventStampFilter();
        eventFilter.setExpandRecurringEvents(true);
        eventFilter.setTimeRange(startDate, endDate);
        eventFilter.setTimezone(tz);
        eventNoteFilter.setParent(collection);
        eventNoteFilter.getStampFilters().add(eventFilter);

        NoteItemFilter reminderTimeFilter = new NoteItemFilter();
        reminderTimeFilter.setParent(collection);
        reminderTimeFilter.setReminderTime(Restrictions.between(startDate,
                endDate));

        for (Item item : getContentService().findItems(eventNoteFilter))
            upcomingItems.add(new UpcomingResult(collection, (NoteItem) item, false));

        for (Item item : getContentService().findItems(reminderTimeFilter))
            upcomingItems.add(new UpcomingResult(collection, (NoteItem) item, true));
    }

    private void findNowNotes(List<NowResult> nowItems, CollectionItem collection) {
        
        NoteItemFilter noteFilter = new NoteItemFilter();
        noteFilter.setParent(collection);
        noteFilter.setTriageStatusCode(Restrictions.eq(TriageStatus.CODE_NOW));

        for (Item item : getContentService().findItems(noteFilter))
            nowItems.add(new NowResult(collection, (NoteItem) item));
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    /**
     * Compare NoteItems using a rank calculated from the NoteItem startDate (if
     * its an event) or the custom alarm date.
     */
    public static class UpcomingResultComparator implements Comparator<UpcomingResult> {

        boolean reverse = false;
        TimeZone timezone;

        public UpcomingResultComparator(TimeZone timezone) {
            this.timezone = timezone;
        }

        public UpcomingResultComparator(boolean reverse, TimeZone timezone) {
            this.reverse = reverse;
            this.timezone = timezone;
        }

        public int compare(UpcomingResult result1, UpcomingResult result2) {
            NoteItem note1 = result1.getNote();
            NoteItem note2 = result2.getNote();

            if (note1.getUid().equals(note2.getUid()))
                return 0;

            // Calculate a rank based on the date or alarm
            long rank1 = getRank(note1, result1.isAlarmResult());
            long rank2 = getRank(note2, result2.isAlarmResult());

            if (rank1 > rank2)
                return reverse ? -1 : 1;
            else
                return reverse ? 1 : -1;
        }

        /**
         * Calculate rank of NoteItem.
         */
        private long getRank(NoteItem note, boolean isAlarmResult) {
            
            // If result is alarm result, return alarm time
            if(isAlarmResult) {
                Date alarmDate = NoteUtils.getCustomAlarm(note);
                if(alarmDate!=null)
                    return alarmDate.getTime();
                else
                    return Long.MIN_VALUE;
            }
            
            
            // otherwise find the startDate and return it
            net.fortuna.ical4j.model.Date startDate = NoteUtils
            .getStartDate(note);

            
            // handle case of floating times
            if (timezone != null)
                startDate = NoteUtils.getNormalizedDate(startDate, timezone);
            
            if (startDate != null)
                return startDate.getTime();

            // default to MIN_VALUE for items without dates
            return Long.MIN_VALUE;
        }
    }

}
