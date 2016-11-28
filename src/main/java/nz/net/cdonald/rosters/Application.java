package nz.net.cdonald.rosters;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public EbeanServerFactoryBean ebeanServerFactoryBean() {
        EbeanServerFactoryBean ebeanServerFactoryBean = new EbeanServerFactoryBean();
        ServerConfig config = new ServerConfig();

        config.setName("db");
        config.loadFromProperties();
        config.setDefaultServer(true);

        ebeanServerFactoryBean.setServerConfig(config);
        return ebeanServerFactoryBean;
    }
}


