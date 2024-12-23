package it.linksmt.rental.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.UpdateUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.UserType;
import it.linksmt.rental.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;

//@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; //to convert objects to json format

    @MockBean
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private UserEntity createdUser;
    private UpdateUserRequest updateUserRequest;
    private UserEntity updatedUser;
    private UserType userType;

    @BeforeEach
    public void setUp() {
        createUserRequest = new CreateUserRequest("johndoe", "John", "Doe", "johndoe@example.com", "password123", 30, UserType.USER);
        createdUser = new UserEntity(1L, "johndoe", "John", "Doe", "johndoe@example.com", "hashedPassword", 30, UserType.USER, LocalDateTime.now(), LocalDateTime.now(), null);
//        updateUserRequest = new UpdateUserRequest("newusername", "newpassword", 30);
//        updatedUser = new UserEntity(1L, "newusername", "John", "Doe", "johndoe@example.com", "newpassword", 20, UserType.USER, LocalDateTime.now(), LocalDateTime.now(), null);
        updateUserRequest = new UpdateUserRequest("newusername", "newpassword", 30);
        updatedUser = new UserEntity(1L, "newusername", "John", "Doe", "johndoe@example.com", "newpassword", 30, UserType.USER, LocalDateTime.now(), LocalDateTime.now(), null);
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);
       // when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser); // Updated mock setup to handle id
    }


    @Test
    void createUserTest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.username").value(createdUser.getUsername()))
                .andExpect(jsonPath("$.name").value(createdUser.getName()))
                .andExpect(jsonPath("$.surname").value(createdUser.getSurname()))
                .andExpect(jsonPath("$.email").value(createdUser.getEmail()))
                .andExpect(jsonPath("$.age").value(createdUser.getAge()))
                .andExpect(jsonPath("$.userType").value(createdUser.getUserType().name()));
    }


    @Test
    void getAllUsersTest() throws Exception {
//        mockMvc.perform(get("/api/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createUserRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(createdUser.getId()))
//                .andExpect(jsonPath("$.username").value(createdUser.getUsername()))
//                .andExpect(jsonPath("$.name").value(createdUser.getName()))
//                .andExpect(jsonPath("$.surname").value(createdUser.getSurname()))
//                .andExpect(jsonPath("$.email").value(createdUser.getEmail()))
//                .andExpect(jsonPath("$.age").value(createdUser.getAge()))
//                .andExpect(jsonPath("$.userType").value(createdUser.getUserType().name()));
        //todo
        List<UserEntity> userList = List.of(createdUser);
        when(userService.findAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(userList.size()))
                .andExpect(jsonPath("$[0].id").value(createdUser.getId()))
                .andExpect(jsonPath("$[0].username").value(createdUser.getUsername()));
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUserById(eq(createdUser.getId()))).thenReturn(createdUser);
        mockMvc.perform(get("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.username").value(createdUser.getUsername()))
                .andExpect(jsonPath("$.name").value(createdUser.getName()))
                .andExpect(jsonPath("$.surname").value(createdUser.getSurname()));
    }

    @Test
    void getUserByIdNotFoundTest() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserTest() throws Exception {
        when(userService.deleteUser(eq(createdUser.getId()))).thenReturn(true);

        mockMvc.perform(delete("/api/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());
    }


    @Test
    void updateUserTest() throws Exception {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("newusername", "newpassword", 30);

        when(userService.updateUser(eq(createdUser.getId()), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.age").value(30));
    }

}
