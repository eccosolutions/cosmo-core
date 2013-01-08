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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Simple notification job that logs a message. Used for testing purposes.
 */
public class DummyNotificationJob extends NotificationJob {

    private static final Log log = LogFactory
            .getLog(DummyNotificationJob.class);
    private String message = null;

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected Report generateReport(JobExecutionContext context)
            throws JobExecutionException {
        log.debug("executing job: " + context.getJobDetail().getGroup() + ":"
                + context.getJobDetail().getName() + " message: " + message);
        DummyReport report = new DummyReport(getUser(), message);
        report.setRunDate(context.getFireTime());
        return report;
    }

}
