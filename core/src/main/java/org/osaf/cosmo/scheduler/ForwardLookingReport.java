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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;

/**
 * Notification report that returns matching items in a time-range, with the
 * range being a day, week, or month, plus all items that are currently triaged
 * as NOW.
 */
public class ForwardLookingReport extends Report {

    private List<UpcomingResult> upcomingItems;
    private List<NowResult> nowItems;
    private Date startDate;
    private TimeZone timezone;
    private Locale locale;
    private String reportType;

    public ForwardLookingReport(User user) {
        super(user);
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public List<UpcomingResult> getUpcomingItems() {
        return upcomingItems;
    }

    public void setUpcomingItems(List<UpcomingResult> upcomingItems) {
        this.upcomingItems = upcomingItems;
    }

    public List<NowResult> getNowItems() {
        return nowItems;
    }

    public void setNowItems(List<NowResult> nowItems) {
        this.nowItems = nowItems;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public boolean isDaily() {
        return ForwardLookingNotificationJob.REPORT_TYPE_DAILY
                .equals(reportType);
    }

    public boolean isWeekly() {
        return ForwardLookingNotificationJob.REPORT_TYPE_WEEKLY
                .equals(reportType);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public static class NowResult {
        private CollectionItem collection;
        private NoteItem note;
        
        public NowResult(CollectionItem collection, NoteItem note) {
            this.collection = collection;
            this.note = note;
        }
        
        public CollectionItem getCollection() {
            return collection;
        }

        public void setCollection(CollectionItem collection) {
            this.collection = collection;
        }

        public NoteItem getNote() {
            return note;
        }

        public void setNote(NoteItem note) {
            this.note = note;
        }
    }
    
    public static class UpcomingResult {
        private CollectionItem collection;
        private NoteItem note;
        private boolean alarmResult = false;

        public UpcomingResult(CollectionItem collection, NoteItem note, boolean alarmResult) {
            this.collection = collection;
            this.note = note;
            this.alarmResult = alarmResult;
        }

        public CollectionItem getCollection() {
            return collection;
        }

        public void setCollection(CollectionItem collection) {
            this.collection = collection;
        }

        public NoteItem getNote() {
            return note;
        }

        public void setNote(NoteItem note) {
            this.note = note;
        }

        public boolean isAlarmResult() {
            return alarmResult;
        }

        public void setAlarmResult(boolean alarmResult) {
            this.alarmResult = alarmResult;
        }

    }
}
