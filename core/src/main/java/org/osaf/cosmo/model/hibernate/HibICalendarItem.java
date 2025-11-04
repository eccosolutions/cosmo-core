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
package org.osaf.cosmo.model.hibernate;

import net.fortuna.ical4j.model.Calendar;
import org.hibernate.annotations.NamedQuery;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Hibernate persistent ICalendarItem.
 */
@Entity
@DiscriminatorValue("icalendar")
@NamedQuery(
    name = "icalendarItem.by.parent.icaluid",
    query = "select item.id from HibICalendarItem item join item.parentDetails pd where pd.primaryKey.collection.id=:parentid and item.icalUid=:icaluid"
)
public abstract class HibICalendarItem extends HibContentItem implements ICalendarItem {

    public static final QName ATTR_ICALENDAR = new HibQName(
            ICalendarItem.class, "icalendar");

    @Nonnull
    @Column(name="icaluid") // Non-null when discriminator is icalendar but column has to be left as nullable
    //@Index(name="idx_icaluid")
    private String icalUid;

    public HibICalendarItem() {
    }

    @Nonnull
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ICalendarItem#getIcalUid()
     */
    public String getIcalUid() {
        return icalUid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ICalendarItem#setIcalUid(java.lang.String)
     */
    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }

    /**
     * Return the Calendar object containing a calendar component.
     * Used by sublcasses to store specific components.
     * @return calendar
     */
    protected Calendar getCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return HibICalendarAttribute.getValue(this, ATTR_ICALENDAR);
    }

    /**
     * Set the Calendar object containing a calendar component.
     * Used by sublcasses to store specific components.
     */
    protected void setCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        HibICalendarAttribute.setValue(this, ATTR_ICALENDAR, calendar);
    }

    @Override
    protected void copyToItem(Item item) {

        if(!(item instanceof ICalendarItem))
            return;

        super.copyToItem(item);

        // copy icalUid
        ICalendarItem icalItem = (ICalendarItem) item;
        icalItem.setIcalUid(getIcalUid());
    }

}
