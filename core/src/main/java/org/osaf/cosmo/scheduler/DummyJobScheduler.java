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

import net.fortuna.ical4j.model.TimeZone;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.StringPropertyUtils;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * JobTypeScheulder that schedules DummyNotificationJobs. Used for testing
 * purposes.
 * <p>
 * A sample scheduler configuration is:
 * </p>
 * <ul>
 * <li>cronexp = 0 0 12 * * ?</li>
 * <li>message = Hello World!</li>
 * </ul>
 * 
 */
public class DummyJobScheduler implements JobTypeScheduler {

    public void scheduleJob(Scheduler scheduler, User user, Schedule schedule)
            throws SchedulerException {
        String cronTab = schedule.getProperties().get("cronexp");

        Trigger trigger = null;
        try {
            trigger = new CronTrigger(schedule.getName(), user.getUsername(),
                    schedule.getName(), user.getUsername(), cronTab, TimeZone
                            .getDefault());
        } catch (ParseException e) {
            throw new SchedulerException("invalid cron expression: " + cronTab,
                    e);
        }

        JobDetail jt = new JobDetail(schedule.getName(), user.getUsername(),
                DummyNotificationJob.class);
        jt.getJobDataMap().put("message",
                schedule.getProperties().get("message"));
        jt.getJobDataMap().put("username", user.getUsername());
        jt.getJobDataMap().put(
                "notificationProperties",
                StringPropertyUtils.getChildProperties("notifier", schedule
                        .getProperties()));

        scheduler.scheduleJob(jt, trigger);
    }

}
