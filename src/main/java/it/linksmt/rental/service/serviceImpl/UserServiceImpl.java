package it.linksmt.rental.service.serviceImpl;

import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.UpdateUserRequest;
import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    public AuthenticationService authenticationService;

    public UserServiceImpl(UserRepository userRepository, AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    @Override
    public UserEntity createUser(CreateUserRequest createUserRequest) {
        if (createUserRequest.getAge() < 18) {
            throw new ServiceException(ErrorCode.USER_NOT_ELIGIBLE, "Age must be at least 18");
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(createUserRequest.getUsername());
        userEntity.setName(createUserRequest.getName());
        userEntity.setSurname(createUserRequest.getSurname());
        userEntity.setEmail(createUserRequest.getEmail());
        userEntity.setPassword(createUserRequest.getPassword());
        userEntity.setAge(createUserRequest.getAge());
        //userEntity.setUserType(createUserRequest.getUserType());
        return userRepository.save(userEntity);

    }

    @Override
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean deleteUser(Long id) {
//        SecurityBean currentUser = SecurityContext.get();

        if (!authenticationService.isAdmin()) {
            throw new AccessDeniedException("Only admins can delete users");
        }
        if(userRepository.findById(id).isEmpty()){
            throw new ServiceException(ErrorCode.USER_NOT_FOUND,
                    "User not found");
        }
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        //todo throw EntityNotFound
        return false;
    }

    @Override
    public UserEntity getUserById(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new ServiceException(ErrorCode.USER_NOT_FOUND,
                    "User not found");
        }
        Optional<UserEntity> user = userRepository.findById(id);
        //todo throw exception
        return user.orElse(null);
    }

    @Override
    public UserEntity updateUser(Long id, UpdateUserRequest updateUserRequest) {
        if (getUserById(id) == null) {
            return null;
        }
        UserEntity user = getUserById(id);
        //update only fields that are not emptyy
        if(userRepository.existsByUsername(updateUserRequest.getUsername())){
            throw new ServiceException(ErrorCode.USER_ALREADY_EXISTS,
                    "Username already exists");
        }
        if (updateUserRequest.getUsername() != null) {
            user.setUsername(updateUserRequest.getUsername());
        }
        if (updateUserRequest.getPassword() != null) {
            user.setPassword(updateUserRequest.getPassword());
        }
        if(updateUserRequest.getAge()<18){
            throw new ServiceException(ErrorCode.USER_NOT_ELIGIBLE,
                    "Age must be at least 18");
        }
            user.setAge(updateUserRequest.getAge());

        return userRepository.save(user);

    }
}
