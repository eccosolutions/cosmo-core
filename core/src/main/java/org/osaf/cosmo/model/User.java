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
package org.osaf.cosmo.model;

import java.util.Set;

/**
 * Represents a user in the cosmo server.
 */
public interface User extends AuditableObject{

    /**
     */
    String USERNAME_OVERLORD = "root";

    // Sort Strings
    /**
     * A String indicating the results should be sorted by Last Name then First Name
     */
    String NAME_SORT_STRING = "Name";
    /**
     * A String indicating the results should be sorted by Username
     */
    String USERNAME_SORT_STRING = "Username";
    /**
     * A String indicating the results should be sorted by Administrator
     */
    String ADMIN_SORT_STRING = "Administrator";
    /**
     * A String indicating the results should be sorted by Email
     */
    String EMAIL_SORT_STRING = "Email";
    /**
     * A String indicating the results should be sorted by Date Created
     */
    String CREATED_SORT_STRING = "Created";
    /**
     * A String indicating the results should be sorted by Date last Modified
     */
    String LAST_MODIFIED_SORT_STRING = "Last Modified";
    /**
     * A String indicating the results should be sorted by Activated status
     */
    String ACTIVATED_SORT_STRING = "Activated";
    /**
     * A String indicating the results should be sorted by Locked status
     */
    String LOCKED_SORT_STRING = "Locked";


    /**
     * The Default Sort Type
     */
    String DEFAULT_SORT_STRING = NAME_SORT_STRING;

    String NAME_URL_STRING = "name";
    String USERNAME_URL_STRING = "username";
    String ADMIN_URL_STRING = "admin";
    String EMAIL_URL_STRING = "email";
    String CREATED_URL_STRING = "created";
    String LAST_MODIFIED_URL_STRING = "modified";
    String ACTIVATED_URL_STRING = "activated";
    String LOCKED_URL_STRING = "locked";

    /**
     */
    int PASSWORD_LEN_MIN = 5;
    /**
     */
    int PASSWORD_LEN_MAX = 16;

    /*
     * I'm not sure about putting this enum here, but it seems weird in other
     * places too. Since sort information is already here, in the *_SORT_STRING
     * constants, I think this is appropriate.
     */
    enum SortType {
        NAME (NAME_URL_STRING, NAME_SORT_STRING),
        USERNAME (USERNAME_URL_STRING, USERNAME_SORT_STRING),
        ADMIN (ADMIN_URL_STRING, ADMIN_SORT_STRING),
        EMAIL (EMAIL_URL_STRING, EMAIL_SORT_STRING),
        CREATED (CREATED_URL_STRING, CREATED_SORT_STRING),
        LAST_MODIFIED (LAST_MODIFIED_URL_STRING, LAST_MODIFIED_SORT_STRING),
        ACTIVATED (ACTIVATED_URL_STRING, ACTIVATED_SORT_STRING),
        LOCKED (LOCKED_URL_STRING, LOCKED_SORT_STRING);

        private final String urlString;
        private final String titleString;

        SortType(String urlString, String titleString){
            this.urlString = urlString;
            this.titleString = titleString;
        }

        public String getTitleString() {
            return titleString;
        }

        public String getUrlString() {
            return urlString;
        }

        public static SortType getByUrlString(String string) {
            if (string.equals(NAME_URL_STRING)){
                return NAME;
            } else if (string.equals(USERNAME_URL_STRING)){
                return USERNAME;
            } else if (string.equals(ADMIN_URL_STRING)){
                return ADMIN;
            } else if (string.equals(EMAIL_URL_STRING)){
                return EMAIL;
            } else if (string.equals(CREATED_URL_STRING)){
                return CREATED;
            } else if (string.equals(LAST_MODIFIED_URL_STRING)){
                return LAST_MODIFIED;
            } else if (string.equals(ACTIVATED_URL_STRING)){
                return ACTIVATED;
            } else if (string.equals(LOCKED_URL_STRING)){
                return LOCKED;
            } else {
                return null;
            }
        }
    }

    /**
     */
    String getUid();

    /**
     * @param uid
     */
    void setUid(String uid);

    /**
     */
    String getUsername();

    /**
     */
    void setUsername(String username);

    /**
     */
    String getOldUsername();

    /**
     */
    boolean isUsernameChanged();

    /**
     */
    String getPassword();

    /**
     */
    void setPassword(String password);

    /**
     */
    String getFirstName();

    /**
     */
    void setFirstName(String firstName);

    /**
     */
    String getLastName();

    /**
     */
    void setLastName(String lastName);

    /**
     */
    String getEmail();

    /**
     */
    void setEmail(String email);

    /**
     */
    String getOldEmail();

    /**
     */
    boolean isEmailChanged();

    /**
     */
    Boolean getAdmin();

    Boolean getOldAdmin();

    /**
     */
    boolean isAdminChanged();

    /**
     */
    void setAdmin(Boolean admin);

    /**
     */
    String getActivationId();

    /**
     */
    void setActivationId(String activationId);

    /**
     */
    boolean isOverlord();

    /**
     */
    boolean isActivated();

    /**
     *
     *
     */
    void activate();

    Boolean isLocked();

    void setLocked(Boolean locked);

    /**
     */
    void validateRawPassword();

    Set<CollectionSubscription> getCollectionSubscriptions();

    void addSubscription(CollectionSubscription subscription);

    /**
     * Get the CollectionSubscription with the specified displayName
     * @param displayname display name of subscription to return
     * @return subscription with specified display name
     */
    CollectionSubscription getSubscription(String displayname);

    /**
     * Remove the CollectionSubscription with the specifed displayName
     * @param displayName display name of the subscription to remove
     */
    void removeSubscription(String displayName);

    /** */
    void removeSubscription(CollectionSubscription sub);

    /**
     * Return true if this user is subscribed to <code>collection</code>
     */
    boolean isSubscribedTo(CollectionItem collection);

    String calculateEntityTag();

}