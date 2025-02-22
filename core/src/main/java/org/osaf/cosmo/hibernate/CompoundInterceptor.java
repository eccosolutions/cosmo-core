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
package org.osaf.cosmo.hibernate;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate Interceptor supports invoking multiple Interceptors
 */
public class CompoundInterceptor implements Interceptor {

    private final static Logger log = LoggerFactory.getLogger(CompoundInterceptor.class);

    private final static List<Interceptor> interceptors = new LinkedList<Interceptor>();

    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] currentState,
        Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
        boolean modified = false;
        for(Interceptor i: interceptors) {
            modified |= i.onFlushDirty(entity, id, currentState, previousState,
                propertyNames, types);
        }
        return modified;
    }

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames,
        Type[] types) throws CallbackException {
        boolean modified = false;
        for(Interceptor i: interceptors) {
            modified |= i.onSave(entity, id, state, propertyNames, types);
        }
        return modified;
    }

    @Override
    public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames,
        Type[] types) throws CallbackException {
            for(Interceptor i: interceptors) {
                i.onDelete(entity, id, state, propertyNames, types);
            }
    }

    public static void registerInterceptor(Interceptor interceptor) {
        log.info("Registering Hibernate interceptor {}", interceptor.getClass().getName());
        interceptors.add(interceptor);
    }

}
