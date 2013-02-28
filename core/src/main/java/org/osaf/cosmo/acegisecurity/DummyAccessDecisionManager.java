package org.osaf.cosmo.acegisecurity;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;



public class DummyAccessDecisionManager implements AccessDecisionManager {


    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        // do nothing
    }

    /**
     * Always returns true, as this manager does not support any
     * config attributes.
     */
    @Override
    public boolean supports(ConfigAttribute attribute) { return true; }

    /**
     * Always return true;
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
