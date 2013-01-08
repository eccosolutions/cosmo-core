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

import java.util.Set;

/**
 * Interface for cosmo scheduler.
 */
public interface Scheduler {

    public void init();
    
    public void destroy();

    public void refreshSchedules();
    
    public void scheduleSingleRefresh();

    public void removeAllSchedules();

    public void handleError(String username, String jobName, Throwable e);
    
    public Set<String> getUsersWithSchedules();
    
    public int getNumberOfActiveJobs();
    
    public void start();
    
    public void stop();
}