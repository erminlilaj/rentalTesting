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

    // Helper methods for test setup
    private UserEntity createMockUser(Long id, String username, int age) {
        return new UserEntity(id, "password", "John", "Doe", "john@example.com", username, age);
    }

    private CreateUserRequest createMockCreateUserRequest(String username, int age) {
        return new CreateUserRequest(username, "John", "Doe", "john@example.com", "password", age);
    }

    private UpdateUserRequest createMockUpdateUserRequest(String username, String password, int age) {
        return new UpdateUserRequest(username, password, age);
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        CreateUserRequest request = createMockCreateUserRequest("johndoe", 25);
        UserEntity savedUser = createMockUser(1L, "johndoe", 25);

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        UserEntity result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testCreateUser_AgeUnder18() {
        // Arrange
        CreateUserRequest request = createMockCreateUserRequest("johndoe", 16);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> userService.createUser(request));
        assertEquals(ErrorCode.USER_NOT_ELIGIBLE, exception.getErrorCode());
        assertEquals("Age must be at least 18", exception.getMessage());
    }

    @Test
    void testFindAllUsers_ReturnsUsers() {
        // Arrange
        List<UserEntity> mockUsers = List.of(
                createMockUser(1L, "johndoe", 25),
                createMockUser(2L, "janedoe", 30)
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserEntity()));
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = userService.deleteUser(1L);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_AsNonAdmin() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(false);

        // Act & Assert
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.deleteUser(1L)
        );

        assertEquals("Only admins can delete users", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Arrange
        when(authenticationService.isAdmin()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> userService.deleteUser(1L)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        UserEntity mockUser = createMockUser(1L, "johndoe", 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        UserEntity result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> userService.getUserById(1L)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        UpdateUserRequest request = createMockUpdateUserRequest("newusername", "newpassword", 30);
        UserEntity existingUser = createMockUser(1L, "johndoe", 25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        // Act
        UserEntity result = userService.updateUser(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Arrange
        UpdateUserRequest request = createMockUpdateUserRequest("newusername", "newpassword", 30);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> userService.updateUser(1L, request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUpdateUser_UsernameAlreadyExists() {
        // Arrange
        UpdateUserRequest request = createMockUpdateUserRequest("existingUsername", "newpassword", 30);
        UserEntity existingUser = createMockUser(1L, "johndoe", 25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("existingUsername")).thenReturn(true);

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> userService.updateUser(1L, request)
        );

        assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void testUpdateUser_AgeUnder18() {
        // Arrange
        UpdateUserRequest request = createMockUpdateUserRequest("newusername", "newpassword", 16);
        UserEntity existingUser = createMockUser(1L, "johndoe", 25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> userService.updateUser(1L, request)
        );

        assertEquals(ErrorCode.USER_NOT_ELIGIBLE, exception.getErrorCode());
        assertEquals("Age must be at least 18", exception.getMessage());
    }
}
