package com.joohoyo.kakaopay.task.app.user;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> get(String id) {
        return userRepository.findById(id);
    }
}
