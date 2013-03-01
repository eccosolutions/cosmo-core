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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.util.TimeZoneUtils;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.util.StringPropertyUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JobTypeScheduler implementation that schedules forward looking jobs, which
 * performs a time-range query on a set of collections and passes the results on
 * to a Notifier.
 *
 * <p>
 * A sample configuration for a schedule that executes at 6:00am every Sunday
 * America/Los_Angeles time and queries all items for the collections A, B, and
 * C in the next week. A, B and C are uids of collection items.
 * </p>
 *
 * <ul>
 * <li>timezone = America/Los_Angeles</li>
 * <li>cronexp = 0 0 6 ? * SUN</li>
 * <li>reportType = weekly</li>
 * <li>collection.A = true</li>
 * <li>collection.B = true</li>
 * <li>collection.C = true</li>
 * </ul>
 *
 * <p>
 * A sample configuration for a schedule that executes at 6:00am every day
 * Mon-Fri America/Los_Angeles time and queries all items for the collections A,
 * B, and C in the next day. A, B and C are uids of collection items.
 * </p>
 *
 * <ul>
 * <li>timezone = America/Los_Angeles</li>
 * <li>cronexp = 0 0 6 ? * MON-FRI</li>
 * <li>reportType = daily</li>
 * <li>collection.A = true</li>
 * <li>collection.B = true</li>
 * <li>collection.C = true</li>
 * </ul>
 *
 * <p>
 * This is assuming custom cron expressions are enabled.  If custom cron
 * expressions are not enabled, the cron expression will be generated
 * automatically based on the <code>reportType</code> parameter as follows:
 * <ul>
 * <li>weekly = 0 0 6 ? * MON (6am every Monday)</li>
 * <li>daily = 0 0 6 ? * * (6am every day)</li>
 * </ul>
 * </p>
 */
public class ForwardLookingJobTypeScheduler implements JobTypeScheduler {

    private static final Log log = LogFactory
            .getLog(ForwardLookingJobTypeScheduler.class);

    private CosmoSecurityManager securityManager = null;
    private ContentService contentService = null;
    private boolean allowCustomCronExpression = false;
    private boolean testMode = false;

    /** 6am every day **/
    private static final String DAILY_CRON_EXP = "0 0 6 ? * *";
    /** 6am every Monday **/
    private static final String WEEKLY_CRON_EXP = "0 0 6 ? * MON";
    /** every hour **/
    private static final String HOURLY_CRON_EXP = "0 0 * ? * *";

    @Override
    public void scheduleJob(Scheduler scheduler, User user, Schedule schedule)
            throws SchedulerException {

        TimeZone tz = null;

        String timezone = schedule.getProperties().get("timezone");
        if (timezone != null)
            tz = TimeZoneUtils.getTimeZone(timezone);

        String locale = schedule.getProperties().get("locale");

        String cronTab = schedule.getProperties().get("cronexp");
        String reportType = schedule.getProperties().get("reportType");

        if (reportType == null || "".equals(reportType))
            throw new SchedulerException("reportType must be present");

        // validate reportType
        if (!ForwardLookingNotificationJob.REPORT_TYPE_DAILY.equals(reportType)
                && !ForwardLookingNotificationJob.REPORT_TYPE_WEEKLY
                        .equals(reportType))
            throw new SchedulerException("invalid reportType " + reportType);

        // Tet Mode sets cron expression to HOURLY
        if(cronTab==null && testMode)
            cronTab = HOURLY_CRON_EXP;

        // validate cronexp
        if (!allowCustomCronExpression || cronTab==null) {
            if (ForwardLookingNotificationJob.REPORT_TYPE_DAILY
                    .equals(reportType))
                 cronTab = DAILY_CRON_EXP;

            if (ForwardLookingNotificationJob.REPORT_TYPE_WEEKLY
                    .equals(reportType))
                 cronTab = WEEKLY_CRON_EXP;
        }

        Trigger trigger = null;
        try {
            trigger = new CronTrigger(schedule.getName(), user.getUsername(),
                    schedule.getName(), user.getUsername(), cronTab,
                    tz == null ? TimeZone.getDefault() : tz);
        } catch (ParseException e) {
            throw new SchedulerException("invalid cron expression: " + cronTab,
                    e);
        }

        // collection uids
        String[] collectionUids = StringPropertyUtils.getChildKeys(
                "collection", schedule.getProperties().keySet().toArray(
                new String[0]));

        // The problem with collection uids is that events in the system can
        // change the validity of a uid. For instance, if a collection is
        // deleted, or if a ticket is revoked, a collection uid may no longer be
        // valid.
        // Ideally the scheduler is hooked into an event system that listens for
        // such events and adjusts in real time. For now verify each collection
        // uid and only pass valid collections.
        List<String> validCollectionUids = new ArrayList<String>();

        // find valid collections
        // execute as user to find out if user has access
        securityManager.initiateSecurityContext(user);
        try {
            for (String uid : collectionUids) {
                try {
                    Item item = contentService.findItemByUid(uid);
                    if (item instanceof CollectionItem)
                        validCollectionUids.add(uid);

                } catch (ItemSecurityException ise) {
                    // log
                }
            }
        } finally {
            // clear current security context
            SecurityContextHolder.clearContext();
        }

        // if there are no valid collections, skip scheduling
        if (validCollectionUids.isEmpty()) {
            if (log.isDebugEnabled())
                log.debug("no valid collections found for job "
                        + user.getUsername() + ":" + schedule.getName());
            return;
        }

        // create jobdetail, which is essentially the job + parameters
        JobDetail jt = new JobDetail(schedule.getName(), user.getUsername(),
                ForwardLookingNotificationJob.class);
        jt.getJobDataMap().put("username", user.getUsername());
        jt.getJobDataMap().put("reportType", reportType);
        jt.getJobDataMap().put("timezone", timezone);
        jt.getJobDataMap().put("locale", locale);
        jt.getJobDataMap().put("collectionUids", validCollectionUids);
        jt.getJobDataMap().put(
                "notificationProperties",
                StringPropertyUtils.getSubProperties("notifier", schedule
                        .getProperties()));

        // schedule job
        scheduler.scheduleJob(jt, trigger);
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setAllowCustomCronExpression(boolean allowCustomCronExpression) {
        this.allowCustomCronExpression = allowCustomCronExpression;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }
}
