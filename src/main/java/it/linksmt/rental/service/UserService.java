package it.linksmt.rental.service;

import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.UpdateUserRequest;
import it.linksmt.rental.entity.UserEntity;

import java.util.List;

public interface UserService {

    UserEntity createUser(CreateUserRequest createUserRequest);

    List<UserEntity> findAllUsers();

    boolean deleteUser(Long id);

    UserEntity getUserById(Long id);

    UserEntity updateUser(Long id, UpdateUserRequest updateUserRequest);
}
