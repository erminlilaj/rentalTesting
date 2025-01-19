package it.linksmt.rental;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collections;

import it.linksmt.rental.dto.LoginUserRequest;
import it.linksmt.rental.dto.RegisterUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.enums.UserType;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.security.SecurityBean;
import it.linksmt.rental.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterUserRequest registerUserRequest;
    private LoginUserRequest loginUserRequest;

    @BeforeEach
    void setUp() {
        registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setUsername("testUser");
        registerUserRequest.setName("Test");
        registerUserRequest.setSurname("User");
        registerUserRequest.setEmail("test@example.com");
        registerUserRequest.setPassword("password");
        registerUserRequest.setAge(20);

        loginUserRequest = new LoginUserRequest();
        loginUserRequest.setUsername("testUser");
        loginUserRequest.setPassword("password");
    }

    @Test
    void testSignUp_UserAlreadyExistsByUsername() {
        when(userRepository.existsByUsername(registerUserRequest.getUsername())).thenReturn(true);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            authenticationService.signUp(registerUserRequest);
        });

        assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("User already exists with username: " + registerUserRequest.getUsername(), exception.getMessage());
    }

    @Test
    void testSignUp_UserAlreadyExistsByEmail() {
        when(userRepository.existsByUsername(registerUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerUserRequest.getEmail())).thenReturn(true);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            authenticationService.signUp(registerUserRequest);
        });

        assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("User already exists with email: " + registerUserRequest.getEmail(), exception.getMessage());
    }

    @Test
    void testSignUp_UserNotEligibleByAge() {
        registerUserRequest.setAge(17);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            authenticationService.signUp(registerUserRequest);
        });

        assertEquals(ErrorCode.USER_NOT_ELIGIBLE, exception.getErrorCode());
        assertEquals("Age must be at least 18", exception.getMessage());
    }

    @Test
    void testSignUp_Success() {
        when(userRepository.existsByUsername(registerUserRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        UserEntity userEntity = authenticationService.signUp(registerUserRequest);

        assertNotNull(userEntity);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testAuthenticate_Success() {
        when(userRepository.findByUsername(loginUserRequest.getUsername())).thenReturn(Optional.of(new UserEntity()));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        UserEntity userEntity = authenticationService.authenticate(loginUserRequest);

        assertNotNull(userEntity);
    }

    @Test
    void testAuthenticate_InvalidCredentials() {
        doThrow(new BadCredentialsException("Invalid credentials")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            authenticationService.authenticate(loginUserRequest);
        });

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
        assertEquals("Invalid username or password", exception.getMessage());
    }

//    @Test
//    void testIsAdmin() {
//        SecurityBean securityBean = mock(SecurityBean.class);
//        when(securityBean.getAuthorities()).thenReturn(Collections.singletonList((GrantedAuthority) () -> "ROLE_ADMIN"));
//
//        try (MockedStatic<SecurityContext> mockedSecurityContext = mockStatic(SecurityContext.class)) {
//            mockedSecurityContext.when(SecurityContext::get).thenReturn(securityBean);
//
//            assertTrue(authenticationService.isAdmin());
//        }
//    }

    @Test
    void testGetCurrentUserId_UserNotLoggedIn() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("username");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            authenticationService.getCurrentUserId();
        });

        assertEquals(ErrorCode.UNAUTHORIZED_ACCESS, exception.getErrorCode());
        assertEquals("User not loogged in", exception.getMessage());
    }

    @Test
    void testGetCurrentUserId_Success() {
        when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("username");
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(userEntity));

        Long userId = authenticationService.getCurrentUserId();

        assertEquals(1L, userId);
    }
}