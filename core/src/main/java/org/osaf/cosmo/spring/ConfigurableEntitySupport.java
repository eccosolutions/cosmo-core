package org.osaf.cosmo.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.stereotype.Component;

/**
 * Provides a static method to configure @Configurable beans which were not loaded by the Spring context.
 * This is used to configure entities by dependency injection even though the entity was loaded outside of the context.
 *
 * @since 30/03/15
 */
@Component
public class ConfigurableEntitySupport implements BeanFactoryAware{
    private static final BeanConfigurerSupport beanConfigurerSupport = new BeanConfigurerSupport();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        beanConfigurerSupport.setBeanFactory(beanFactory);
        beanConfigurerSupport.setBeanWiringInfoResolver(new AnnotationBeanWiringInfoResolver());
    }

    public static void configureBean(Object bean) {
        beanConfigurerSupport.configureBean(bean);
    }
}
