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
package org.osaf.cosmo.model.mock;

import org.osaf.cosmo.model.AuditableObject;
import org.osaf.cosmo.model.EntityFactory;

import java.util.Date;

/**
 * Extends BaseModelObject and adds creationDate, modifiedDate
 * to track when Object was created and modified.
 */
public abstract class MockAuditableObject implements AuditableObject {

    private static final EntityFactory FACTORY = new MockEntityFactory();

    private Date creationDate;
    private Date modifiedDate;
    private final String etag = "";

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#getCreationDate()
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#setCreationDate(java.util.Date)
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#getModifiedDate()
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#setModifiedDate(java.util.Date)
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAuditableObject#updateTimestamp()
     */
    public void updateTimestamp() {
        modifiedDate = new Date();
    }

    public EntityFactory getFactory() {
        return FACTORY;
    }
}
