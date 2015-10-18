/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class AbstractHibernateDaoTestCase extends AbstractSpringDaoTestCase {

    protected HibernateTestHelper helper = null;
    
    protected Session session = null;
    protected SessionFactory sessionFactory = null;
    
    public AbstractHibernateDaoTestCase() {
        super();
        helper = new HibernateTestHelper();
    }
    
    
    @AfterTransaction
    protected void onTearDownAfterTransaction() throws Exception {
        // Get a reference to the Session and bind it to the TransactionManager
        SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
        Session s = holder.getSession();
        SessionFactoryUtils.closeSession(s);
    }


    protected void clearSession() {
        //session.flush();
        session.clear();
    }

    @BeforeTransaction
    protected void onSetUpBeforeTransaction() throws Exception {
        // Unbind session from TransactionManager
        session = sessionFactory.openSession();
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
    }
}
