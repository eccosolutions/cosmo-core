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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.scheduler.ForwardLookingReport.NowResult;
import org.osaf.cosmo.scheduler.ForwardLookingReport.UpcomingResult;

/**
 * Notifier implementation that logs results. Used for testing purposes.
 * 
 */
public class LogNotifier implements Notifier {

    private static final Log log = LogFactory.getLog(LogNotifier.class);

    public void sendNotificationReport(Report report,
            Map<String, String> properties) {

        log.debug("notify: runDate = " + report.getRunDate() + " user = "
                + report.getUser());

        if (report instanceof ForwardLookingReport)
            handleForwardLookingReport((ForwardLookingReport) report);
        else if (report instanceof DummyReport)
            handleDummyReport((DummyReport) report);
    }

    private void handleDummyReport(DummyReport report) {
        log.debug("DummyReport: message = " + report.getMessage());
    }

    private void handleForwardLookingReport(ForwardLookingReport report) {
        log.debug("ForwardLookingReport:");
        log.debug("reportType = " + report.getReportType());
        log.debug("results:");
        log.debug("upcoming items:");
        for (UpcomingResult result : report.getUpcomingItems())
            log.debug("Collection = " + result.getCollection().getDisplayName()
                    + " Note = " + result.getNote().getDisplayName()
                    + " isAlarm = " + result.isAlarmResult());
        log.debug("NOW items:");
        for (NowResult result : report.getNowItems())
            log.debug("Collection = " + result.getCollection().getDisplayName()
                    + " Note = " + result.getNote().getDisplayName());
    }

}
