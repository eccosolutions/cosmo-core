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

/**
 * The scheduler uses notifiers to send notification reports to a destination.
 * Destination examples include an email address or IM account.
 */
public interface Notifier {

    /**
     * Format and send notification report.
     * 
     * @param report
     *            report to format and send
     * @param properties
     *            notifier properties
     */
    public void sendNotificationReport(Report report,
            Map<String, String> properties);
}
