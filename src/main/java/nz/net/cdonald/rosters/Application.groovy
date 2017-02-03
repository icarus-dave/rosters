package nz.net.cdonald.rosters

import com.auth0.spring.security.api.JwtWebSecurityConfigurer
import com.avaje.ebean.EbeanServer
import com.avaje.ebean.EbeanServerFactory
import com.avaje.ebean.config.Platform
import com.avaje.ebean.config.ServerConfig
import com.avaje.ebean.dbmigration.DbMigration
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import nz.net.cdonald.rosters.auth.AuthenticationExceptionEntryPoint
import nz.net.cdonald.rosters.auth.CustomAccessDeniedHandler
import nz.net.cdonald.rosters.auth.InviteAuthnComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.SimpleCommandLinePropertySource
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

import javax.annotation.PostConstruct

@SpringBootApplication
public class Application {
	public static void main(String[] args) throws Exception {

		def cmdArgs = new SimpleCommandLinePropertySource(args)

		if (cmdArgs.containsProperty("generateDB")) {
			//Generate new database schema with command:
			//mvn spring-boot:run -Drun.arguments="--generateDB,--version=1"
			//the generated files will be placed in ./dbmigration which will then be bundled into the WAR (as specified in pom.xml)
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

	/*
		Use Jackson Views to restrict what is returned to the client
	 */
	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	public void configureObjectMapper() {
		objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,true)
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
 mvn spring-boot:run -Drun.profiles=nocors
*/
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("!noauth") //double negative!
public class AuthConfig extends WebSecurityConfigurerAdapter {

	@Value('${jwt.audience}')
	String audience

	@Value('${jwt.issuer}')
	String issuer

	@Value('${jwt.secret}')
	String secret

	@Autowired
	InviteAuthnComponent inviteAuthnComponent

	@Autowired
	AuthenticationExceptionEntryPoint authenticationExceptionEntryPoint

	@Autowired
	CustomAccessDeniedHandler customAccessDeniedHandler

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		JwtWebSecurityConfigurer
				.forHS256(audience, issuer, inviteAuthnComponent)
				.configure(http)
					.exceptionHandling()
					.authenticationEntryPoint(authenticationExceptionEntryPoint)
					.accessDeniedHandler(customAccessDeniedHandler).and()
				//broad access decisions are made outside of business logic code
				.authorizeRequests()
						//enable CORS support
						.antMatchers(HttpMethod.OPTIONS,"/**").permitAll()
						//webconfig is cool for all (i.e. to configure front-end app)
						.antMatchers(HttpMethod.GET,"/api/webconfig").permitAll()
						//everything else must be authenticated, with the controller adding authz
						.anyRequest().fullyAuthenticated();
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
