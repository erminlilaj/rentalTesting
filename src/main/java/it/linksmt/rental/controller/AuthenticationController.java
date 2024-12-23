package it.linksmt.rental.controller;

import it.linksmt.rental.dto.CreateUserRequest;
import it.linksmt.rental.dto.LoginUserRequest;
import it.linksmt.rental.dto.RegisterUserRequest;
import it.linksmt.rental.entity.UserEntity;

import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.JwtService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/auth")
@RestController
@CrossOrigin
public class AuthenticationController {

    private final JwtService jwtService;

    private final AuthenticationService authenticationService;
    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }
    @PostMapping(value = "/signup",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest registerUserRequest) {

            UserEntity registeredUser = authenticationService.signUp(registerUserRequest);
            return ResponseEntity.ok().body(registeredUser);

    }
    @PostMapping(value = "/login",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> authenticate(@RequestBody LoginUserRequest loginUserRequest){

           UserEntity user = authenticationService.authenticate(loginUserRequest);
           String token = jwtService.generateToken(user);

           return ResponseEntity.ok(token);
    }
    @GetMapping(value = "/userId",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long>getLoggedUserId(){
        Long userId=authenticationService.getCurrentUserId();
        return ResponseEntity.ok(userId);
    }
    @GetMapping(value = "/isAdmin",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isAdmin(){
        boolean isAdmin=authenticationService.isAdmin();
        return ResponseEntity.ok(isAdmin);
    }

}
