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
package org.osaf.cosmo.hibernate;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.VersionType;
import org.hibernate.type.descriptor.java.JdbcTimestampTypeDescriptor;
import org.hibernate.type.descriptor.sql.BigIntTypeDescriptor;

/**
 * Custom Hibernate type that maps a java.util.Date
 * to a SQL BIGINT column, storing the number of 
 * milliseconds that have passed since Jan 1, 1970 GMT.
 *
 */
public class LongTimestampType extends AbstractSingleColumnStandardBasicType<Date> implements LiteralType<Date>, VersionType<Date> {

	// copy of 3.6 TimestampType but with cosmo methods overridden (see bottom of class)
	// cosmo wants a sql BIGINT and a java Date mapping (was extending http://fisheye.jboss.org/browse/Hibernate/core/tags/hibernate-3.3.2.GA/core/src/main/java/org/hibernate/type/TimestampType.java?hb=true)

	// cosmo's get and set now translate to nullSafeGet/Set- of AbstractStandardBasicType
	// the get returns sqlTypeDescriptor.getExtractor( javaTypeDescriptor ).extract( rs, name, options );
	// where sqlTypeDescriptor is the first argument of the constructor
	// so our 'get' calls BigIntTypeDescriptor.getExtractor which returns X(=Date) javaTypeDescriptor.wrap( rs.getLong( name ), options );
	// so our 'get' calls JdbcTimestampTypeDescriptor.wrap(long) - which exists
	// the set calls sqlTypeDescriptor.getBinder( javaTypeDescriptor ).bind( st, (T) value, index, options ); (where T is Date)
	// so our 'set' calls BigIntTypeDescriptor.getBinder which calls st.setLong( index, javaTypeDescriptor.unwrap( value, Long.class, options ) );
	// so our 'set' calls JdbcTimestampTypeDescriptor.unwrap(date, long.class) - which exists

	public static final LongTimestampType INSTANCE = new LongTimestampType();

	public LongTimestampType() {
		// sqlTypeDescriptor, javaTypeDescriptor
		super( BigIntTypeDescriptor.INSTANCE, JdbcTimestampTypeDescriptor.INSTANCE );
	}

	public String getName() {
		return "long_timestamp";
	}

	// the generic method of the super super class - unsure why this is overridden in TimestampType [perhaps to use Date, and not Timestamp]
	public Date fromStringValue(String xml) throws HibernateException {
		return fromString( xml );
	}

	// only register our specific case
	@Override
	public String[] getRegistrationKeys() {
		return new String[] { getName() };
	}

	// implement VersionType
	public Date next(Date current, SessionImplementor session) {
		return seed( session );
	}
	public Date seed(SessionImplementor session) {
		return new Timestamp( System.currentTimeMillis() );
	}
	public Comparator<Date> getComparator() {
		return getJavaTypeDescriptor().getComparator();
	}

	// implement LiteralType
	public String objectToSQLString(Date value, Dialect dialect) throws Exception {
		// seems the below could do the trick, but we stick to what cosmo had...
		return "" + value.getTime();
		/*
		final Timestamp ts = Timestamp.class.isInstance( value )
				? ( Timestamp ) value
				: new Timestamp( value.getTime() );
		// TODO : use JDBC date literal escape syntax? -> {d 'date-string'} in yyyy-mm-dd hh:mm:ss[.f...] format
		return StringType.INSTANCE.objectToSQLString( ts.toString(), dialect );
		*/
	}
}
