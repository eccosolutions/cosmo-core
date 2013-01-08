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

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;


/**
 * Management Bean for Scheduler
 */
@ManagedResource(objectName="cosmo:name=scheduler", description="Cosmo Scheduler Management")
public class SchedulerManager {

    private Scheduler scheduler;

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    @ManagedAttribute
    public int getNumberOffUsersWithActiveSchedules() {
        return scheduler.getUsersWithSchedules().size();
    }
    
    @ManagedAttribute
    public int getNumberOfActiveJobs() {
        return scheduler.getNumberOfActiveJobs();
    }
    
    @ManagedOperation
    public void stop() {
        scheduler.stop();
    }
    
    @ManagedOperation
    public void start() {
        scheduler.start();
    }
    
    @ManagedOperation
    public void refreshSchedules() {
        scheduler.scheduleSingleRefresh();
    }
}
