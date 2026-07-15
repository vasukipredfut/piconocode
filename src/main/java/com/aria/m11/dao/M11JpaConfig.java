package com.aria.m11.dao;

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
 * Dedicated EntityManagerFactory + JPA repository wiring for the M11
 * Integration Hub package. Mirrors the m0 page wiring so M11 entities live
 * in their own persistence unit while sharing the primary
 * {@code psqlDataSource} connection pool.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "m11EntityManagerFactory",
        transactionManagerRef = "m11TransactionManager",
        basePackages = "com.aria.m11.dao")
public class M11JpaConfig {

    @Bean(name = "m11EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean m11EntityManagerFactory(
            @Qualifier("psqlDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource);
        JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(adapter);
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        props.put("hibernate.show_sql", false);
        bean.setJpaPropertyMap(props);
        bean.setPersistenceUnitName("m11");
        bean.setPackagesToScan("com.aria.m11.model");
        return bean;
    }

    @Bean(name = "m11TransactionManager")
    public PlatformTransactionManager m11TransactionManager(
            @Qualifier("m11EntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
