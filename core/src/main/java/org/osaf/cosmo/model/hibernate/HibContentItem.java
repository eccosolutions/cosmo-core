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

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Hibernate persistent ContentItem.
 */
@Entity
@DiscriminatorValue("content")
@NamedQueries({
    @NamedQuery(name = "contentItem.by.uid", query = "from HibContentItem i where i.uid=:uid"),
    @NamedQuery(name = "contentItem.by.parent.timestamp", query = "select item from HibContentItem item left join fetch item.stamps left join fetch item.attributes left join fetch item.tombstones join item.parentDetails pd where pd.primaryKey.collection=:parent and item.modifiedDate>:timestamp"),
    @NamedQuery(name = "contentItem.by.parent", query = "select item from HibContentItem item left join fetch item.stamps left join fetch item.attributes left join fetch item.tombstones join item.parentDetails pd where pd.primaryKey.collection=:parent"),
    @NamedQuery(name = "contentItem.by.owner", query = "from HibContentItem i where i.owner=:owner")
})
public abstract class HibContentItem extends HibItem implements ContentItem {

    /**
     *
     */
    private static final long serialVersionUID = 4904755977871771389L;

    @Column(name = "lastmodifiedby", length=255)
    private String lastModifiedBy = null;

    @Column(name = "lastmodification")
    private Integer lastModification = null;

    @Column(name = "sent")
    private Boolean sent = null;

    @Column(name = "needsreply")
    private Boolean needsReply = null;

    public HibContentItem() {
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#getLastModifiedBy()
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#setLastModifiedBy(java.lang.String)
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#getLastModification()
     */
    public Integer getLastModification() {
        return lastModification;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#setLastModification(java.lang.Integer)
     */
    public void setLastModification(Integer lastModification) {
        this.lastModification = lastModification;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#getSent()
     */
    public Boolean getSent() {
        return sent;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#setSent(java.lang.Boolean)
     */
    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#getNeedsReply()
     */
    public Boolean getNeedsReply() {
        return needsReply;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.ContentItem#setNeedsReply(java.lang.Boolean)
     */
    public void setNeedsReply(Boolean needsReply) {
        this.needsReply = needsReply;
    }

    @Override
    protected void copyToItem(Item item) {
        if(!(item instanceof ContentItem))
            return;

        super.copyToItem(item);

        ContentItem contentItem = (ContentItem) item;

        contentItem.setLastModifiedBy(getLastModifiedBy());
        contentItem.setLastModification(getLastModification());
        contentItem.setSent(getSent());
        contentItem.setNeedsReply(getNeedsReply());
    }
}
