package com.sagar.hibernate.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = "com.sagar.hibernate")
@EnableWebMvc
@EnableTransactionManagement
public class AppConfig implements WebMvcConfigurer {
    @Bean
    public DataSource dataSource() {

        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(
                "jdbc:mysql://localhost:3306/orm_mapping"
        );


        dataSource.setUsername("root");
        dataSource.setPassword("sagarhd");

        dataSource.setDriverClassName(
                "com.mysql.cj.jdbc.Driver"
        );


        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);

        return dataSource;
    }



    //this to tell spring jpa we are using hibernate vendor

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {

        HibernateJpaVendorAdapter adapter =
                new HibernateJpaVendorAdapter();

        adapter.setShowSql(true);
        adapter.setGenerateDdl(true);

        return adapter;
    }


    @Bean
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(
                entityManagerFactory
        );
    }


    //entity manager factory config
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            JpaVendorAdapter jpaVendorAdapter) {

        LocalContainerEntityManagerFactoryBean emf =
                new LocalContainerEntityManagerFactoryBean();

        // DataSource used by Hibernate
        emf.setDataSource(dataSource);

        // Hibernate is our JPA provider
        emf.setJpaVendorAdapter(jpaVendorAdapter);

        // Scan for @Entity classes
        emf.setPackagesToScan(
                "com.sagar.hibernate.entity"
        );

        // Hibernate-specific properties
        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                "hibernate.hbm2ddl.auto",
                "update"
        );

        properties.put(
                "hibernate.show_sql",
                "true"
        );

        properties.put(
                "hibernate.format_sql",
                "true"
        );

        properties.put(
                "hibernate.highlight_sql",
                "true"
        );

        emf.setJpaPropertyMap(properties);

        return emf;
    }



}
