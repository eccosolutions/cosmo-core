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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public abstract class AbstractHibernateDaoTestCase extends AbstractSpringDaoTestCase {

    protected HibernateTestHelper helper;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired protected DataSource jdbcDataSource;

    public AbstractHibernateDaoTestCase() {
        super();
        helper = new HibernateTestHelper();
    }

    protected void clearSession() {
        entityManager.flush();
        entityManager.clear();
    }

    protected void cleanupDb () throws Exception {
        Connection conn = jdbcDataSource.getConnection();

        Statement stmt = conn.createStatement();
        stmt.executeUpdate("delete from cosmo_event_stamp");
        stmt.executeUpdate("delete from cosmo_stamp");
        stmt.executeUpdate("delete from cosmo_attribute");
        stmt.executeUpdate("delete from cosmo_collection_item");
        stmt.executeUpdate("delete from cosmo_tombstones");
        stmt.executeUpdate("delete from cosmo_item");
        stmt.executeUpdate("delete from cosmo_content_data");
        stmt.executeUpdate("delete from cosmo_users");

        conn.commit();
    }

    protected EntityManager getSession() {
        return entityManager;
    }
}
