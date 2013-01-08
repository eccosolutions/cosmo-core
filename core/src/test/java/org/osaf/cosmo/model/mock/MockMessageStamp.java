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

import java.io.Reader;

import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.MessageStamp;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;


/**
 * Represents a Message Stamp.
 */
public class MockMessageStamp extends MockStamp implements
        java.io.Serializable, MessageStamp {

   
    /**
     * 
     */
    private static final long serialVersionUID = -6100568628972081120L;
    
    public static final QName ATTR_MESSAGE_ID = new MockQName(
            MessageStamp.class, "messageId");
    
    public static final QName ATTR_MESSAGE_HEADERS = new MockQName(
            MessageStamp.class, "headers");
    
    public static final QName ATTR_MESSAGE_FROM = new MockQName(
            MessageStamp.class, "from");
    
    public static final QName ATTR_MESSAGE_TO = new MockQName(
            MessageStamp.class, "to");
    
    public static final QName ATTR_MESSAGE_CC = new MockQName(
            MessageStamp.class, "cc");
    
    public static final QName ATTR_MESSAGE_BCC = new MockQName(
            MessageStamp.class, "bcc");
    
    public static final QName ATTR_MESSAGE_ORIGINATORS = new MockQName(
            MessageStamp.class, "originators");
    
    public static final QName ATTR_MESSAGE_DATE_SENT = new MockQName(
            MessageStamp.class, "dateSent");
    
    public static final QName ATTR_MESSAGE_IN_REPLY_TO = new MockQName(
            MessageStamp.class, "inReplyTo");
    
    public static final QName ATTR_MESSAGE_REFERENCES = new MockQName(
            MessageStamp.class, "references");
    
    /** default constructor */
    public MockMessageStamp() {
    }
    
    public MockMessageStamp(Item item) {
        setItem(item);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getType()
     */
    public String getType() {
        return "message";
    }
    
    // Property accessors
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getMessageId()
     */
    public String getMessageId() {
        // id stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_ID);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setMessageId(java.lang.String)
     */
    public void setMessageId(String id) {
        // id stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_ID, id);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getHeaders()
     */
    public String getHeaders() {
        // headers stored as TextAttribute on Item
        return MockTextAttribute.getValue(getItem(), ATTR_MESSAGE_HEADERS);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setHeaders(java.lang.String)
     */
    public void setHeaders(String headers) {
        // headers stored as TextAttribute on Item
        MockTextAttribute.setValue(getItem(), ATTR_MESSAGE_HEADERS, headers);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setHeaders(java.io.Reader)
     */
    public void setHeaders(Reader headers) {
        // headers stored as TextAttribute on Item
        MockTextAttribute.setValue(getItem(), ATTR_MESSAGE_HEADERS, headers);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getFrom()
     */
    public String getFrom() {
        // from stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_FROM);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setFrom(java.lang.String)
     */
    public void setFrom(String from) {
        // from stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_FROM, from);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getTo()
     */
    public String getTo() {
        // to stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_TO);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setTo(java.lang.String)
     */
    public void setTo(String to) {
        // to stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_TO, to);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getBcc()
     */
    public String getBcc() {
        // bcc stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_BCC);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setBcc(java.lang.String)
     */
    public void setBcc(String bcc) {
        //bcc stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_BCC, bcc);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getCc()
     */
    public String getCc() {
        // cc stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_CC);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setCc(java.lang.String)
     */
    public void setCc(String cc) {
        // cc stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_CC, cc);
        updateTimestamp();
    }
 
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getOriginators()
     */
    public String getOriginators() {
        // originators stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_ORIGINATORS);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setOriginators(java.lang.String)
     */
    public void setOriginators(String originators) {
        // originators stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_ORIGINATORS, originators);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getDateSent()
     */
    public String getDateSent() {
        // inReployTo stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_DATE_SENT);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setDateSent(java.lang.String)
     */
    public void setDateSent(String dateSent) {
        // inReployTo stored as TextAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_DATE_SENT, dateSent);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getInReplyTo()
     */
    public String getInReplyTo() {
        // inReployTo stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setInReplyTo(java.lang.String)
     */
    public void setInReplyTo(String inReplyTo) {
        // inReployTo stored as TextAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO, inReplyTo);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#getReferences()
     */
    public String getReferences() {
        // references stored as TextAttribute on Item
        return MockTextAttribute.getValue(getItem(), ATTR_MESSAGE_REFERENCES);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setReferences(java.lang.String)
     */
    public void setReferences(String references) {
        // references stored as TextAttribute on Item
        MockTextAttribute.setValue(getItem(), ATTR_MESSAGE_REFERENCES, references);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceMessageStamp#setReferences(java.io.Reader)
     */
    public void setReferences(Reader references) {
        // references stored as TextAttribute on Item
        MockTextAttribute.setValue(getItem(), ATTR_MESSAGE_REFERENCES, references);
        updateTimestamp();
    }

    /**
     * Return MessageStamp from Item
     * @param item
     * @return MessageStamp from Item
     */
    public static MessageStamp getStamp(Item item) {
        return (MessageStamp) item.getStamp(MessageStamp.class);
    }
    
    public Stamp copy() {
        MessageStamp stamp = new MockMessageStamp();
        return stamp;
    }
}
