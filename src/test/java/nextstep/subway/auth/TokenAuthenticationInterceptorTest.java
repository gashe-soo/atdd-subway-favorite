package nextstep.subway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import nextstep.subway.auth.domain.Authentication;
import nextstep.subway.auth.domain.AuthenticationToken;
import nextstep.subway.auth.dto.TokenRequest;
import nextstep.subway.auth.dto.UserPrincipal;
import nextstep.subway.auth.infrastructure.JwtTokenProvider;
import nextstep.subway.auth.ui.UserAuthenticate;
import nextstep.subway.auth.ui.TokenAuthenticate;
import nextstep.subway.auth.ui.UserLoader;
import nextstep.subway.auth.ui.token.TokenAuthenticationConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationInterceptorTest {

    @Mock
    private UserLoader userLoader;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private TokenAuthenticate tokenAuthenticate;

    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "password";
    public static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.ih1aovtQShabQ7l0cINw4k1fagApg3qLWiB8Kt59Lno";

    @BeforeEach
    void setUp(){
        when(userLoader.loadUserPrincipal(anyString())).thenReturn(new UserPrincipal(1L, EMAIL, 20));
        this.tokenAuthenticate = new UserAuthenticate(userLoader);
    }

    @Test
    void convert() throws IOException {
        // given
        MockHttpServletRequest request = createMockRequest();
        TokenAuthenticationConverter authenticationConverter = new TokenAuthenticationConverter();

        // when
        AuthenticationToken authenticationToken = authenticationConverter.convert(request);

        // then
        assertThat(authenticationToken.getPrincipal()).isEqualTo(EMAIL);
        assertThat(authenticationToken.getCredentials()).isEqualTo(PASSWORD);
    }

    @Test
    void authenticate() {
        // given
        AuthenticationToken authenticationToken = new AuthenticationToken(EMAIL, PASSWORD);

        // when
        Authentication authentication = tokenAuthenticate.authenticate(authenticationToken);

        // then
        assertThat(authentication.getPrincipal()).isNotNull();
    }

    @Test
    void preHandle() throws IOException {
        // given
        MockHttpServletRequest request = createMockRequest();
        TokenAuthenticationConverter authenticationConverter = new TokenAuthenticationConverter();

        // when
        AuthenticationToken authenticationToken = authenticationConverter.convert(request);
        Authentication authentication = tokenAuthenticate.authenticate(authenticationToken);

        // then
        assertThat(authentication.getPrincipal()).isNotNull();
    }

    @Test
    public void afterAuthentication() throws IOException{
        // given
        MockHttpServletRequest request = createMockRequest();
        TokenAuthenticationConverter authenticationConverter = new TokenAuthenticationConverter();
        AuthenticationToken authenticationToken = authenticationConverter.convert(request);
        when(jwtTokenProvider.createToken(anyString())).thenReturn(JWT_TOKEN);

        // when
        Authentication authentication = tokenAuthenticate.authenticate(authenticationToken);
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        String tokenString = jwtTokenProvider.createToken(new ObjectMapper().writeValueAsString(userDetails));

        // then
        assertThat(tokenString).isEqualTo(JWT_TOKEN);
    }

    private MockHttpServletRequest createMockRequest() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        TokenRequest tokenRequest = new TokenRequest(EMAIL, PASSWORD);
        request.setContent(new ObjectMapper().writeValueAsString(tokenRequest).getBytes());
        return request;
    }

}
