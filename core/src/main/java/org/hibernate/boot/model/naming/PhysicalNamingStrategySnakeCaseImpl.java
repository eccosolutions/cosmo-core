package org.hibernate.boot.model.naming;

import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * This is just to allow Spring Boot to initialise. You should set the following in application.yml:
 * <pre>
 *   spring:
 *     jpa:
 *         hibernate:
 *             naming:
 *                 physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
 * </pre>
 */
public class PhysicalNamingStrategySnakeCaseImpl implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier logicalName,
        JdbcEnvironment jdbcEnvironment) {
        return null;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier logicalName,
        JdbcEnvironment jdbcEnvironment) {
        return null;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return null;
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier logicalName,
        JdbcEnvironment jdbcEnvironment) {
        return null;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName,
        JdbcEnvironment jdbcEnvironment) {
        return null;
    }
}
