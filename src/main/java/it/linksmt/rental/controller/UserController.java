package it.linksmt.rental.controller;

import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.UpdateUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody CreateUserRequest createUserRequest) {
        UserEntity createdUser = userService.createUser(createUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        List<UserEntity> userList = userService.findAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {

        UserEntity user = userService.getUserById(id);


        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {

            return ResponseEntity.status(HttpStatus.OK).body(user);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest updateUserRequest) {
        UserEntity updatedUser = userService.updateUser(id, updateUserRequest);

        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);


    }
}
