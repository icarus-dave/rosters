package nz.net.cdonald.rosters

import com.avaje.ebean.EbeanServer
import com.avaje.ebean.EbeanServerFactory
import com.avaje.ebean.config.Platform
import com.avaje.ebean.config.ServerConfig
import com.avaje.ebean.dbmigration.DbMigration
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.SimpleCommandLinePropertySource
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
@Profile("local")
public class LocalDevConfiguration {
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