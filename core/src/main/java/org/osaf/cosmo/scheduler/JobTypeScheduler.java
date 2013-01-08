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

import org.osaf.cosmo.model.User;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Interface for classes that schedule a particular job type. A JobTypeScheduler
 * is responsible for scheduling a Quartz job for a user.
 */
public interface JobTypeScheduler {

    /**
     * Schedule a Quartz job.
     * 
     * @param scheduler
     *            Quartz scheduler
     * @param user
     *            associated user
     * @param schedule
     *            schedule
     * @throws SchedulerException
     */
    public void scheduleJob(Scheduler scheduler, User user, Schedule schedule)
            throws SchedulerException;
}
