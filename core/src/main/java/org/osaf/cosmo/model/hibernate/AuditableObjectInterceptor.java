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
package org.osaf.cosmo.model.hibernate;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Date;

/**
 * Hibernate Interceptor that updates creationDate, modifiedDate,
 * and etag each time an AuditableObject is saved/updated.
 */
public class AuditableObjectInterceptor extends EmptyInterceptor {


    @Override
    public boolean onFlushDirty(Object object, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if(! (object instanceof HibAuditableObject))
            return false;

        // Set new modifyDate so that calculateEntityTag()
        // has access to it
        HibAuditableObject ao = (HibAuditableObject) object;
        Date curDate = new Date(System.currentTimeMillis());
        ao.setModifiedDate(curDate);

        // update modifiedDate and entityTag
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "modifiedDate".equals( propertyNames[i] ) ) {
                currentState[i] = curDate;
            }
        }
        return true;
    }

    @Override
    public boolean onSave(Object object, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

        if(! (object instanceof HibAuditableObject))
            return false;

        // Set new modifyDate so that calculateEntityTag()
        // has access to it
        HibAuditableObject ao = (HibAuditableObject) object;
        Date curDate = new Date(System.currentTimeMillis());
        ao.setModifiedDate(curDate);

        // initialize modifiedDate, creationDate and entityTag
        for ( int i=0; i < propertyNames.length; i++ ) {
            if ( "creationDate".equals(propertyNames[i]) ||
                  "modifiedDate".equals(propertyNames[i]) ) {
                state[i] = curDate;
            }
        }
        return true;
    }

}
