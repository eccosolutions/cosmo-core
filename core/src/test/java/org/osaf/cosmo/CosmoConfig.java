package org.osaf.cosmo;

import org.osaf.cosmo.hibernate.CompoundInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class CosmoConfig extends BaseCosmoConfig {

    public CosmoConfig() {
        super(CompoundInterceptor::registerInterceptor);
    }
}
