package org.osaf.cosmo.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.ClobProxy;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractJavaType;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.osaf.cosmo.xml.DomReader;
import org.osaf.cosmo.xml.DomWriter;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * The dirty properties logger showed that HibXmlAttribute has an Element (as in w3 Node) value which is being changed. I then found this:
 *   "HHH000481: Encountered Java type for which we could not locate a JavaTypeDescriptor and which does not appear to implement equals and/or hashCode. This can lead to significant performance problems when performing equality/dirty checking involving this Java type. Consider registering a custom JavaTypeDescriptor or at least implementing equals/hashCode."
 * This also appears on our logs. So, basically the interface Element on HibXmlAttribute can't be interrogated easily so its always dirty.
 * It might be okay, but the default equals hibernate applies on Element always returns false (Element doesn't have Serializable, equals or hashCode).
 * NB For dirty checking use the log entry. The class listed does the calculation - DefaultFlushEntityEventListener (breakpoint on isAssignable to object)
 *
 * We reinstated a @TypeDef (as per 4e282bb7) just to override the equals.
 * The suggested approaches are here: https://stackoverflow.com/a/41813127
 *  We could register (using service-provider - see https://stackoverflow.com/a/21518400)
 *  but we can avoid registering by using @TypeDef (exists in package-info)
 *  other items of interest: MaterializedClobType / ClobTypeDescriptor
 *  <p>>
 *
 *  See https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#basic-legacy
 */
public class ElementTypeDescriptor extends AbstractJavaType<Element> {

    private static final Log log = LogFactory.getLog(ElementTypeDescriptor.class);

    public static final ElementTypeDescriptor INSTANCE = new ElementTypeDescriptor();

    protected ElementTypeDescriptor() {
        super(Element.class, new ElementMutabilityPlan());
    }

    @Override
    public boolean areEqual(Element x, Element y) {
        if(x==null || y==null)
            return false;

        return x.isEqualNode(y);
    }

    @Override
    public String toString(Element value) {
        try {
            return value != null ? DomWriter.write(value) : null;
        } catch (Exception e) {
            log.error("Error serializing XML clob", e);
            throw new HibernateException("Error serializing XML clob: " + e.getMessage());
        }
    }

    // used when passing the value as a PreparedStatement bind parameter
    @SuppressWarnings("unchecked")
    @Override
    public <X> X unwrap(Element value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        } else if (Element.class.isAssignableFrom(type)) {
            return (X) value;
        } else if (Reader.class.isAssignableFrom(type)) {
            return (X) new StringReader(toString(value));
        } else if (CharacterStream.class.isAssignableFrom(type)) {
            return (X) new CharacterStreamImpl(toString(value));
        // Since NClob extends Clob, we need to check if type is an NClob
        // before checking if type is a Clob. That will ensure that
        // the correct type is returned.
        } else if ( DataHelper.isNClob( type ) ) {
            return (X) options.getLobCreator().createNClob(toString(value));
        } else if (Clob.class.isAssignableFrom(type)) {
            return (X) ClobProxy.generateProxy(toString(value));
        } else if (String.class.isAssignableFrom(type)) {
            return (X) toString(value);
        }
        throw unknownUnwrap(type);
    }

    // used to transform the JDBC column value object to the actual mapping object type
    @Override
    public <X> Element wrap(X value, WrapperOptions options) {
        try {
            if (value == null) {
                return null;
            } else if (Element.class.isAssignableFrom(value.getClass())) {
                return (Element) value;
            } else if (Clob.class.isAssignableFrom(value.getClass())) {
                try (Reader characterStream = ((Clob) value).getCharacterStream()) {
                    return (Element) DomReader.read(characterStream);
                }
            } else if (Reader.class.isAssignableFrom(value.getClass())) {
                try {
                    return (Element) DomReader.read((Reader) value);
                } finally {
                    ((Reader) value).close();
                }
            } else if (String.class.isAssignableFrom(value.getClass())) {
                return (Element) DomReader.read((String) value);
            }
        } catch (SQLException e) {
            throw new HibernateException("Unable to access clob stream", e);
        } catch (IOException | ParserConfigurationException | XMLStreamException ioe) {
            throw new HibernateException("cannot read icalendar stream", ioe);
        }
        throw unknownWrap(value.getClass());
    }

    protected static class ElementMutabilityPlan extends MutableMutabilityPlan<Element> {
        @Override
        protected Element deepCopyNotNull(Element value) {
            if (value == null)
                return null;
            return (Element) value.cloneNode(true);
        }
    }
}
