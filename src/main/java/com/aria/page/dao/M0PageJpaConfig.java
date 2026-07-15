package com.aria.page.dao;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated EntityManagerFactory + JPA repository wiring for the M0 page
 * substrate.
 *
 * <p>{@code PsqlConfig} (legacy) limits its {@code psqlEntityManagerFactory}
 * to {@code com.aria.model} via {@code setPackagesToScan}. Editing that
 * historical configuration is off-limits per Round 1F path ownership, so
 * the m0 entities get their own EMF — pointed at the same primary
 * {@code psqlDataSource}, same dialect, same Hibernate config — so they
 * share the underlying connection pool and behave transactionally like
 * the rest of the app.
 *
 * <p>Both EMFs read/write the same MariaDB schema; we just don't share
 * a single persistence unit (they each scan their own package).
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "m0PageEntityManagerFactory",
        transactionManagerRef = "m0PageTransactionManager",
        basePackages = {
            "com.aria.page.dao",
            "com.aria.workflow.dao",
            // Round 1G — Schema V2 substrate packages
            "com.aria.savedview.dao",
            "com.aria.entity_registry.dao",
            "com.aria.action.dao",
            "com.aria.datasource.dao",
            // Round 2 — M10 gate-pass + tenant-config packages
            "com.aria.dispatch.dao",
            "com.aria.tenant.dao"
        })
public class M0PageJpaConfig {

    @Bean(name = "m0PageEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean m0PageEntityManagerFactory(
            @Qualifier("psqlDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource);
        JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(adapter);
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.jdbc.lob.non_contextual_creation", true);
        bean.setJpaPropertyMap(props);
        bean.setPersistenceUnitName("m0Page");
        bean.setPackagesToScan(
                "com.aria.page.model",
                "com.aria.workflow.model",
                "com.aria.savedview.model",
                "com.aria.entity_registry.model",
                "com.aria.action.model",
                "com.aria.datasource.model",
                "com.aria.dispatch.model",
                "com.aria.tenant.model");
        return bean;
    }

    @Bean(name = "m0PageTransactionManager")
    public PlatformTransactionManager m0PageTransactionManager(
            @Qualifier("m0PageEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
