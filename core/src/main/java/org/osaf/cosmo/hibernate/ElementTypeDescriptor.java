package org.osaf.cosmo.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.ClobProxy;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.DataHelper;
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

// Good breakpoint is JavaTypeDescriptorRegistry and find: log.unknownJavaTypeNoEqualsHashCode( javaType );
// ?? MaterializedClobType
// see ClobTypeDescriptor - extend that?
// can avoid registration by using service-provider - see https://stackoverflow.com/a/21518400
// by using package-info: JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(new ElementTypeDescriptor());
// NB dirty checking - go from the log!! the class listed does the calculation - DefaultFlushEntityEventListener (and possibly breakpoint on isAssisnable to object)
public class ElementTypeDescriptor extends AbstractTypeDescriptor<Element> {

    private static final Log log = LogFactory.getLog(ElementTypeDescriptor.class);

    public static final ElementTypeDescriptor INSTANCE = new ElementTypeDescriptor();

    protected ElementTypeDescriptor() {
        super(Element.class, new ElementType.ElementMutabilityPlan());
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

    @Override
    public Element fromString(String string) {
        try {
            return (Element) DomReader.read(string);
        } catch (Exception e) {
            log.error("Error deserializing XML clob '" + string + "'", e);
            return null;
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

}
