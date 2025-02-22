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
package org.osaf.cosmo.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.icalendar.ICalendarClientFilterManager;

/**
 * A filter used to initialize the client identifier for
 * the ICalendarClientFilterManager, which is responsible for
 * exporting icalendar tailored to a specific client.
 *
 * The filter relies on a map of regex expression keys that
 * map to a client identifier key.
 */
public class ClientICalendarFilter implements Filter {

    private static final Log log = LogFactory.getLog(ClientICalendarFilter.class);

    private ICalendarClientFilterManager filterManager;
    private Map<String, String> clientKeyMap = new HashMap<>();


    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        // translate User-Agent to client identifier key
        String userAgent = translateUserAgent(httpRequest.getHeader("User-Agent"));

        try {
            if(log.isDebugEnabled())
                log.debug("setting client to: " + userAgent);
            filterManager.setClient(userAgent);
            chain.doFilter(request, response);
        } finally {
            filterManager.setClient(null);
        }

    }

    private String translateUserAgent(String agent) {

        if(agent==null)
            return null;

        // Translate User-Agent header into client key by
        // finding match using rules in clientKeyMap.
        for(Entry<String, String> entry :clientKeyMap.entrySet()) {
            if(agent.matches(entry.getKey()))
                return entry.getValue();
        }

        return agent;
    }

    public void setClientKeyMap(Map<String, String> clientKeyMap) {
        this.clientKeyMap = clientKeyMap;
    }

    public void setFilterManager(ICalendarClientFilterManager filterManager) {
        this.filterManager = filterManager;
    }

    public void init(FilterConfig arg0) {

    }
}
