package org.osaf.cosmo.scheduler;

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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job that runs a report and passes the results
 * to a Notifier, which is responsible for notifying
 * the user of the results.
 */
public abstract class NotificationJob extends ServiceJob {
	private Map<String, Notifier> notifiers;
	private Map<String, String> notificationProperties;
	
	private static final Log log = LogFactory.getLog(NotificationJob.class);
	
	
	@Override
	protected final void executeJob(JobExecutionContext context)
			throws JobExecutionException {
		Report report = generateReport(context);
		String notifierKey = notificationProperties.get("name");
		
		log.debug("finding notifier for key: " + notifierKey);
		
		Notifier notifier = notifiers.get(notifierKey);
		
		if(notifier!=null)
			notifier.sendNotificationReport(report, notificationProperties);
	}
	
	protected abstract Report generateReport(JobExecutionContext context) throws JobExecutionException;

	public Map<String, Notifier> getNotifiers() {
		return notifiers;
	}

	public void setNotifiers(Map<String, Notifier> notifiers) {
		this.notifiers = notifiers;
	}

	public void setNotificationProperties(Map<String, String> notificationProperties) {
		this.notificationProperties = notificationProperties;
	}
}
