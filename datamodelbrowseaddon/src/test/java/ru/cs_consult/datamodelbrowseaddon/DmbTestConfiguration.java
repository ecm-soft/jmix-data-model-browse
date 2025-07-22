package ru.cs_consult.datamodelbrowseaddon;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.sql.DataSource;
import java.util.List;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(DmbConfiguration.class)
@PropertySource("classpath:/ru/cs_consult/datamodelbrowseaddon/test-app.properties")
@JmixModule(id = "ru.cs_consult.datamodelbrowseaddon.test", dependsOn = DmbConfiguration.class)
public class DmbTestConfiguration {

    @Bean
    @Primary
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    @Primary
    UserRepository UserRepository(AnnotatedResourceRoleProvider resourceRoleProvider) {
        UserDetails admin = User.builder().username("admin").password("{noop}")
                .authorities(List.of(RoleGrantedAuthority.ofResourceRole(resourceRoleProvider.getRoleByCode("system-access")))).build();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        userRepository.addUser(admin);
        return userRepository;
    }
}
