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

import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;
import org.osaf.cosmo.model.TaskStamp;


/**
 * Represents a Task Stamp.
 */
public class MockTaskStamp extends MockStamp implements
        java.io.Serializable, TaskStamp {

    /**
     * 
     */
    private static final long serialVersionUID = -6197756070431706553L;

    public static final QName ATTR_ICALENDAR = new MockQName(
            TaskStamp.class, "icalendar");
    
    /** default constructor */
    public MockTaskStamp() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceTaskStamp#getType()
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
        TaskStamp stamp = new MockTaskStamp();
        return stamp;
    }
}
