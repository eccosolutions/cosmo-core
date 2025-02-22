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
package org.osaf.cosmo.dao;


import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for DAO that provides access to server
 * properties.
 *
 */
public interface ServerPropertyDao extends Dao {

    /**
     * Get a server property value
     * @param property
     */
    @Transactional(readOnly = true)
    String getServerProperty(String property);


    /**
     * Set a sever property value
     * @param property
     * @param value
     */
    void setServerProperty(String property, String value);
}
