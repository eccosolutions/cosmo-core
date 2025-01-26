/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.service;

import java.util.Set;

import org.osaf.cosmo.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Interface for services that manage user accounts.
 */
public interface UserService extends Service {

    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    Set<User> getUsers();


    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    User getUser(String username);

    /**
     * Returns the user account identified by the given id.
     *
     * @param userId the id of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    User getUserById(long userId);

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    User getUserByEmail(String email);

    /**
     * Returns the user account associated with the given activation id.
     *
     * @param activationId the activation id associated with the account to return
     *
     * @return the User associated with activationId
     *
     * @throws DataRetrievalFailureException if there is no user associated with this
     * activation id.
     */
    User getUserByActivationId(String activationId);


    /**
     * Creates a user account in the repository. Digests the raw
     * password and uses the result to replace the raw
     * password. Returns a new instance of <code>User</code>
     * after saving the original one.
     *
     * @param user the account to create
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    User createUser(User user);

    /**
     * Creates a user account in the repository as per
     * {@link #createUser(User)}. Sends the {@link #EVENT_CREATE_USER}
     * event to each provided listener.
     *
     * @param user the account to create
     * @param listeners an array of listeners to notify
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    User createUser(User user,
                    ServiceListener[] listeners);

    /**
     * Updates a user account that exists in the repository. If the
     * password has been changed, digests the raw new password and
     * uses the result to replace the stored password. Returns a new
     * instance of <code>User</code>  after saving the original one.
     *
     * @param user the account to update
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    User updateUser(User user);

    /**
     * Removes a user account from the repository.
     *
     * @param user the account to remove
     */
    void removeUser(User user);

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    void removeUser(String username);

    /**
     * Removes a set of user accounts from the repository.
     * @param users
     * @throws OverlordDeletionException
     */
    void removeUsers(Set<User> users) throws OverlordDeletionException;

    /**
     * Removes the user accounts identified by the given usernames from
     * the repository.
     * @param usernames
     * @throws OverlordDeletionException
     */
    void removeUsersByName(Set<String> usernames) throws OverlordDeletionException;

    /**
     * Generates a random password in a format suitable for
     * presentation as an authentication credential.
     */
    String generatePassword();
}
