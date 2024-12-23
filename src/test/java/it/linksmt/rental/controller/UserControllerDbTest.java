package it.linksmt.rental.controller;

import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.UpdateUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.UserType;
import it.linksmt.rental.repository.UserRepository;
//import it.linksmt.rental.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
//@Testcontainers
public class UserControllerDbTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private UserEntity createdUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest("johNdoe", "John", "Doe", "johndoe@example.com", "password", 30, UserType.USER);
        updateUserRequest = new UpdateUserRequest("updatedUsername", "updatedPassw", 44);
        createdUser = new UserEntity(null, "johnndoe", "John", "Doe", "johndoe@example.com", "password", 30, UserType.USER, LocalDateTime.now(), LocalDateTime.now(), null);
        userRepository.deleteAll();
    }

    @Test
    void createUserTest() throws Exception {
        String requestBody = objectMapper.writeValueAsString(createUserRequest);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        List<UserEntity> users = userRepository.findAll();
        assert !users.isEmpty();
        assert users.get(0).getUsername().equals("johNdoe");

    }

    @Test
    void getUserById() throws Exception {

        UserEntity savedUser = userRepository.save(createdUser);


        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    void getUserByIdNotFound() throws Exception {


        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound());

    }

    @Test
    void getAllUsers() throws Exception {
        UserEntity savedUser = userRepository.save(new UserEntity(null, "admin1", "John", "Doe", "admin1@example.com", "password", 30, UserType.ADMIN, null, null, null));
        UserEntity savedUser2 = userRepository.save(new UserEntity(null, "admin2", "John", "Doe", "admin2@example.com", "password", 30, UserType.ADMIN, null, null, null));
        UserEntity savedUser3 = userRepository.save(new UserEntity(null, "admin3", "John", "Doe", "admin3@example.com", "password", 30, UserType.ADMIN, null, null, null));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
        List<UserEntity> users = userRepository.findAll();
        assert !users.isEmpty();
        assert users.get(0).getId().equals(savedUser.getId());
        assert users.get(1).getId().equals(savedUser2.getId());
        assert users.get(2).getId().equals(savedUser3.getId());

    }


    @Test
    void deleteUser() throws Exception {

        UserEntity savedUser = userRepository.save(createdUser);

        mockMvc.perform(delete("/api/users/" + savedUser.getId()))
                .andExpect(status().isNoContent());


        assert userRepository.findById(savedUser.getId()).isEmpty();
    }

    @Test
    void updateUser() throws Exception {
        UserEntity savedUser = userRepository.save(createdUser);
        String requestBody = objectMapper.writeValueAsString(updateUserRequest);


        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUsername"))
                .andExpect(jsonPath("$.age").value(44));
    }
}