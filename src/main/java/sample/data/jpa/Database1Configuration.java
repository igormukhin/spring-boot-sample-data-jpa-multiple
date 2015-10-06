package sample.data.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by igor.mukhin on 05.10.2015.
 */
@Configuration
@EnableJpaRepositories(basePackages="sample.data.jpa.repo1", entityManagerFactoryRef="emf1", transactionManagerRef="tm1")
public class Database1Configuration {

    private EmbeddedDatabase database;

    private String dbname = "testdb1";

    private JpaProperties jpaProperties = new JpaProperties();

    @Autowired(required = false)
    private PersistenceUnitManager persistenceUnitManager;

    @Bean(name="dataSource1")
    // IMPORTANT: http://docs.spring.io/spring-boot/docs/1.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#howto-two-datasources
    @Primary
    public DataSource dataSource1() {
        this.database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseConnection.HSQL.getType()).setName(dbname).build();
        return this.database;
    }

    @PreDestroy
    public void close() {
        if (this.database != null) {
            this.database.shutdown();
        }
    }

    @Bean(name="emfb1")
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder1() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(this.jpaProperties.isShowSql());
        adapter.setDatabase(this.jpaProperties.getDatabase());
        adapter.setDatabasePlatform(this.jpaProperties.getDatabasePlatform());
        adapter.setGenerateDdl(this.jpaProperties.isGenerateDdl());

        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(
                adapter, this.jpaProperties, this.persistenceUnitManager);
        //builder.setCallback(getVendorCallback());
        return builder;
    }

    @Bean(name="emf1")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory1(
            @Qualifier("dataSource1") DataSource dataSource,
            @Qualifier("emfb1") EntityManagerFactoryBuilder factoryBuilder) {
        Map<String, Object> vendorProperties = new HashMap<String, Object>();
        vendorProperties.put("hibernate.hbm2ddl.auto", "create-drop");
        vendorProperties.put("hibernate.hbm2ddl.import_files", "import1.sql");
        return factoryBuilder
                .dataSource(dataSource)
                .packages("sample.data.jpa.domain1")
                .properties(vendorProperties)
                .build();
    }

    @Bean(name="tm1")
    public PlatformTransactionManager transactionManager1(@Qualifier("emf1") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

}
