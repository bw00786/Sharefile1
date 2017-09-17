package com.sharefile.sdk.controller.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sharefile.sdk.dao.*;
import com.sharefile.sdk.exception.ShareFileException;
import com.sharefile.sdk.services.UserService;
import com.sharefile.sdk.services.UserServiceImpl;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.PropertyResourceBundle;

@org.springframework.context.annotation.Configuration
@ComponentScan({"com.sharefile.sdk.main",
        "com.sharefile.sdk.model",
        "com.sharefile.sdk.dao",
        "com.sharefile.sdk.services",
        "com.sharefile.sdk.util",
        "com.sharefile.sdk.exception",
        "com.sharefileapps.apps.test"
})

@EnableTransactionManagement

public class HibernateConfig {

    private static final Logger log = LoggerFactory.getLogger(HibernateConfig.class);
    private static PropertyResourceBundle propBundle;



    public HibernateConfig() throws ShareFileException {

    }

    @Bean
    public ParticipantValue participantValue() { return new ParticipantValueImpl();}

    @Bean
    public ProviderName providerName() {return new ProviderNameImpl();}

    @Bean
    public CCNValue ccnValue() {return new CCNValueImpl();}

    @Bean
    public UserService userService() {return new UserServiceImpl();}

    @Bean
    public  SessionFactory sessionFactory() throws PropertyVetoException, ShareFileException {
        log.info("Creating session factory bean");
        log.info("CONFIG_DIR {}", System.getProperty("CONFIG_DIR"));

        try (InputStream propStrStream = new FileInputStream(System.getProperty("CONFIG_DIR") + "/app.properties")) {

            propBundle = new PropertyResourceBundle(propStrStream);


            LocalSessionFactoryBuilder builder =
                    new LocalSessionFactoryBuilder(atlasDataSource());
            builder.scanPackages("com.sharefile.sdk.model")
                    .addProperties(hibernateProperties());
            log.info("About to return to calling method");


            return builder.buildSessionFactory();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ShareFileException(e.getMessage());
        }
    }


    private static Properties hibernateProperties() {
        Properties prop = new Properties();

        prop.setProperty("hibernate.dialect", propBundle.getString("hibernate.orm.atlas.dialect"));
        prop.setProperty("hibernate.show_sql", propBundle.getString("hibernate.orm.atlas.show_sql"));
        prop.setProperty("hibernate.format_sql", propBundle.getString("hibernate.orm.atlas.format_sql"));
        prop.setProperty("hibernate.generate_statistics", propBundle.getString("hibernate.orm.atlas.generate_statistics"));
        prop.setProperty("hibernate.connection.driver_class", propBundle.getString("hibernate.orm.atlas.driver"));
        prop.setProperty("hibernate.c3p0.idle_test_period", propBundle.getString("hibernate.orm.atlas.c3p0.idle_test_period"));

        return prop;
    }


    @Bean(name = "dataSource")
    public static  DataSource atlasDataSource() throws PropertyVetoException {
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass(propBundle.getString("hibernate.orm.atlas.driver"));
        comboPooledDataSource.setJdbcUrl(propBundle.getString("hibernate.orm.atlas.url"));
        comboPooledDataSource.setUser(propBundle.getString("hibernate.orm.atlas.username"));
        comboPooledDataSource.setPassword(propBundle.getString("hibernate.orm.atlas.password"));
        comboPooledDataSource.setMinPoolSize(Integer.parseInt(propBundle.getString("hibernate.orm.atlas.c3p0.min_size")));
        comboPooledDataSource.setMaxPoolSize(Integer.parseInt(propBundle.getString("hibernate.orm.atlas.c3p0.max_size")));
        comboPooledDataSource.setCheckoutTimeout(Integer.parseInt(propBundle.getString("hibernate.orm.atlas.c3p0.checkout_timeout")));
        comboPooledDataSource.setTestConnectionOnCheckout(Boolean.parseBoolean(propBundle.getString("hibernate.orm.atlas.c3p0.test_connection_on_checkout")));
        comboPooledDataSource.setTestConnectionOnCheckin(Boolean.parseBoolean(propBundle.getString("hibernate.orm.atlas.c3p0.test_connection_on_checkin")));
        comboPooledDataSource.setPreferredTestQuery(propBundle.getString("hibernate.orm.atlas.c3p0.preferredTestQuery"));
        comboPooledDataSource.setIdleConnectionTestPeriod(Integer.parseInt(propBundle.getString("hibernate.orm.atlas.c3p0.idle_test_period")));
        comboPooledDataSource.setAutoCommitOnClose(Boolean.parseBoolean(propBundle.getString("hibernate.orm.atlas.c3p0.auto_commit_on_close")));
        comboPooledDataSource.setDataSourceName(propBundle.getString("hibernate.orm.atlas.c3p0.name"));
        comboPooledDataSource.setDebugUnreturnedConnectionStackTraces(Boolean.parseBoolean(propBundle.getString("hibernate.orm.atlas.c3p0.debug_unreturned_connection_stack_traces")));
        return comboPooledDataSource;
    }

    //Create a transaction manager
    @Bean
    public HibernateTransactionManager txManager() throws PropertyVetoException, ShareFileException {
        return new HibernateTransactionManager(sessionFactory());


    }

}
