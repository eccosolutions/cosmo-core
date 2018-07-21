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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Filter that sets up Hibernate session for the duration of the job to allow
 * for lazy loading to work.
 */
public class HibernateSessionFilter implements Filter {

    private SessionFactory sessionFactory;

    private static final Log log = LogFactory
            .getLog(HibernateSessionFilter.class);

    public void doFilter(JobExecutionContext context, FilterChain chain)
            throws JobExecutionException {
        boolean opened = false;
        try {
            opened = bindSession();
            chain.doFilter(context);
        } finally {
            if (opened) releaseSession();
        }
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private void releaseSession() {
        log.debug("unbinding session to thread");

        // Unbind session from TransactionManager and close
        SessionHolder sessionHolder =
                (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.closeSession(sessionHolder.getSession());
    }

    private boolean bindSession() {
        log.debug("binding session to thread");

        // Get a reference to the Session and bind it to the TransactionManager
        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            return false;
        }
        Session session = sessionFactory.openSession();
        TransactionSynchronizationManager.bindResource(sessionFactory,
                new SessionHolder(session));
        return true;
    }

}
