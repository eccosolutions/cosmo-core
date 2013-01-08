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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Interface for a filter to be executed by a Job. This allows code to be run
 * before/after a job is executed. For example, a SecurityContext can be
 * initialized.
 */
public interface Filter {

    /**
     * Method filter should implement. After before code is run, the filter
     * chain should be invoked and then after code should be run.
     * 
     * @param context
     * @param chain
     * @throws JobExecutionException
     */
    public void doFilter(JobExecutionContext context, FilterChain chain)
            throws JobExecutionException;
}
