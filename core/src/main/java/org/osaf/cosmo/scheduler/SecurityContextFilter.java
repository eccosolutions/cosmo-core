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
package org.osaf.cosmo.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.UserService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.security.context.SecurityContextHolder;

/**
 * Job Filter that initializes the current security context with the owner of of
 * the job. The owner of the job is assumed to be the user with the username
 * equal to the valude associated with the "username" key in the JobDetai map.
 * If no username is present this filter does nothing.
 */
public class SecurityContextFilter implements Filter {

    private CosmoSecurityManager securityManager;
    private UserService userService;

    private static final Log log = LogFactory
            .getLog(SecurityContextFilter.class);

    @Override
    public void doFilter(JobExecutionContext context, FilterChain chain)
            throws JobExecutionException {
        try {
            // initiate security context with user
            String username = context.getJobDetail().getJobDataMap().getString(
                    "username");

            if (username != null) {
                log
                        .debug("initializing security context for user: "
                                + username);

                User user = userService.getUser(username);
                if (user == null)
                    throw new JobExecutionException("no user found for user "
                            + username);

                securityManager.initiateSecurityContext(user);
            }

            chain.doFilter(context);
        } finally {
            log.debug("clearing security context");
            // clear current security context
            SecurityContextHolder.clearContext();
        }
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
