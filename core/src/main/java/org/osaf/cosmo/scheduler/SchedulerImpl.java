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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.ScheduleService;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * Cosmo Scheduler implementation based on Quartz job scheduling engine.
 */
public class SchedulerImpl implements Scheduler {

    private static final Log log = LogFactory.getLog(SchedulerImpl.class);

    private ScheduleService scheduleService;
    private org.quartz.Scheduler scheduler;
    private HashMap<String, JobTypeScheduler> jobSchedulers;
    private HashMap<String, Set<Schedule>> userSchedules = new HashMap<String, Set<Schedule>>();
    private boolean initialized = false;
    private boolean stopped = true;
    private boolean enabled = true;
    
    /**
     * Controls the maximum number of jobs per user.  Default is no limit (-1). 
     */
    private int maxJobsPerUser = -1;

    /**
     * default refresh interval of 1 hour
     */
    private long refreshInterval = 1000 * 60 * 60;

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.scheduler.Scheduler#init()
     */
    public synchronized void init() {
        if (initialized)
            return;

        if(!enabled)
            return;
        
        if (scheduler == null)
            throw new IllegalStateException("scheduler must not be null");
        if (scheduleService == null)
            throw new IllegalStateException(
                    "scheduleService must not be null");

        if (log.isDebugEnabled())
            log.debug("scheduler initializing");
        
        // schedule job that will refresh schedules
        JobDetail jt = new JobDetail("scheduler", "refresh",
                ScheduleRefreshJob.class);
        Trigger trigger = new SimpleTrigger("refresh", "scheduler",
                SimpleTrigger.REPEAT_INDEFINITELY, refreshInterval);

        try {
            scheduler.start();
            scheduler.scheduleJob(jt, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("error scheduling refresh job", e);
        }

        stopped = false;
        initialized = true;

        if (log.isDebugEnabled())
            log.debug("scheduler initialized");
    }
        
    
    public Set<Schedule> getSchedulesForUser(String username) {
        return userSchedules.get(username);
    }


    public synchronized void destroy() {
        try {
            log.info("shutting down scheduler for good");
            scheduler.shutdown();
        } catch (SchedulerException e) {
            log.error("Error shutting down scheduler", e);
        }
    }
    
    public synchronized void stop() {
        if(stopped)
            return;
        
        try {
            log.info("stopping scheduler");
            scheduler.standby();
            stopped = true;
        } catch (SchedulerException e) {
            log.error("Error shutting down scheduler", e);
        }
    }
    
    public synchronized void start() {
        
        if(!stopped)
            return;
        
        try {
            log.info("restarting scheduler");
            scheduler.start();
            stopped = false;
        } catch (SchedulerException e) {
            log.error("Error shutting down scheduler", e);
        }
    }
    
    public int getNumberOfActiveJobs() {
        int num = 0;
        try {
            for (String group : scheduler.getJobGroupNames())
                num += scheduler.getJobNames(group).length;

            return num;
        } catch (SchedulerException e) {
            log.error(e);
        }
        
        return num;
    }

    public Set<String> getUsersWithSchedules() {
        HashSet<String> users = new HashSet<String>();
        users.addAll(userSchedules.keySet());
        return users;
    }
    
    public void scheduleSingleRefresh() {
        // schedule job that will refresh schedules
        long currTime = System.currentTimeMillis();
        JobDetail jt = new JobDetail("scheduler", "refresh" + currTime,
                ScheduleRefreshJob.class);
        Trigger trigger = new SimpleTrigger("refresh" + currTime, "scheduler");

        try {
            scheduler.start();
            scheduler.scheduleJob(jt, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("error scheduling refresh job", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.scheduler.Scheduler#refreshSchedules()
     */
    public synchronized void refreshSchedules() {
        if (log.isDebugEnabled())
            log.debug("refreshing schedlules");

        // all users with schedules
        Set<User> users = scheduleService.getUsersWithSchedules();

        // keep track of usernames processed
        Set<String> processed = new HashSet<String>();

        for (User user : users) {
            processed.add(user.getUsername());
            Set<Schedule> schedules = scheduleService.getSchedulesForUser(user);
            Set<Schedule> oldSchedules = userSchedules.get(user.getUsername());
            // If no existing schedules exist, add
            if (oldSchedules == null)
                scheduleUserJobs(user, schedules);
            // otherwise compare schedules to existing schedules and reschedule
            // if necessary
            else if (!oldSchedules.equals(schedules)) {
                removeAllJobsForUser(user.getUsername());
                scheduleUserJobs(user, schedules);
            }
        }

        // prune
        for (String userName : userSchedules.keySet())
            if (!processed.contains(userName))
                removeAllJobsForUser(userName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.scheduler.Scheduler#removeAllSchedules()
     */
    public void removeAllSchedules() {
        for (String user : userSchedules.keySet())
            removeAllJobsForUser(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.scheduler.Scheduler#handleError(java.lang.String,
     *      java.lang.String, java.lang.Throwable)
     */
    public void handleError(String username, String jobName, Throwable e) {
        log.info("error exuting job " + username + ":" + jobName, e);
    }

    private void removeAllJobsForUser(String username) {

        // jobs are organized by group (username) and jobname
        String[] jobNames = null;

        if (log.isDebugEnabled())
            log.debug("unscheduling jobs for user: " + username);

        try {
            jobNames = scheduler.getJobNames(username);
        } catch (SchedulerException e) {
            // log and continue
            log.error("scheduler error", e);
            return;
        }

        for (String jobName : jobNames) {
            try {
                scheduler.deleteJob(jobName, username);
            } catch (SchedulerException e) {
                log.error("scheduler error: " + e.getMessage());
            }
        }

        userSchedules.remove(username);
    }

    protected void scheduleUserJobs(User user, Set<Schedule> schedules) {

        for (Schedule schedule : schedules) {
            try {
                scheduleUserJob(user, schedule);
            } catch (SchedulerException e) {
                log.error("scheduler error: " + e.getMessage());
                log.debug("disabling job " + user.getUsername() + ":"
                        + schedule.getName());
                scheduleService.enableScheduleForUser(schedule, user, false);
            }
        }

        userSchedules.put(user.getUsername(), schedules);
    }

    private void scheduleUserJob(User user, Schedule schedule)
            throws SchedulerException {
        
        log.debug("scheduling job: " + schedule.getName() + " for user : "
                + user.getUsername());
        
        // check if user has reached max number of schedules
        if (maxJobsPerUser > 0
                && scheduler.getJobNames(user.getUsername()).length >= maxJobsPerUser) {
            log.info("user " + user.getUsername()
                    + " has reached the maximum allowed of schedules, ignoring schedule");
            return;
        }

        String jobType = schedule.getProperties().get("type");

        JobTypeScheduler jobScheduler = jobSchedulers.get(jobType);
        if (jobScheduler != null)
            jobScheduler.scheduleJob(scheduler, user, schedule);
        else
            log.info("no job scheduler found for job type " + jobType);
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public void setJobSchedulers(HashMap<String, JobTypeScheduler> jobSchedulers) {
        this.jobSchedulers = jobSchedulers;
    }

    public void setScheduler(org.quartz.Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxJobsPerUser() {
        return maxJobsPerUser;
    }

    public void setMaxJobsPerUser(int maxJobsPerUser) {
        this.maxJobsPerUser = maxJobsPerUser;
    }

}
