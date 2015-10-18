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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Lob;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * Represents the data of a piece of Content. Data is stored
 * as a BufferedContent, either in memory (small content) or
 * on disk (large content).
 */
@Entity
@Table(name="cosmo_content_data")
@Configurable
public class HibContentData extends BaseModelObject {

    /**
     * 
     */
    private static final long serialVersionUID = -5014854905531456753L;
    
    @Column(name = "content", length=102400000)
    @Lob
    private Blob content = null;

    @Transient
    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    { readResolve(); }

    public Object readResolve() {
        AnnotationBeanConfigurerAspect.aspectOf().configureBean(this);
        return this;
    }
   
    /**
     */
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }


    /**
     * Get an InputStream to the content data.  Repeated
     * calls to this method will return new instances
     * of InputStream.
     */
    public InputStream getContentInputStream() {
        if(content==null)
            return null;

        try {
            return content.getBinaryStream();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Set the content using an InputSteam.  Does not close the 
     * InputStream.
     * @param is content data
     * @throws IOException
     */
    public void setContentInputStream(InputStream is, long length) throws IOException {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        content = sessionFactory.getCurrentSession().getLobHelper().createBlob(is, length);
    }
    
    /**
     * @return the size of the data read, or -1 for no data present
     */
    public long getSize() {
        try {
            if(content != null) return content.length();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    } 
}
