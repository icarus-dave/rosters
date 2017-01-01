package nz.net.cdonald.rosters

import com.auth0.spring.security.api.JwtWebSecurityConfigurer
import com.avaje.ebean.EbeanServer
import com.avaje.ebean.EbeanServerFactory
import com.avaje.ebean.config.Platform
import com.avaje.ebean.config.ServerConfig
import com.avaje.ebean.dbmigration.DbMigration
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.SimpleCommandLinePropertySource
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@SpringBootApplication
public class Application {
	public static void main(String[] args) throws Exception {

		def cmdArgs = new SimpleCommandLinePropertySource(args)

		if (cmdArgs.containsProperty("generateDB")) {
			//Generate new database schema with command:
			//mvn spring-boot:run -Drun.arguments="--generateDB,--version=1"
			if (!cmdArgs.containsProperty("version"))
				throw new IllegalArgumentException("Version not specified, usage: --generateDB --version=1")

			def version = cmdArgs.getProperty("version");

			ServerConfig config = new ServerConfig()
			config.setName("db")
			config.setDefaultServer(true)
			config.getDataSourceConfig().setOffline(true)
			config.setDatabasePlatformName("h2")
			config.addPackage("nz.net.cdonald.rosters.domain")
			EbeanServer server = EbeanServerFactory.create(config)

			System.setProperty("ddl.migration.generate", "true")
			System.setProperty("ddl.migration.version", version)
			System.setProperty("ddl.migration.name", "rosters")
			DbMigration dbMigration = new DbMigration()
			dbMigration.setPathToResources(".")
			dbMigration.setPlatform(Platform.H2)
			dbMigration.generateMigration()

			return
		}

		SpringApplication.run(Application.class, args);
	}

	@Bean
	public EbeanServerFactoryBean ebeanServerFactoryBean() {
		EbeanServerFactoryBean ebeanServerFactoryBean = new EbeanServerFactoryBean();
		ServerConfig config = new ServerConfig();

		config.setName("db");
		config.loadFromProperties()
		//doesn't seem to pick up packages...
		config.addPackage("nz.net.cdonald.rosters.domain")
		config.setDefaultServer(true)
		ebeanServerFactoryBean.setServerConfig(config)
		return ebeanServerFactoryBean
	}
}


@Configuration
@Profile("nocors")
public class NoCORSConfiguration {
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				//Let us hack on the front end without security errors
				registry.addMapping("/**")
						.allowedMethods("*")
			}
		};
	}
}

/*
When using the spring-boot command, by default it will use profiles:
* local
* noauth

Of course somethings things may not work as expected, thus if you want auth then run:
mvn spring-boot:run -Drun.arguments=local
*/
@EnableWebSecurity
@Configuration
@Profile("!noauth") //double negative!
public class AuthConfig extends WebSecurityConfigurerAdapter {

	@Value('${jwt.audience}')
	String audience

	@Value('${jwt.issuer}')
	String issuer

	@Value('${jwt.secret}')
	String secret

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		JwtWebSecurityConfigurer
				.forHS256(audience, issuer, secret.getBytes())
				.configure(http)
				.authorizeRequests()
					.antMatchers("/api/**").fullyAuthenticated();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/api/webconfig");
	}
}

@EnableWebSecurity
@Configuration
@Profile("noauth") //double negative!
public class NoAuthConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().anyRequest().permitAll();
	}

}