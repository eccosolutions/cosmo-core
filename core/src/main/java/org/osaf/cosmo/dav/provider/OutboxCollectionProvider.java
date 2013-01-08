/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.provider;

import java.io.IOException;
import java.util.ArrayList;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.RequestStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavRequest;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResponse;
import org.osaf.cosmo.dav.caldav.ScheduleMultiResponse;
import org.osaf.cosmo.dav.caldav.ScheduleResponse;
import org.osaf.cosmo.dav.io.DavInputContext;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.User;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavOutboxCollection</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see CollectionProvider
 * @see BaseProvider
 */
public class OutboxCollectionProvider extends CollectionProvider {
    private static final Log log = LogFactory.getLog(OutboxCollectionProvider.class);
	
    public OutboxCollectionProvider(DavResourceFactory resourceFactory, EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    /* (non-Javadoc)
     * @see org.osaf.cosmo.dav.provider.BaseProvider#post(org.osaf.cosmo.dav.DavRequest, org.osaf.cosmo.dav.DavResponse, org.osaf.cosmo.dav.DavResource)
     */
    @Override
    public void post(
    		DavRequest request
    		, DavResponse response
    		, DavResource resource
    		) throws DavException ,IOException
	{
		if (log.isTraceEnabled())
			log.trace("Handling POST method for Outbox");
		
		int status = DavResponse.SC_OK;
        ScheduleMultiResponse ms = new ScheduleMultiResponse();
		try {
			//according to http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-05
			// only REQUEST and REFRESH are available for post
	        DavInputContext ctx = (DavInputContext) createInputContext(request);
	        Calendar calendar = ctx.getCalendar(true);
	        
        	processPostRequest(calendar, ms);
		} catch (Exception exc) {
			status = DavResponse.SC_INTERNAL_SERVER_ERROR;
		}

        response.sendXmlResponse(ms, status);
	}
    
    private void processPostRequest(Calendar calendar, ScheduleMultiResponse ms) {
        if (!Method.REQUEST.equals(calendar.getMethod()))
        	return;
        
    	processPostFreeBusyRequest(calendar, ms);
    }
    
    @SuppressWarnings("unchecked")
    private void processPostFreeBusyRequest(Calendar calendar, ScheduleMultiResponse ms) {
    	for (VFreeBusy freebusy : (ArrayList<VFreeBusy>)calendar.getComponents(VFreeBusy.VFREEBUSY)){
    		DateTime periodStrart = (DateTime)freebusy.getStartDate().getDate();
    		DateTime periodEnd = (DateTime)freebusy.getEndDate().getDate();
    		Period period = new Period(periodStrart, periodEnd);
    		
    		User user = null;
    		for (Attendee attendee : (ArrayList<Attendee>)freebusy.getProperties(Property.ATTENDEE)) {
    			// since we might have multiple responses for one user lets create a flag here
				try {
					String email = attendee.getCalAddress().getSchemeSpecificPart();
					user = getResourceFactory().getUserService().getUserByEmail(email);
				} catch (Exception e) {
	    			ScheduleResponse resp = new ScheduleResponse(attendee.getCalAddress().toString());
					resp.setStatus(RequestStatus.CLIENT_ERROR);
					ms.addResponse(resp);
					continue;
				}
				
				// Handle case where user doesn't exist.
				// Not sure what to return, it seems CalendarServer returns the following
				if(user==null) {
				    ScheduleResponse resp = new ScheduleResponse(attendee.getCalAddress().toString());
                    resp.setStatus( "3.7;Invalid Calendar User");
                    ms.addResponse(resp);
                    continue;
				}
				
				/*
				 * TODO Apple iCal send this property, need to be taken into action
				 * 
				https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/icalendar-maskuids-02.txt?rev=1510
				120	   Property Name: X-CALENDARSERVER-MASK-UID
				121	
				122	   Purpose: This property indicates the unique identifier for a calendar
				123	   component that is to be ignored when calculating free-busy time.
				124
				*/								
				VFreeBusy vfb = getResourceFactory().getCalendarQueryProcessor().freeBusyQuery(user, period);
				vfb.getProperties().add(attendee);
				vfb.getProperties().add(freebusy.getOrganizer());
				Calendar cal = ICalendarUtils.createBaseCalendar(vfb);
				cal.getProperties().add(Method.REPLY);
    			ScheduleResponse resp = new ScheduleResponse(attendee.getCalAddress().toString());
				resp.setCalendarData(cal.toString());
				ms.addResponse(resp);
    		}
    	}
    }
}
