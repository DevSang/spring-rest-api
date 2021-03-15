package airi.ojt.backend.ojtrestapi.config;

import airi.ojt.backend.ojtrestapi.accounts.Account;
import airi.ojt.backend.ojtrestapi.accounts.AccountRole;
import airi.ojt.backend.ojtrestapi.accounts.AccountService;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public ApplicationRunner applicationRunner() {
    return new ApplicationRunner() {
      @Autowired
      AccountService accountService;

      @Override
      public void run(ApplicationArguments args) throws Exception {
        Account sanghyuk = Account.builder()
            .email("test@gmail.com")
            .password("airi1234")
            .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
            .build();
        accountService.saveAccount(sanghyuk);
      }
    };
  }
}
