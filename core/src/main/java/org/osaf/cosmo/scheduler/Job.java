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
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Base class for stateful quartz job that includes hooks for executing common
 * code before and after execution (@link Filter).
 * 
 * Because its stateful, any changes to the underlying JobDetailMap will be
 * persisted between executions and only one execution per JobDetail can be
 * running at a time.
 */
public abstract class Job extends QuartzJobBean implements StatefulJob {

    private List<Filter> filters;
    private Scheduler scheduler;

    @Override
    protected final void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        // kick off new filter chain
        FilterChain filterChain = new FilterChainImpl(this,
                new ArrayList<Filter>(filters), scheduler);
        filterChain.doFilter(context);
    }

    /**
     * Job execution code goes here.
     * 
     * @param context
     *            job context
     * @throws JobExecutionException
     */
    protected abstract void executeJob(JobExecutionContext context)
            throws JobExecutionException;

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

}
