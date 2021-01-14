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

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.uuid.VersionFourGenerator;
import org.osaf.cosmo.model.*;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * EntityFactory implementation that uses Hibernate
 * persistent objects.
 */
public class HibEntityFactory implements EntityFactory {

    private IdentifierGenerator idGenerator = new VersionFourGenerator();

    public CollectionItem createCollection() {
        return new HibCollectionItem();
    }

    public NoteItem createNote() {
        return new HibNoteItem();
    }

    public AvailabilityItem createAvailability() {
        return new HibAvailabilityItem();
    }

    public BinaryAttribute createBinaryAttribute(QName qname, byte[] bytes) {
        return new HibBinaryAttribute(qname, bytes);
    }

    public BinaryAttribute createBinaryAttribute(QName qname, InputStream is) {
        return new HibBinaryAttribute(qname, is);
    }

    public CalendarAttribute createCalendarAttribute(QName qname, Calendar cal) {
        return new HibCalendarAttribute(qname, cal);
    }

    public CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col) {
        return new HibCalendarCollectionStamp(col);
    }

    public CollectionSubscription createCollectionSubscription() {
        return new HibCollectionSubscription();
    }

    public DecimalAttribute createDecimalAttribute(QName qname, BigDecimal bd) {
        return new HibDecimalAttribute(qname, bd);
    }

    public EventExceptionStamp createEventExceptionStamp(NoteItem note) {
        return new HibEventExceptionStamp(note);
    }

    public EventStamp createEventStamp(NoteItem note) {
        return new HibEventStamp(note);
    }

    public FileItem createFileItem() {
        return new HibFileItem();
    }

    public FreeBusyItem createFreeBusy() {
        return new HibFreeBusyItem();
    }

    public IntegerAttribute createIntegerAttribute(QName qname, Long longVal) {
        return new HibIntegerAttribute(qname, longVal);
    }

    public XmlAttribute createXMLAttribute(QName qname, Element e) {
        return new HibXmlAttribute(qname, e);
    }

    public MessageStamp createMessageStamp() {
        return new HibMessageStamp();
    }

    public PasswordRecovery createPasswordRecovery(User user, String key) {
        return new HibPasswordRecovery(user, key);
    }

    public QName createQName(String namespace, String localname) {
        return new HibQName(namespace, localname);
    }

    public StringAttribute createStringAttribute(QName qname, String str) {
        return new HibStringAttribute(qname, str);
    }

    public TaskStamp createTaskStamp() {
        return new HibTaskStamp();
    }

    public TextAttribute createTextAttribute(QName qname, Reader reader) {
        return new HibTextAttribute(qname, reader);
    }

    public User createUser() {
        return new HibUser();
    }

    public String generateUid() {
        return idGenerator.nextIdentifier().toString();
    }

}
