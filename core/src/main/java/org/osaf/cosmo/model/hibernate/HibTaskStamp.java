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

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TaskStamp;


/**
 * Hibernate persistent TaskStamp.
 */
@Entity
@DiscriminatorValue("task")
public class HibTaskStamp extends HibStamp implements
        java.io.Serializable, TaskStamp {

    /**
     *
     */
    private static final long serialVersionUID = -6197756070431706553L;

    public static final QName ATTR_ICALENDAR = new HibQName(
            TaskStamp.class, "icalendar");

    /** default constructor */
    public HibTaskStamp() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.Stamp#getType()
     */
    public String getType() {
        return "task";
    }

    /**
     * Return TaskStamp from Item
     * @param item
     * @return TaskStamp from Item
     */
    public static TaskStamp getStamp(Item item) {
        return (TaskStamp) item.getStamp(TaskStamp.class);
    }

    public Stamp copy() {
        TaskStamp stamp = new HibTaskStamp();
        return stamp;
    }

}
