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

import javax.persistence.PersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.ServerPropertyDao;
import org.osaf.cosmo.model.ServerProperty;
import org.osaf.cosmo.model.hibernate.HibServerProperty;

/**
 * Implementation of ServerPropertyDao using Hibernate persistent objects.
 *
 */
public class ServerPropertyDaoImpl extends HibernateSessionSupport implements
        ServerPropertyDao {

    private static final Log log = LogFactory
            .getLog(ServerPropertyDaoImpl.class);

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ServerPropertyDao#getServerProperty(java.lang.String)
     */
    public String getServerProperty(String property) {
        try {
            var query = currentSession().createQuery(
                "from HibServerProperty where name=:name", ServerProperty.class).setParameter(
                "name", property);
            var prop = getUniqueResult(query);
            if (prop != null)
                return prop.getValue();
            else
                return null;
        } catch (PersistenceException e) {
            throw convertJpaAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dao.ServerPropertyDao#setServerProperty(java.lang.String, java.lang.String)
     */
    public void setServerProperty(String property, String value) {
        try {

            var query = currentSession().createQuery(
                "from HibServerProperty where name=:name", ServerProperty.class).setParameter(
                "name", property);
            var prop = getUniqueResult(query);
            if (prop != null) {
                prop.setValue(value);
                currentSession().update(prop);
            }
            else {
                prop = new HibServerProperty(property, value);
                currentSession().save(prop);
            }

            currentSession().flush();

        } catch (PersistenceException e) {
            throw convertJpaAccessException(e);
        }
    }
}
