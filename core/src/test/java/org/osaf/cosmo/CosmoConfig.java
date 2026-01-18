package org.osaf.cosmo;

import javax.sql.DataSource;
import org.osaf.cosmo.hibernate.CompoundInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class CosmoConfig extends BaseCosmoConfig {

    public CosmoConfig() {
        super(CompoundInterceptor::registerInterceptor);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
//        em.setPackagesToScan("org.osaf.cosmo.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        // Explicitly set the interface to the Jakarta JPA standard to avoid the SessionFactory conflict
        em.setEntityManagerFactoryInterface(jakarta.persistence.EntityManagerFactory.class);
        em.setEntityManagerInterface(jakarta.persistence.EntityManager.class);

        return em;
    }
}
