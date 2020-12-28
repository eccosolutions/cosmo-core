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
package org.osaf.cosmo.acegisecurity.ui.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

/**
 * Custom authentication filter to allow users to set a preferred
 * login redirect url and to return the redirect url in the message
 * body instead of issueing an actual redirect to allow for dynamic
 * client side processing of authentication results.
 *
 * Note: this class overrides the values of
 * <code>AbstractProcessingFilter.defaultTargetUrl</code>
 * and
 * <code>AbstractProcessingFilter.alwaysUseDefaultTargetUrl</code>
 *
 * @author travis
 *
 */
public class CosmoAuthenticationProcessingFilter extends
        UsernamePasswordAuthenticationFilter {

    private Boolean alwaysUseUserPreferredUrl = false;
    private String cosmoDefaultLoginUrl;
    private String authenticationFailureUrl;

    public CosmoAuthenticationProcessingFilter() {
        super();
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.hasLength(authenticationFailureUrl, "authenticationFailureUrl must be specified");

        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler(){

            /* A place to put authentication so it will be available to
             * sendRedirect
             */
            private final ThreadLocal<Authentication> currentAuthentication = new ThreadLocal<Authentication>();

            /*
             * On successful authentication:
             * 1) First, try to find a url the user was attempting to visit
             * 2) If that does not exist, try to find the user's preferred login url
             * 3) If that does not exist, get the default redirect url for this filter
             *
             * Finally, return the url in the message body.
             */
            @Override
            protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
                String targetUrl = super.determineTargetUrl(request, response);

                if (targetUrl == null) {
                    targetUrl = getRelativeUrl(request, cosmoDefaultLoginUrl);
                }
                return targetUrl;
            }

            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                currentAuthentication.set(authentication);
                super.onAuthenticationSuccess(request, response, authentication);
                currentAuthentication.remove();
            }
        };
        // Ensure sendRedirect will always be called with url = true on successful auth.
        successHandler.setAlwaysUseDefaultTargetUrl(true);
        setAuthenticationSuccessHandler(successHandler);

        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler(authenticationFailureUrl);
        setAuthenticationFailureHandler(failureHandler);
    }


    private static String getRelativeUrl(HttpServletRequest request, String path){
        if (path != null){
            return request.getContextPath() + path;
        }
        else return null;
    }

    public Boolean getAlwaysUseUserPreferredUrl() {
        return alwaysUseUserPreferredUrl;
    }

    public void setAlwaysUseUserPreferredUrl(Boolean alwaysUseUserPreferredUrl) {
        this.alwaysUseUserPreferredUrl = alwaysUseUserPreferredUrl;
    }

    public String getCosmoDefaultLoginUrl() {
        return cosmoDefaultLoginUrl;
    }

    public void setCosmoDefaultLoginUrl(String cosmoDefaultTargetUrl) {
        this.cosmoDefaultLoginUrl = cosmoDefaultTargetUrl;
    }

    public void setAuthenticationFailureUrl(String authenticationFailureUrl) {
        this.authenticationFailureUrl = authenticationFailureUrl;
    }
}
