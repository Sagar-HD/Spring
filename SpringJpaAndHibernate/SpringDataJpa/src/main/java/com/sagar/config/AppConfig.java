package com.sagar.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.Value;
import org.hibernate.boot.model.relational.Database;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "com.sagar")
@EnableWebMvc
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.sagar.repository")
@PropertySource("classpath:application.properties")
public class AppConfig implements WebMvcConfigurer {

    @Bean
    public DataSource dataSource(Environment env) {
        HikariConfig config=new HikariConfig();
        config.setUsername(env.getProperty("datasource.username"));
        config.setJdbcUrl(env.getProperty("datasource.url"));
        config.setPassword(env.getProperty("datasource.password"));


        return new HikariDataSource(config);

    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,Environment env){
        LocalContainerEntityManagerFactoryBean emf=new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.sagar.entity");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties properties = new Properties();

        properties.setProperty(
                "hibernate.hbm2ddl.auto",
                env.getProperty("hibernate.hbm2ddl.auto")
        );

        properties.setProperty(
                "hibernate.show_sql",
                env.getProperty("hibernate.show_sql")
        );

        properties.setProperty(
                "hibernate.format_sql",
                env.getProperty("hibernate.format_sql")
        );
        emf.setJpaProperties(properties);
        return emf;


    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf){
        return new JpaTransactionManager(emf);
    }
}
