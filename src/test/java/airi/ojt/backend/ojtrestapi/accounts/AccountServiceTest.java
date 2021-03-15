package airi.ojt.backend.ojtrestapi.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();


  @Autowired
  AccountService accountService;

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Test
  public void findByUsername() {
    // Given
    String password = "keesun";
    String username = "keesun@email.com";
    Account account = Account.builder()
        .email(username)
        .password(password)
        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
        .build();
    this.accountService.saveAccount(account);

    // When
    UserDetailsService userDetailsService = accountService;
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    // Then
    assertThat(this.passwordEncoder.matches(password, userDetails.getPassword())).isTrue();
  }

  @Test
  public void findByUsernameFail() {
    //Expected
    String username = "ran@gmail.com";
    expectedException.expect(UsernameNotFoundException.class);
    expectedException.expectMessage(Matchers.containsString(username));

    //When
    accountService.loadUserByUsername(username);
  }
} 