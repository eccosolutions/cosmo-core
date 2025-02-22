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

import java.io.IOException;
import java.io.InputStream;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.osaf.cosmo.model.DataSizeException;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.Item;

/**
 * Hibernate persistent FileItem.
 */
@Entity
@DiscriminatorValue("file")
public class HibFileItem extends HibContentItem implements FileItem {


    /**
     *
     */
    private static final long serialVersionUID = -3829504638044059875L;

    @Column(name = "contentType", length=64)
    private String contentType = null;

    @Column(name = "contentLanguage", length=32)
    private String contentLanguage = null;

    @Column(name = "contentEncoding", length=32)
    private String contentEncoding = null;

    @Column(name = "contentLength")
    private Long contentLength = null;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="contentdataid")
    @Cascade( {CascadeType.ALL })
    private HibContentData contentData = null;

    public HibFileItem() {
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#getContent()
     */
    public byte[] getContent() {
        try {
            InputStream contentStream = contentData.getContentInputStream();
            byte[] result = IOUtils.toByteArray(contentStream);
            contentStream.close();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Error getting content");
        }
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#setContent(byte[])
     */
    public void setContent(byte[] content) {
        if(contentData==null) {
            contentData = new HibContentData();
        }

        // Verify size is not greater than MAX.
        // TODO: do this checking in ContentData.setContentBytes()
        if (content.length > MAX_CONTENT_SIZE)
            throw new DataSizeException("Item content too large");

        contentData.setContentBytes(content);

        setContentLength((long) content.length);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#clearContent()
     */
    public void clearContent() {
        contentData = null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#setContent(java.io.InputStream)
     */
    public void setContent(InputStream is, final long length) throws IOException {
        if(contentData==null) {
            contentData = new HibContentData();
        }

        // Verify size is not greater than MAX.
        // TODO: do this checking in ContentData.setContentInputStream()
        if (length > MAX_CONTENT_SIZE)
            throw new DataSizeException("Item content too large");

        contentData.setContentInputStream(is, length);

        setContentLength(length);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#getContentInputStream()
     */
    public InputStream getContentInputStream() {
        if(contentData==null)
            return null;
        else
            return contentData.getContentInputStream();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#getContentEncoding()
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#setContentEncoding(java.lang.String)
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#getContentLanguage()
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#setContentLanguage(java.lang.String)
     */
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#getContentLength()
     */
    public Long getContentLength() {
        return contentLength;
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#setContentLength(java.lang.Long)
     */
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#getContentType()
     */
    public String getContentType() {
        return contentType;
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.FileItem#setContentType(java.lang.String)
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Item copy() {
        FileItem copy = new HibFileItem();
        copyToItem(copy);
        return copy;
    }

    @Override
    protected void copyToItem(Item item) {
        if(!(item instanceof FileItem))
            return;

        super.copyToItem(item);

        FileItem contentItem = (FileItem) item;

        try {
            contentItem.setContent(getContent());
            contentItem.setContentEncoding(getContentEncoding());
            contentItem.setContentLanguage(getContentLanguage());
            contentItem.setContentType(getContentType());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error copying content");
        }
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append(
                "contentLength", getContentLength()).append("contentType",
                getContentType()).append("contentEncoding",
                getContentEncoding()).append("contentLanguage",
                getContentLanguage()).toString();
    }
}
