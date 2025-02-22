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
package org.osaf.cosmo.service.account;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.ServiceEvent;
import org.osaf.cosmo.service.ServiceListener;

/**
 * A <code>ServiceListener</code> that is responsible for sending
 * an activation notification upon user creation.
 */
public class ActivationListener implements ServiceListener {
    private static final Log log = LogFactory.getLog(ActivationListener.class);

    private final AccountActivator activator;
    private final ActivationContext context;

    public ActivationListener(AccountActivator activator,
                              ActivationContext context) {
        this.activator = activator;
        this.context = context;
    }

    public void before(ServiceEvent se) {
        if (! se.getId().equals("CREATE_USER"))
            return;

        User user = (User) se.getState()[0];
        if (activator.isRequired() && ! user.isOverlord()) {
            String activationId = activator.generateActivationToken();
            if (log.isDebugEnabled())
                log.debug("setting activation id " + activationId +
                          " for user " + user.getUsername());
            user.setActivationId(activationId);
        }
    }

    public void after(ServiceEvent se) {
        if (! se.getId().equals("CREATE_USER"))
            return;

        User user = (User) se.getState()[0];
        if (activator.isRequired() && ! user.isOverlord()) {
            if (log.isDebugEnabled())
                log.debug("sending activation message for user " +
                          user.getUsername());
            activator.sendActivationMessage(user, context);
            // XXX throws MessagingException
        }
    }
}
