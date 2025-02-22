/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.model.hibernate;

import java.io.Serial;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

/**
 * Hibernate persistent User.
 */
@Entity
@Table(name="cosmo_users",
indexes = {
        @Index(name="idx_useruid", columnList="user_uid"),
        @Index(name="idx_username", columnList="username"),
        @Index(name="idx_useremail", columnList="email"),
        @Index(name="idx_activationid", columnList="activationid")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibUser extends HibAuditableObject implements User {

    @Serial
    private static final long serialVersionUID = -5401963358519490736L;

    /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    //public static final int USERNAME_LEN_MAX = 32;
    public static final int USERNAME_LEN_MAX = 50;

    /**
     */
    public static final int FIRSTNAME_LEN_MIN = 1;
    /**
     */
    public static final int FIRSTNAME_LEN_MAX = 128;
    /**
     */
    public static final int LASTNAME_LEN_MIN = 1;
    /**
     */
    public static final int LASTNAME_LEN_MAX = 128;
    /**
     */
    public static final int EMAIL_LEN_MIN = 1;
    /**
     */
    public static final int EMAIL_LEN_MAX = 128;

    @Column(name = "user_uid", nullable=false, unique=true, length=255)
    @NotNull
    @Length(min=1, max=255)
    private String uid;

    @Column(name = "username", nullable=false)
    @NotNull
    @NaturalId
    @Length(min=USERNAME_LEN_MIN, max=USERNAME_LEN_MAX)
    //per bug 11599:
    // Usernames must be between 3 and 32 characters; may contain any Unicode
    //character in the following range of unicode code points: [#x20-#xD7FF] |
    //[#xE000-#xFFFD] EXCEPT #x7F or #x3A
    // Oh and don't allow ';' or '/' because there are problems with encoding
    // them in urls (tomcat doesn't support it)
    @Pattern(regexp="^[\\u0020-\\ud7ff\\ue000-\\ufffd&&[^\\u007f\\u003a;/\\\\]]+$")
    private String username;

    private transient String oldUsername;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "firstname")
    @Length(min=FIRSTNAME_LEN_MIN, max=FIRSTNAME_LEN_MAX)
    private String firstName;

    @Column(name = "lastname")
    @Length(min=LASTNAME_LEN_MIN, max=LASTNAME_LEN_MAX)
    private String lastName;

    @Column(name = "email", nullable=false, unique=true)
    @NotNull
    @Length(min=EMAIL_LEN_MIN, max=EMAIL_LEN_MAX)
    @Email
    private String email;

    private transient String oldEmail;

    @Column(name = "activationid", nullable=true, length=255)
    @Length(min=1, max=255)
    private String activationId;

    @Column(name = "admin")
    private Boolean admin;

    private transient Boolean oldAdmin;

    @Column(name = "locked")
    private Boolean locked;

    @OneToMany(targetEntity=HibCollectionSubscription.class, mappedBy = "owner", fetch= FetchType.LAZY)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private final Set<CollectionSubscription> subscriptions =
        new HashSet<>(0);

    /**
     */
    public HibUser() {
        admin = Boolean.FALSE;
        locked = Boolean.FALSE;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getUid()
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setUid(java.lang.String)
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getUsername()
     */
    public String getUsername() {
        return username;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setUsername(java.lang.String)
     */
    public void setUsername(String username) {
        oldUsername = this.username;
        this.username = username;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getOldUsername()
     */
    public String getOldUsername() {
        return oldUsername != null ? oldUsername : username;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#isUsernameChanged()
     */
    public boolean isUsernameChanged() {
        return oldUsername != null && ! oldUsername.equals(username);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getFirstName()
     */
    public String getFirstName() {
        return firstName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setFirstName(java.lang.String)
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getLastName()
     */
    public String getLastName() {
        return lastName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setLastName(java.lang.String)
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getEmail()
     */
    public String getEmail() {
        return email;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setEmail(java.lang.String)
     */
    public void setEmail(String email) {
        oldEmail = this.email;
        this.email = email;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getOldEmail()
     */
    public String getOldEmail() {
        return oldEmail;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#isEmailChanged()
     */
    public boolean isEmailChanged() {
        return oldEmail != null && ! oldEmail.equals(email);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getAdmin()
     */
    public Boolean getAdmin() {
        return admin;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getOldAdmin()
     */
    public Boolean getOldAdmin() {
        return oldAdmin;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#isAdminChanged()
     */
    public boolean isAdminChanged() {
        return oldAdmin != null && ! oldAdmin.equals(admin);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setAdmin(java.lang.Boolean)
     */
    public void setAdmin(Boolean admin) {
        oldAdmin = this.admin;
        this.admin = admin;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getActivationId()
     */
    public String getActivationId() {
        return activationId;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setActivationId(java.lang.String)
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#isOverlord()
     */
    public boolean isOverlord() {
        return username != null && username.equals(USERNAME_OVERLORD);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#isActivated()
     */
    public boolean isActivated() {
        return this.activationId == null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#activate()
     */
    public void activate(){
       this.activationId = null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#isLocked()
     */
    public Boolean isLocked() {
        return locked;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#setLocked(java.lang.Boolean)
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    /**
     * Username determines equality
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || username == null)
            return false;
        if (! (obj instanceof User))
            return false;

        return username.equals(((User) obj).getUsername());
    }

    @Override
        public int hashCode() {
        if (username == null)
            return super.hashCode();
        else
            return username.hashCode();
    }

    /**
     */
    @Override
	public String toString() {
        return new ToStringBuilder(this).
            append("username", username).
            append("password", "xxxxxx").
            append("firstName", firstName).
            append("lastName", lastName).
            append("email", email).
            append("admin", admin).
            append("activationId", activationId).
            append("locked", locked).
            toString();
    }


    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#validateRawPassword()
     */
    public void validateRawPassword() {
        if (password == null) {
            throw new ModelValidationException(this, "Password not specified");
        }
        if (password.length() < PASSWORD_LEN_MIN) {// ||
            //password.length() > PASSWORD_LEN_MAX) {
            throw new ModelValidationException(this, "Password must be " +
                                               PASSWORD_LEN_MIN + //" to " +
                                               //PASSWORD_LEN_MAX +
                                               " characters in length");
        }
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getCollectionSubscriptions()
     */
    public Set<CollectionSubscription> getCollectionSubscriptions() {
        return subscriptions;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#addSubscription(org.osaf.cosmo.model.CollectionSubscription)
     */
    public void addSubscription(CollectionSubscription subscription) {
        subscription.setOwner(this);
        subscriptions.add(subscription);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#getSubscription(java.lang.String)
     */
    public CollectionSubscription getSubscription(String displayname) {

        for (CollectionSubscription sub : subscriptions) {
            if (sub.getDisplayName().equals(displayname))
                return sub;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#removeSubscription(java.lang.String)
     */
    public void removeSubscription(String displayName) {
        removeSubscription(getSubscription(displayName));
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#removeSubscription(org.osaf.cosmo.model.CollectionSubscription)
     */
    public void removeSubscription(CollectionSubscription sub) {
        if (sub != null)
            subscriptions.remove(sub);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.User#isSubscribedTo(org.osaf.cosmo.model.CollectionItem)
     */
    public boolean isSubscribedTo(CollectionItem collection){
        for (CollectionSubscription sub : subscriptions){
            if (collection.getUid().equals(sub.getCollectionUid())) return true;
        }
        return false;
    }
}
