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
package org.osaf.cosmo.calendar.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Trigger;

import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.calendar.InstanceList;


/**
 * Contains methods for determining if a Calendar matches
 * a CalendarFilter.
 */
public class CalendarFilterEvaluater {

    private static final String COMP_VCALENDAR = "VCALENDAR";

    private final Stack<Component> stack = new Stack<>();

    public CalendarFilterEvaluater() {}


    /**
     * Evaulate CalendarFilter against a Calendar.
     * @param calendar calendar to evaluate against
     * @param filter filter to apply
     * @return true if the filter
     * @throws UnsupportedQueryException if filter represents a query
     *                              that the server does not support
     */
    public boolean evaluate(Calendar calendar, CalendarFilter filter) {
        ComponentFilter rootFilter = filter.getFilter();

        // root filter must be "VCALENDAR"
        if(!COMP_VCALENDAR.equalsIgnoreCase(rootFilter.getName()))
            return false;

        stack.clear();

        // evaluate all component filters
        for(Iterator it = rootFilter.getComponentFilters().iterator(); it.hasNext();) {
            ComponentFilter compFilter = (ComponentFilter) it.next();

            // If any component filter fails to match, then the calendar filter
            // does not match
            if(!evaluateComps(calendar.getComponents(), compFilter))
                return false;
        }

        return true;
    }

    private boolean evaluate(ComponentList comps, ComponentFilter filter) {
        // Evaluate component filter against a set of components.
        // If any component matches, then evaluation succeeds.
        // This is basically a big OR
        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            Component parent = it.next();
            stack.push(parent);
            if(evaluateComps(getSubComponents(parent),filter)==true) {
                stack.pop();
                return true;
            }
            stack.pop();
        }
        return false;
    }

    private boolean evaluate(ComponentList comps, PropertyFilter filter) {

        // Evaluate property filter against a set of components.
        // If any component matches, then evaluation succeeds.
        // This is basically a big OR
        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            if(evaluate(it.next(),filter)==true)
                return true;
        }
        return false;
    }

    private boolean evaluateComps(ComponentList components, ComponentFilter filter) {

        /*The CALDAV:comp-filter XML element is empty and the
        calendar component type specified by the "name"
        attribute exists in the current scope;*/
        if(filter.getComponentFilters().isEmpty() && filter.getPropFilters().isEmpty() && filter.getTimeRangeFilter()==null && filter.getIsNotDefinedFilter()==null) {
            ComponentList comps = components.getComponents(filter.getName().toUpperCase());
            return !comps.isEmpty();
        }

        /* The CALDAV:comp-filter XML element contains a CALDAV:is-not-
        defined XML element and the calendar object or calendar
        component type specified by the "name" attribute does not exist
        in the current scope;*/
        if(filter.getIsNotDefinedFilter()!=null) {
            ComponentList comps = components.getComponents(filter.getName().toUpperCase());
            return comps.isEmpty();
        }

        // Match the component
        ComponentList comps = components.getComponents(filter.getName().toUpperCase());
        if(comps.isEmpty())
            return false;

        /*The CALDAV:comp-filter XML element contains a CALDAV:time-range
        XML element and at least one recurrence instance in the
        targeted calendar component is scheduled to overlap the
        specified time range, and all specified CALDAV:prop-filter and
        CALDAV:comp-filter child XML elements also match the targeted
        calendar component;*/

        // Evaulate time-range filter
        if(filter.getTimeRangeFilter()!=null) {
            if(evaluate(comps, filter.getTimeRangeFilter())==false)
                return false;
        }

        for(Iterator<ComponentFilter> it = filter.getComponentFilters().iterator(); it.hasNext();) {
            if(evaluate(comps, it.next())==false)
                return false;
        }

        for(Iterator<PropertyFilter> it = filter.getPropFilters().iterator(); it.hasNext();) {
            if(evaluate(comps, it.next())==false)
                return false;
        }

        return true;
    }

    private boolean evaluate(Component component, PropertyFilter filter) {

        /*The CALDAV:prop-filter XML element is empty and a property of
        the type specified by the "name" attribute exists in the
        enclosing calendar component;*/
        if(filter.getParamFilters().isEmpty() && filter.getTimeRangeFilter()==null && filter.getIsNotDefinedFilter()==null && filter.getTextMatchFilter()==null) {
            PropertyList props = component.getProperties(filter.getName());
            return !props.isEmpty();
        }

        /*The CALDAV:prop-filter XML element contains a CALDAV:is-not-
        defined XML element and no property of the type specified by
        the "name" attribute exists in the enclosing calendar
        component;*/
        if(filter.getIsNotDefinedFilter()!=null) {
            PropertyList props = component.getProperties(filter.getName());
            return props.isEmpty();
        }

        // Match the property
        PropertyList props = component.getProperties(filter.getName());
        if(props.isEmpty())
            return false;

        /*The CALDAV:prop-filter XML element contains a CALDAV:time-range
        XML element and the property value overlaps the specified time
        range, and all specified CALDAV:param-filter child XML elements
        also match the targeted property;*/

        // Evaulate time-range filter
        if(filter.getTimeRangeFilter()!=null) {
            if(evaluate(props, filter.getTimeRangeFilter())==false)
                return false;
        }

        if(filter.getTextMatchFilter()!=null) {
            props = evaluate(props, filter.getTextMatchFilter());
            if(props.isEmpty())
                return false;
        }

        for(Iterator<ParamFilter> it = filter.getParamFilters().iterator(); it.hasNext();) {
            if(evaluate(props, it.next())==false)
                    return false;
        }

        return true;
    }

    private boolean evaluate(PropertyList props, ParamFilter filter) {
        // Evaluate param filter against a set of properties.
        // If any property matches, then evaluation succeeds.
        // This is basically a big OR
        for(Iterator<Property> it=props.iterator();it.hasNext();) {
            if(evaulate(it.next(),filter)==true)
                return true;
        }
        return false;
    }

    private boolean evaulate(Property property, ParamFilter filter) {

        /*The CALDAV:param-filter XML element is empty and a parameter of
        the type specified by the "name" attribute exists on the
        calendar property being examined;*/
        if(filter.getIsNotDefinedFilter()==null && filter.getTextMatchFilter()==null) {
            ParameterList params = property.getParameters(filter.getName());
            return !params.isEmpty();
        }

       /* The CALDAV:param-filter XML element contains a CALDAV:is-not-
        defined XML element and no parameter of the type specified by
        the "name" attribute exists on the calendar property being
        examined;*/
        if(filter.getIsNotDefinedFilter()!=null) {
            ParameterList params = property.getParameters(filter.getName());
            return params.isEmpty();
        }

        // Match the parameter
        ParameterList params = property.getParameters(filter.getName());
        if(params.isEmpty())
            return false;

        // Match the TextMatchFilter
        if(evaluate(params, filter.getTextMatchFilter())==false)
            return false;

        return true;
    }

    private PropertyList evaluate(PropertyList props, TextMatchFilter filter) {
        PropertyList results = new PropertyList();
        for(Iterator<Property> it = props.iterator(); it.hasNext();) {
            Property prop = it.next();
            if(evaluate(prop,filter)==true)
                results.add(prop);
        }
        return results;
    }

    private boolean evaluate(ParameterList params, TextMatchFilter filter) {
        // Evaluate textmatch filter against a set of parameters.
        // If any param matches, then evaluation succeeds.
        // This is basically a big OR
        for(Iterator<Parameter> it = params.iterator(); it.hasNext();) {
            Parameter param = it.next();
            if(evaluate(param,filter)==true)
                return true;
        }
        return false;
    }

    private boolean evaluate(Property property, TextMatchFilter filter) {
        boolean matched = false;
        if(filter.isCaseless())
            matched = property.getValue().toLowerCase().contains(filter.getValue().toLowerCase());
        else
            matched = property.getValue().contains(filter.getValue());

        if(filter.isNegateCondition())
            return !matched;
        else
            return matched;
    }

    private boolean evaluate(Parameter param, TextMatchFilter filter) {
        boolean matched = false;
        if(filter.isCaseless())
            matched = param.getValue().toLowerCase().contains(filter.getValue().toLowerCase());
        else
            matched = param.getValue().contains(filter.getValue());

        if(filter.isNegateCondition())
            return !matched;
        else
            return matched;
    }

    private boolean evaluate(ComponentList comps, TimeRangeFilter filter) {

        Component comp = (Component) comps.get(0);

        if(comp instanceof VEvent || comp instanceof VAvailability)
            return evaluateVEventTimeRange(comps, filter);
        else if(comp instanceof VFreeBusy)
            return evaulateVFreeBusyTimeRange((VFreeBusy) comp, filter);
        else if(comp instanceof VToDo)
            return evaulateVToDoTimeRange(comps, filter);
        else if(comp instanceof VJournal)
            return evaluateVJournalTimeRange((VJournal) comp, filter);
        else if(comp instanceof VAlarm)
            return evaluateVAlarmTimeRange(comps, filter);
        else
            return false;
    }

    private boolean evaluate(PropertyList props, TimeRangeFilter filter) {
        // Evaluate timerange filter against a set of properties.
        // If any property matches, then evaluation succeeds.
        // This is basically a big OR
        for(Iterator<Property> it = props.iterator(); it.hasNext();) {
            if(evaluate(it.next(),filter)==true)
                return true;
        }
        return false;
    }

    private boolean evaluate(Property property, TimeRangeFilter filter) {
        if(!(property instanceof DateProperty) )
            return false;

        DateProperty dateProp = (DateProperty) property;
        Date date = dateProp.getDate();

        return (  (date.before(filter.getPeriod().getEnd()) &&
              date.after(filter.getPeriod().getStart())) ||
              date.equals(filter.getPeriod().getStart()) );
    }

    private ComponentList getSubComponents(Component component) {
        if(component instanceof VEvent)
            return ((VEvent) component).getAlarms();
        else if(component instanceof VTimeZone)
            return ((VTimeZone) component).getObservances();
        else if(component instanceof VToDo)
            return ((VToDo) component).getAlarms();

        return new ComponentList();
    }

    /*
     * A VEVENT component overlaps a given time range if the condition
        for the corresponding component state specified in the table below
        is satisfied.  Note that, as specified in [RFC2445], the DTSTART
        property is REQUIRED in the VEVENT component.  The conditions
        depend on the presence of the DTEND and DURATION properties in the
        VEVENT component.  Furthermore, the value of the DTEND property
        MUST be later in time than the value of the DTSTART property.  The
        duration of a VEVENT component with no DTEND and DURATION
        properties is 1 day (+P1D) when the DTSTART is a DATE value, and 0
        seconds when the DTSTART is a DATE-TIME value.

        +---------------------------------------------------------------+
        | VEVENT has the DTEND property?                                |
        |   +-----------------------------------------------------------+
        |   | VEVENT has the DURATION property?                         |
        |   |   +-------------------------------------------------------+
        |   |   | DURATION property value is greater than 0 seconds?    |
        |   |   |   +---------------------------------------------------+
        |   |   |   | DTSTART property is a DATE-TIME value?            |
        |   |   |   |   +-----------------------------------------------+
        |   |   |   |   | Condition to evaluate                         |
        +---+---+---+---+-----------------------------------------------+
        | Y | N | N | * | (start <  DTEND AND end > DTSTART)            |
        +---+---+---+---+-----------------------------------------------+
        | N | Y | Y | * | (start <  DTSTART+DURATION AND end > DTSTART) |
        |   |   +---+---+-----------------------------------------------+
        |   |   | N | * | (start <= DTSTART AND end > DTSTART)          |
        +---+---+---+---+-----------------------------------------------+
        | N | N | N | Y | (start <= DTSTART AND end > DTSTART)          |
        +---+---+---+---+-----------------------------------------------+
        | N | N | N | N | (start <  DTSTART+P1D AND end > DTSTART)      |
        +---+---+---+---+-----------------------------------------------+
     */
    private boolean evaluateVEventTimeRange(ComponentList comps, TimeRangeFilter filter) {

        InstanceList instances = new InstanceList();
        if(filter.getTimezone()!=null)
            instances.setTimezone(new TimeZone(filter.getTimezone()));
        ArrayList<Component> mods = new ArrayList<>();

        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            Component comp = it.next();
            // Add master first
            if(comp.getProperty(Property.RECURRENCE_ID)==null)
                instances.addComponent(comp, filter.getPeriod().getStart(), filter.getPeriod().getEnd());
        }

        // Add overides after master has been added
        for(Component mod : mods)
            instances.addOverride(mod, filter.getPeriod().getStart(), filter.getPeriod().getEnd());

        if(!instances.isEmpty())
            return true;

        return false;
    }

    /*
        A VFREEBUSY component overlaps a given time range if the condition
        for the corresponding component state specified in the table below
        is satisfied.  The conditions depend on the presence in the
        VFREEBUSY component of the DTSTART and DTEND properties, and any
        FREEBUSY properties in the absence of DTSTART and DTEND.  Any
        DURATION property is ignored, as it has a special meaning when
        used in a VFREEBUSY component.

        When only FREEBUSY properties are used, each period in each
        FREEBUSY property is compared against the time range, irrespective
        of the type of free busy information (free, busy, busy-tentative,
        busy-unavailable) represented by the property.


        +------------------------------------------------------+
        | VFREEBUSY has both the DTSTART and DTEND properties? |
        |   +--------------------------------------------------+
        |   | VFREEBUSY has the FREEBUSY property?             |
        |   |   +----------------------------------------------+
        |   |   | Condition to evaluate                        |
        +---+---+----------------------------------------------+
        | Y | * | (start <= DTEND) AND (end > DTSTART)         |
        +---+---+----------------------------------------------+
        | N | Y | (start <  freebusy-period-end) AND           |
        |   |   | (end   >  freebusy-period-start)             |
        +---+---+----------------------------------------------+
        | N | N | FALSE                                        |
        +---+---+----------------------------------------------+
     */
    private boolean evaulateVFreeBusyTimeRange(VFreeBusy freeBusy, TimeRangeFilter filter) {
        DtStart start = freeBusy.getStartDate();
        DtEnd end = freeBusy.getEndDate();

        if (start != null && end != null) {
            InstanceList instances = new InstanceList();
            if (filter.getTimezone() != null)
                instances.setTimezone(new TimeZone(filter.getTimezone()));
            instances.addComponent(freeBusy, filter.getPeriod().getStart(),
                    filter.getPeriod().getEnd());
            return !instances.isEmpty();
        }

        PropertyList props = freeBusy.getProperties(Property.FREEBUSY);
        if(props.isEmpty())
            return false;

        Iterator<FreeBusy> it = props.iterator();
        while(it.hasNext()) {
            FreeBusy fb = it.next();
            PeriodList periods = fb.getPeriods();
            Iterator<Period> periodIt = periods.iterator();
            while(periodIt.hasNext()) {
                Period period = periodIt.next();
                if(filter.getPeriod().getStart().before(period.getEnd()) &&
                   filter.getPeriod().getEnd().after(period.getStart()))
                    return true;
            }
        }

        return false;
    }

    /*
      A VJOURNAL component overlaps a given time range if the condition
        for the corresponding component state specified in the table below
        is satisfied.  The conditions depend on the presence of the
        DTSTART property in the VJOURNAL component and on whether the
        DTSTART is a DATE-TIME or DATE value.  The effective "duration" of
        a VJOURNAL component is 1 day (+P1D) when the DTSTART is a DATE
        value, and 0 seconds when the DTSTART is a DATE-TIME value.

        +----------------------------------------------------+
        | VJOURNAL has the DTSTART property?                 |
        |   +------------------------------------------------+
        |   | DTSTART property is a DATE-TIME value?         |
        |   |   +--------------------------------------------+
        |   |   | Condition to evaluate                      |
        +---+---+--------------------------------------------+
        | Y | Y | (start <= DTSTART)     AND (end > DTSTART) |
        +---+---+--------------------------------------------+
        | Y | N | (start <  DTSTART+P1D) AND (end > DTSTART) |
        +---+---+--------------------------------------------+
        | N | * | FALSE                                      |
        +---+---+--------------------------------------------+ */
    private boolean evaluateVJournalTimeRange(VJournal journal, TimeRangeFilter filter) {
        DtStart start = journal.getStartDate();

        if(start==null)
            return false;

        InstanceList instances = new InstanceList();
        if (filter.getTimezone() != null)
            instances.setTimezone(new TimeZone(filter.getTimezone()));
        instances.addComponent(journal, filter.getPeriod().getStart(),
                filter.getPeriod().getEnd());
        return !instances.isEmpty();
    }

    /*
     *  A VTODO component is said to overlap a given time range if the
        condition for the corresponding component state specified in the
        table below is satisfied.  The conditions depend on the presence
        of the DTSTART, DURATION, DUE, COMPLETED, and CREATED properties
        in the VTODO component.  Note that, as specified in [RFC2445], the
        DUE value MUST be a DATE-TIME value equal to or after the DTSTART
        value if specified.

     +-------------------------------------------------------------------+
     | VTODO has the DTSTART property?                                   |
     |   +---------------------------------------------------------------+
     |   |   VTODO has the DURATION property?                            |
     |   |   +-----------------------------------------------------------+
     |   |   | VTODO has the DUE property?                               |
     |   |   |   +-------------------------------------------------------+
     |   |   |   | VTODO has the COMPLETED property?                     |
     |   |   |   |   +---------------------------------------------------+
     |   |   |   |   | VTODO has the CREATED property?                   |
     |   |   |   |   |   +-----------------------------------------------+
     |   |   |   |   |   | Condition to evaluate                         |
     +---+---+---+---+---+-----------------------------------------------+
     | Y | Y | N | * | * | (start  <= DTSTART+DURATION)  AND             |
     |   |   |   |   |   | ((end   >  DTSTART)  OR                       |
     |   |   |   |   |   |  (end   >= DTSTART+DURATION))                 |
     +---+---+---+---+---+-----------------------------------------------+
     | Y | N | Y | * | * | ((start <  DUE)      OR  (start <= DTSTART))  |
     |   |   |   |   |   | AND                                           |
     |   |   |   |   |   | ((end   >  DTSTART)  OR  (end   >= DUE))      |
     +---+---+---+---+---+-----------------------------------------------+
     | Y | N | N | * | * | (start  <= DTSTART)  AND (end >  DTSTART)     |
     +---+---+---+---+---+-----------------------------------------------+
     | N | N | Y | * | * | (start  <  DUE)      AND (end >= DUE)         |
     +---+---+---+---+---+-----------------------------------------------+
     | N | N | N | Y | Y | ((start <= CREATED)  OR  (start <= COMPLETED))|
     |   |   |   |   |   | AND                                           |
     |   |   |   |   |   | ((end   >= CREATED)  OR  (end   >= COMPLETED))|
     +---+---+---+---+---+-----------------------------------------------+
     | N | N | N | Y | N | (start  <= COMPLETED) AND (end  >= COMPLETED) |
     +---+---+---+---+---+-----------------------------------------------+
     | N | N | N | N | Y | (end    >  CREATED)                           |
     +---+---+---+---+---+-----------------------------------------------+
     | N | N | N | N | N | TRUE                                          |
     +---+---+---+---+---+-----------------------------------------------+
     */
    private boolean evaulateVToDoTimeRange(ComponentList comps, TimeRangeFilter filter) {
        ArrayList<Component> mods = new ArrayList<>();
        VToDo master = null;

        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            Component comp = it.next();
            // Add master first
            if(comp.getProperty(Property.RECURRENCE_ID)==null)
                master = (VToDo) comp;
        }

        // If there is no DTSTART, evaluate using special rules as
        // listed in the nice state table above
        if(mods.isEmpty()) {
            if(master.getStartDate()==null)
                return isVToDoInRange(master, filter.getPeriod());
        }

        // Otherwise use standard InstantList, which relies on
        // DTSTART,DURATION.
        // TODO: Handle case of no DURATION and instead DUE
        // DUE is kind of like DTEND
        InstanceList instances = new InstanceList();
        if(filter.getTimezone()!=null)
            instances.setTimezone(new TimeZone(filter.getTimezone()));

        instances.addComponent(master, filter.getPeriod().getStart(), filter
                .getPeriod().getEnd());

        // Add overides after master has been added
        for(Component mod : mods)
            instances.addOverride(mod, filter.getPeriod().getStart(), filter.getPeriod().getEnd());

        if(!instances.isEmpty())
            return true;

        return false;
    }

    /*
     * Determine if VTODO overlaps timerange assuming the VTODO
     * has no DTSTART, using the state table defined in RFC-4791
     * Sec 9.9.
     */
    private boolean isVToDoInRange(VToDo vtodo, Period period) {

        if(vtodo.getDue() != null) {
            //(start  <  DUE)      AND (end >= DUE)
            Date dueDate = vtodo.getDue().getDate();
            return (period.getStart().compareTo(dueDate) < 0) &&
                   (period.getEnd().compareTo(dueDate) >=0);
        } else if(vtodo.getCreated()!=null && vtodo.getDateCompleted()!=null) {
            //((start <= CREATED)  OR  (start <= COMPLETED))
            //AND
            //((end   >= CREATED)  OR  (end   >= COMPLETED))
            Date createDate = vtodo.getCreated().getDate();
            Date completeDate = vtodo.getDateCompleted().getDate();
            return ( (period.getStart().compareTo(createDate)<=0 ||
                      period.getStart().compareTo(completeDate)<=0) &&
                     (period.getEnd().compareTo(createDate)>=0 ||
                      period.getEnd().compareTo(completeDate)>=0)
                    );
        } else if(vtodo.getDateCompleted()!=null) {
            //(start  <= COMPLETED) AND (end  >= COMPLETED)
            Date completeDate = vtodo.getDateCompleted().getDate();
            return (period.getStart().compareTo(completeDate)<=0) &&
                   (period.getEnd().compareTo(completeDate)>=0);
        } else if(vtodo.getCreated()!=null) {
            //(end    >  CREATED)
            Date createDate = vtodo.getCreated().getDate();
            return period.getEnd().compareTo(createDate) > 0;
        } else {
            return true;
        }

    }

    /*
     * A VALARM component is said to overlap a given time range if the
        following condition holds:

           (start <= trigger-time) AND (end > trigger-time)

       A VALARM component can be defined such that it triggers repeatedly.
       Such a VALARM component is said to overlap a given time range if at
       least one of its triggers overlaps the time range.
     */

    private boolean evaluateVAlarmTimeRange(ComponentList comps, TimeRangeFilter filter) {

        // VALARAM must have parent VEVENT or VTODO
        Component parent = stack.peek();
        if(parent==null)
            return false;

        // See if trigger-time overlaps the time range for each VALARM
        for(Iterator<Component> it=comps.iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            Trigger trigger = alarm.getTrigger();
            if(trigger==null)
                continue;

            List<Date> triggerDates = ICalendarUtils.getTriggerDates(alarm, parent);

            for(Date triggerDate: triggerDates) {
                if(filter.getPeriod().getStart().compareTo(triggerDate)<=0 &&
                   filter.getPeriod().getEnd().after(triggerDate))
                   return true;
            }
        }

        return false;
    }
}
