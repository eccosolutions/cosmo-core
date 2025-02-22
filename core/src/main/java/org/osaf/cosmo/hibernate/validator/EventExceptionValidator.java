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
package org.osaf.cosmo.hibernate.validator;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.util.CalendarUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.io.Serializable;

/**
 * Check if a Calendar object contains a valid VEvent exception.
 * @author randy
 *
 */
public class EventExceptionValidator implements ConstraintValidator<EventException, Object>, Serializable {

    private static final Log log = LogFactory.getLog(EventExceptionValidator.class);

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        Calendar calendar = null;
        try {
            calendar = (Calendar) value;

            // validate entire icalendar object
            calendar.validate(true);

            // additional check to prevent bad .ics
            CalendarUtils.parseCalendar(calendar.toString());

            // make sure we have a VEVENT with a recurrenceid
            ComponentList comps = calendar.getComponents();
            if(comps==null) {
                log.warn("error validating event exception: " + calendar.toString());
                return false;
            }

            comps = comps.getComponents(Component.VEVENT);
            if(comps.isEmpty()) {
                log.warn("error validating event exception: " + calendar.toString());
                return false;
            }

            VEvent event = (VEvent) comps.get(0);
            if(event ==null) {
                log.warn("error validating event exception: " + calendar.toString());
                return false;
            }

            RecurrenceId recurrenceId = event.getRecurrenceId();

            if(recurrenceId==null || recurrenceId.getValue()==null ||
                    recurrenceId.getValue() != null && recurrenceId.getValue().isEmpty()) {
                log.warn("error validating event exception: " + calendar.toString());
                return false;
            }

            return true;
        } catch(ValidationException ve) {
            log.warn("event validation error", ve);
            log.warn("error validating event: " + calendar.toString() );
            return false;
        } catch (RuntimeException | IOException e) {
            return false;
        } catch(ParserException e) {
            log.warn("parse error", e);
            log.warn("error parsing event: " + calendar.toString() );
            return false;
        }
    }

    public void initialize(EventException parameters) {
    }
}

