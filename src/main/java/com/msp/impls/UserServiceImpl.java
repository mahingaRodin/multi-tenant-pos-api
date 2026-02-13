package com.msp.impls;

import com.msp.configs.JwtProvider;
import com.msp.exceptions.UserException;
import com.msp.models.User;
import com.msp.repositories.UserRepository;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepo;
    private final JwtProvider provider;

    @Override
    public User getCurrentUserFromToken(String token) throws UserException {
        String email = provider.getEmailFromToken(token);
        User user = userRepo.findByEmail(email);
        if(user==null) {
            throw new UserException("Invalid Token!");
        }
        return user;
    }

    @Override
    public User getCurrentUser() throws UserException{
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByEmail(email);
        if(user==null) {
            throw new UserException("User Not Found!");
        }
        return user;
    }

    @Override
    public User getUserByEmail(String email) throws UserException{
        User user = userRepo.findByEmail(email);
        if(user==null) {
            throw new UserException("User Not Found!");
        }
        return user;
    }

    @Override
    public User getUserById(UUID id) throws UserException {
        return userRepo.findById(id).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
