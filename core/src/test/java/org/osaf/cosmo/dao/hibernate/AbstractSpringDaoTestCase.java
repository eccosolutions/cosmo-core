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
package org.osaf.cosmo.dao.hibernate;

import org.osaf.cosmo.CosmoConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@EnableAutoConfiguration
@EnableTransactionManagement(proxyTargetClass = true)
@Rollback
public abstract class AbstractSpringDaoTestCase extends AbstractTransactionalJUnit4SpringContextTests {


    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(CosmoConfig.class)
    @ComponentScan("org.osaf.cosmo")
    @EntityScan(basePackages = "org.osaf.cosmo.model.hibernate") // BUT in parent we use persistence.xml which isn't getting detected for some reason
    public static class LibTestConfiguration {
    }
}
