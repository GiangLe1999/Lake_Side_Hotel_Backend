package vn.riverlee.lake_side_hotel.service;

import vn.riverlee.lake_side_hotel.model.User;

import java.util.Optional;

public interface UserService {
    User registerUser(String email, String password, String fullName);

    User findOrCreateGoogleUser(String email, String googleId, String fullName);

    Optional<User> findByEmail(String email);
}
