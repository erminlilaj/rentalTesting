package it.linksmt.rental;

import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.UpdateUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.serviceImpl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "johndoe";
    private static final String NEW_USERNAME = "newusername";
    private static final String PASSWORD = "password";
    private static final String NEW_PASSWORD = "newpassword";
    private static final String NAME = "John";
    private static final String SURNAME = "Doe";
    private static final String EMAIL = "john@example.com";
    private static final int AGE = 25;
    private static final int NEW_AGE = 30;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(USERNAME, NAME, SURNAME, EMAIL, PASSWORD, AGE);
        UserEntity savedUser = new UserEntity(USER_ID, PASSWORD, NAME, SURNAME, EMAIL, USERNAME, AGE);

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        UserEntity result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));

        // Capture and verify saved entity
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity capturedEntity = captor.getValue();
        assertEquals(AGE, capturedEntity.getAge());
    }

    @Test
    void testCreateUser_AgeUnder18() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(USERNAME, NAME, SURNAME, EMAIL, PASSWORD, 16);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> userService.createUser(request));
        assertEquals(ErrorCode.USER_NOT_ELIGIBLE, exception.getErrorCode());
    }

    @Test
    void testFindAllUsers_ReturnsUsers() {
        // Arrange
        List<UserEntity> mockUsers = List.of(
                new UserEntity(1L, USERNAME, NAME, SURNAME, EMAIL, PASSWORD, AGE),
                new UserEntity(2L, "janedoe", "Jane", "Doe", "jane@example.com", "securepass", 30)
        );
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act
        List<UserEntity> result = userService.findAllUsers();

        // Assert
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindAllUsers_Empty() {
        // Arrange
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<UserEntity> result = userService.findAllUsers();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeleteUser_AsAdmin_Success() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(true);
        when(userRepository.existsById(USER_ID)).thenReturn(true);

        // Act
        boolean result = userService.deleteUser(USER_ID);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(USER_ID);
    }

    @Test
    void testDeleteUser_AsNonAdmin() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(false);

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.deleteUser(USER_ID)
        );

        assertEquals("Only admins can delete users", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(true);
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        // Act
        boolean result = userService.deleteUser(USER_ID);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        UserEntity mockUser = new UserEntity(USER_ID, PASSWORD, NAME, SURNAME, EMAIL, USERNAME, AGE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));

        // Act
        UserEntity result = userService.getUserById(USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act
        UserEntity result = userService.getUserById(USER_ID);

        // Assert
        assertNull(result);
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(NEW_USERNAME, NEW_PASSWORD, NEW_AGE);
        UserEntity existingUser = new UserEntity(USER_ID, USERNAME, NAME, SURNAME, EMAIL, PASSWORD, AGE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        // Act
        UserEntity result = userService.updateUser(USER_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(NEW_USERNAME, result.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(NEW_USERNAME, NEW_PASSWORD, NEW_AGE);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act
        UserEntity result = userService.updateUser(USER_ID, request);

        // Assert
        assertNull(result);
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
