/*
 * Copyright 2007 Open Source Applications Foundation
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helps manage transactions for testing.  This is handled by Spring
 * in production.
 */
public class HibernateTransactionHelper {

    final PlatformTransactionManager txManager;
    final EntityManagerFactory entityManagerFactory;

    public HibernateTransactionHelper(PlatformTransactionManager txManager, EntityManagerFactory entityManagerFactory) {
        this.txManager = txManager;
        this.entityManagerFactory = entityManagerFactory;
    }

    public TransactionStatus startNewTransaction() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TransactionSynchronizationManager.bindResource(entityManagerFactory, new EntityManagerHolder(entityManager));
        TransactionStatus transactionStatus = txManager.getTransaction(new DefaultTransactionDefinition());
        return transactionStatus;
    }

    public void endTransaction(TransactionStatus ts, boolean rollback) {
        if(rollback)
            txManager.rollback(ts);
        else
            txManager.commit(ts);

        EntityManagerHolder holder = (EntityManagerHolder) TransactionSynchronizationManager.unbindResource(
            entityManagerFactory);
        EntityManager em = holder.getEntityManager();
        EntityManagerFactoryUtils.closeEntityManager(em);
    }

}
