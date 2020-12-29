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

import org.hibernate.annotations.Type;
import org.osaf.cosmo.model.AuditableObject;
import org.osaf.cosmo.model.EntityFactory;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * Hibernate persistent AuditableObject.
 */
@MappedSuperclass
public abstract class HibAuditableObject extends BaseModelObject implements AuditableObject {

    private static final EntityFactory FACTORY = new HibEntityFactory();

    @Column(name = "createdate")
    @Type(type="long_timestamp")
    private Date creationDate;

    @Column(name = "modifydate")
    @Type(type="long_timestamp")
    private Date modifiedDate;


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.AuditableObject#getCreationDate()
     */
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.AuditableObject#getModifiedDate()
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.AuditableObject#updateTimestamp()
     */
    public void updateTimestamp() {
        modifiedDate = new Date();
    }

    public EntityFactory getFactory() {
        return FACTORY;
    }
}
