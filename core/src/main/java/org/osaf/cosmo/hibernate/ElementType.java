package org.osaf.cosmo.hibernate;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.w3c.dom.Element;

// we don't need the 'discriminator type' implements DiscriminatorType<Element>
public class ElementType extends AbstractSingleColumnStandardBasicType<Element> {

    public ElementType() {
        super(org.hibernate.type.descriptor.sql.ClobTypeDescriptor.DEFAULT, ElementTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "xml_clob";
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

    @Override
    protected MutabilityPlan<Element> getMutabilityPlan() {
        return new ElementType.ElementMutabilityPlan();
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
