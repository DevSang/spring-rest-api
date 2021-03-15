package airi.ojt.backend.ojtrestapi.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import airi.ojt.backend.ojtrestapi.accounts.Account;
import airi.ojt.backend.ojtrestapi.accounts.AccountRole;
import airi.ojt.backend.ojtrestapi.accounts.AccountService;
import airi.ojt.backend.ojtrestapi.common.BaseControllerTest;
import airi.ojt.backend.ojtrestapi.common.TestDescription;
import java.util.Set;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthServerConfigTest extends BaseControllerTest {

  @Autowired
  AccountService accountService;

  @Test
  @TestDescription("인증 토급을 발급 받는 테스트")
  public void getAuthToken() throws Exception {
    //Given
    String username = "dev.sanghyuk@gmail.com";
    String password = "airi1234";
    Account sanghyuk = Account.builder()
        .email(username)
        .password(password)
        .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
        .build();

    this.accountService.saveAccount(sanghyuk);

    String clientId = "myApp";
    String clientSecret = "pass";

    this.mockMvc.perform(post("/oauth/token")
        .with(httpBasic(clientId, clientSecret))
        .param("username", username)
        .param("password", password)
        .param("grand_type", "password"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("access_token").exists());
  }

}