package shop.ourshopping.backend.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

// MyBatis 및 JPA를 실행하기 위한 Bean등록
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "shop.ourshopping.repository", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager_jpa")
public class DataSourceConfig {

	@Autowired
	private ApplicationContext applicationContext;

	private static final String DEFAULT_NAMING_STRATEGY = "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy";

	// properties에서 sql 정보를 가져와 HikariCP 설정
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public HikariConfig hikariConfig() {
		return new HikariConfig();
	}

	// DataSource 등록
	@Bean
	public DataSource dataSource() {
		return new HikariDataSource(hikariConfig());
	}

	// properties에서 mybatis 설정(카멜케이스) 가져옴
	@Bean
	@ConfigurationProperties(prefix = "mybatis.configuration")
	public org.apache.ibatis.session.Configuration mybatisConfg() {

		return new org.apache.ibatis.session.Configuration();
	}

	// SqlSession 생성
	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		// SQL 정보 가져오기
		factoryBean.setDataSource(dataSource());
		// Jar파일로 배포할 때 alias들을 찾지 못하는 문제 방지
		factoryBean.setVfs(SpringBootVFS.class);

		// mybatis 설정(카멜케이스)
		factoryBean.setConfiguration(mybatisConfg());

		// xml Mapper 등록
		factoryBean.setMapperLocations(applicationContext.getResources("classpath:/mappers/**/*Mapper.xml"));
		// mybatis class alias 등록
		factoryBean.setTypeAliasesPackage("shop.ourshopping.dto.mybatis");

		return factoryBean.getObject();
	}

	// SqlSessionTemplate은 필요한 시점에 세션을 닫고 커밋하거나 롤백하는 것을 포함한 세션의 생명주기를 관리함
	@Bean
	public SqlSessionTemplate sqlSession() throws Exception {

		return new SqlSessionTemplate(sqlSessionFactory());
	}

	// mybatis 트랜잭션 설정
	@Bean(name = "transactionManager_mybatis")
	public PlatformTransactionManager transactionManager() {

		return new DataSourceTransactionManager(dataSource());
	}

	// jpa 설정
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
		Map<String, String> propertiesHashMap = new HashMap<String, String>();
		// JPA Camel Case 설정
		propertiesHashMap.put("hibernate.physical_naming_strategy", DEFAULT_NAMING_STRATEGY);

		// SQL 정보 가져오기
		LocalContainerEntityManagerFactoryBean rep = builder.dataSource(dataSource())
				.packages("shop.ourshopping.entity").properties(propertiesHashMap).build();

		return rep;
	}

	// PlatformTransactionManager이 많으면 EntityManagerFactoryBuilder가
	// 생성되지 않으므로 @Primary를 사용하여 생성
	@Bean(name = "transactionManager_jpa")
    @Primary
	public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder builder) {

		return new JpaTransactionManager(entityManagerFactory(builder).getObject());
	}
	
	// 트랜잭션 동시 적용
    @Bean(name = "transactionManager")
	@Autowired
    public PlatformTransactionManager transactionManager(@Qualifier("transactionManager_mybatis") PlatformTransactionManager txManager1, @Qualifier("transactionManager_jpa") PlatformTransactionManager txManager2) {
    	
    	return new ChainedTransactionManager(txManager1, txManager2);
    }
}