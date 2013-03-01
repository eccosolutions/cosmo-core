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

/**
 * FilterChain implementation. Takes a list of Filters and calls each filter
 * until there are no more filters at which point the job is executed.
 */
public class FilterChainImpl implements FilterChain {

    private List<Filter> filters;
    private Job job;
    private Scheduler scheduler;

    public FilterChainImpl(Job job, List<Filter> filters, Scheduler scheduler) {
        this.job = job;
        this.filters = filters == null ? null : new ArrayList<Filter>(filters);
        this.scheduler = scheduler;
    }

    public void doFilter(JobExecutionContext context)
            throws JobExecutionException {
        if (filters != null && !filters.isEmpty()) {
            Filter filter = filters.remove(0);
            filter.doFilter(context, this);
        } else {
            try {
                job.executeJob(context);
            } catch (RuntimeException e) {
                scheduler.handleError(context.getJobDetail().getGroup(),
                        context.getJobDetail().getName(), e);
            }
        }
    }

}
