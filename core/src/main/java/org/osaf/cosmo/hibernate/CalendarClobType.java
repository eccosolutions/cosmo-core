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
package org.osaf.cosmo.hibernate;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAvailability;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.ClobProxy;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractJavaType;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.hibernate.type.descriptor.jdbc.ClobJdbcType;
import org.osaf.cosmo.calendar.util.CalendarUtils;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.ParseException;


/**
 * Custom Hibernate type that persists ical4j Calendar object
 * to CLOB field in database.
 */
public class CalendarClobType extends AbstractSingleColumnStandardBasicType<Calendar> {
    private static final Log log = LogFactory.getLog(CalendarClobType.class);

    public CalendarClobType() {
        super(ClobJdbcType.DEFAULT, new CalendarTypeDescriptor());
    }

    public String getName() {
        return "calendar_clob";
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    @Override
    protected MutabilityPlan<Calendar> getMutabilityPlan() {
        return new CalendarMutabilityPlan();
    }

    private static class CalendarMutabilityPlan extends MutableMutabilityPlan<Calendar> {
        @Override
        protected Calendar deepCopyNotNull(Calendar value) {
            try {
                final Calendar copy = new Calendar(value);
                // TODO: Remove the below availability mangling when the underlying iCal bug is fixed
                final ComponentList<CalendarComponent> vAvailabilities = copy.getComponents(Component.VAVAILABILITY);
                final ComponentList<CalendarComponent> originalAvailabilities = value.getComponents(Component.VAVAILABILITY);
                for (int i = 0; i < vAvailabilities.size(); i++) {
                    VAvailability vAvailability = (VAvailability) vAvailabilities.get(i);
                    if (vAvailability.getAvailable().isEmpty()) {
                        final VAvailability originalAvailability = (VAvailability) originalAvailabilities.get(i);
                        // Check it's the same availability we're mangling
                        assert originalAvailability.getProperty(Property.UID).equals(vAvailability.getProperty(Property.UID));
                        vAvailability.getAvailable().addAll(originalAvailability.getAvailable());
                    } else {
                        log.warn("VAvailability.copy() has been fixed - remove the workaround in CalendarClobType.deepCopy()");
                    }
                }
                return copy;
            } catch (IOException e) {
                throw new HibernateException("Unable to read original calendar", e);
            } catch (ParseException e) {
                log.error("parse error with following ics:" + value);
                throw new HibernateException("Unable to parse original calendar", e);
            } catch (URISyntaxException e) {
                throw new HibernateException("Unknown syntax exception", e);
            }
        }
    }

    /** for what was calendar_clob */
    public static class CalendarTypeDescriptor extends AbstractJavaType<Calendar> {
        public CalendarTypeDescriptor() {
            super(Calendar.class, new CalendarMutabilityPlan());
        }

        @Override
        public String toString(Calendar value) {
            return value.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <X> X unwrap(Calendar value, Class<X> type, WrapperOptions options) {
            if (value == null) {
                return null;
            } else if (Calendar.class.isAssignableFrom(type)) {
                return (X) value;
            } else if (CharacterStream.class.isAssignableFrom(type)) {
                return (X) new CharacterStreamImpl(value.toString());
            } else if (Clob.class.isAssignableFrom(type)) {
                return (X) ClobProxy.generateProxy(value.toString());
            } else if (String.class.isAssignableFrom(type)) {
                return (X) value.toString();
            }
                throw unknownUnwrap(type);
        }

        @Override
        public <X> Calendar wrap(X value, WrapperOptions options) {
            try {
                if (value == null) {
                    return null;
                } else if (Calendar.class.isAssignableFrom(value.getClass())) {
                    return (Calendar) value;
                } else if (Clob.class.isAssignableFrom(value.getClass())) {
                    try (Reader characterStream = ((Clob) value).getCharacterStream()) {
                        return CalendarUtils.parseCalendar(characterStream);
                    }
                } else if (Reader.class.isAssignableFrom(value.getClass())) {
                    try {
                        return CalendarUtils.parseCalendar((Reader) value);
                    } finally {
                        ((Reader) value).close();
                    }
                }
            } catch ( SQLException e ) {
                throw new HibernateException( "Unable to access clob stream", e );
            } catch (ParserException e) {
                log.error("error parsing icalendar from db", e);
                // shouldn't happen because we always persist valid data
                throw new HibernateException("cannot parse icalendar stream");
            } catch (IOException ioe) {
                throw new HibernateException("cannot read icalendar stream");
            }
            throw unknownWrap(value.getClass());
        }
    }
}
