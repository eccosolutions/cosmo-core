package org.osaf.cosmo.model.hibernate;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.xml.DomReader;
import org.osaf.cosmo.xml.DomWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA2.1 converters, used in preference to custom types such as the now-removed XmlClobType.
 * The converter will auto-apply and the field can just be marked as @Lob as normal.
 *
 * @since 11/10/15
 */
public class JpaConverters {
    @Converter
    public static class XmlConverter implements AttributeConverter<Node, String> {
        private static final Log log = LogFactory.getLog(XmlConverter.class);
        @Override
        public String convertToDatabaseColumn(Node attribute) {
            String xml = null;
            if (attribute != null) {
                try {
                    xml = DomWriter.write(attribute);
                } catch (Exception e) {
                    log.error("Error serializing XML clob", e);
                    throw new HibernateException("Error serializing XML clob: " + e.getMessage());
                }
            }
            return xml;
        }

        @Override
        public Node convertToEntityAttribute(String dbData) {
            try {
                return DomReader.read(dbData);
            } catch (Exception e) {
                log.error("Error deserializing XML clob '" + dbData + "'", e);
                return null;
            }
        }
    }
}
