package vn.riverlee.lake_side_hotel.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import vn.riverlee.lake_side_hotel.model.User;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    User registerUser(String email, String password, String fullName);

    User findOrCreateGoogleUser(String email, String googleId, String fullName);

    Optional<User> findByEmail(String email);
}
