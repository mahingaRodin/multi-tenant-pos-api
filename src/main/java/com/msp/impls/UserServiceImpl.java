package com.msp.impls;

import com.msp.configs.JwtProvider;
import com.msp.exceptions.UserException;
import com.msp.mappers.UserMapper;
import com.msp.models.User;
import com.msp.payloads.dtos.UserDto;
import com.msp.repositories.UserRepository;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepo;
    private final JwtProvider provider;

    @Override
    @Cacheable(value = "users-by-token", key = "#token")
    public User getCurrentUserFromToken(String token) throws UserException {
        String email = provider.getEmailFromToken(token);
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UserException("Invalid Token!");
        }
        return user;
    }

    @Override
    public User getCurrentUser() throws UserException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserByEmail(email);
    }

    @Override
    @Cacheable(key = "#email")
    public User getUserByEmail(String email) throws UserException {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UserException("User Not Found!");
        }
        return user;
    }

    @Override
    @Cacheable(key = "#id")
    public User getUserById(UUID id) throws UserException {
        return userRepo.findById(id).orElse(null);
    }

    @Override
    @Cacheable(value = "users-page", key = "#page + '-' + #size + '-' + #sortBy")
    public Page<UserDto> getAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return userRepo.findAll(pageable).map(UserMapper::toDTO);
    }
}