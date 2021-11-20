package shop.ourshopping.backend.configuration;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

	@Autowired
	private ApplicationContext applicationContext;

	// properties가져오기(sql 설정)
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public HikariConfig hikariConfig() {
		return new HikariConfig();
	}

	@Bean
	public DataSource dataSource() {
		return new HikariDataSource(hikariConfig());
	}

	// properties가져오기(MyBatis Camel Case 설정)
	@Bean
	@ConfigurationProperties(prefix = "mybatis.configuration")
	public org.apache.ibatis.session.Configuration mybatisConfg() {

		return new org.apache.ibatis.session.Configuration();
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		// SQL 정보 가져오기
		factoryBean.setDataSource(dataSource());
		// Jar파일을 만들 때 찾지 못하는 문제 방지
		factoryBean.setVfs(SpringBootVFS.class);

		factoryBean.setConfiguration(mybatisConfg());

		// Mapper 등록
		factoryBean.setMapperLocations(applicationContext.getResources("classpath:/mappers/**/*Mapper.xml"));
		factoryBean.setTypeAliasesPackage("shop.ourshopping.dto.mybatis");

		return factoryBean.getObject();
	}

	// MyBatis쿼리문을 수행해주는 역할
	@Bean
	public SqlSessionTemplate sqlSession() throws Exception {

		return new SqlSessionTemplate(sqlSessionFactory());
	}

	@Bean(name = "transactionManager")
	@Primary
	public PlatformTransactionManager transactionManager() {

		return new DataSourceTransactionManager(dataSource());
	}
}