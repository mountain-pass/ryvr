package au.com.mountainpass.ryvr.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override

  protected void configure(HttpSecurity http) throws Exception {
    http.headers().cacheControl().disable();
    http.httpBasic().and().authorizeRequests()
        .antMatchers("/", "/webjars/**", "/css/**", "/webjars.js", "/js/**", "/images/**",
            "/favicon.ico", "/favicon-*", "mstile-*", "apple-touch-icon-*")
        .permitAll().anyRequest().authenticated().and().formLogin().loginPage("/").permitAll().and()
        .logout().permitAll().and().addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class).csrf()
        .csrfTokenRepository(csrfTokenRepository());
  }

  private CsrfTokenRepository csrfTokenRepository() {
    HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
    repository.setHeaderName("X-XSRF-TOKEN");
    return repository;
  }

  // @Autowired
  // public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
  // auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
  // }

  public class CsrfHeaderFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
      CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
      if (csrf != null) {
        Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
        String token = csrf.getToken();
        if (cookie == null || token != null && !token.equals(cookie.getValue())) {
          cookie = new Cookie("XSRF-TOKEN", token);
          cookie.setPath("/");
          response.addCookie(cookie);
        }
      }
      filterChain.doFilter(request, response);
    }
  };

  @Bean
  public CsrfHeaderFilter csrfHeaderFilter() {
    return new CsrfHeaderFilter();
  }
}
