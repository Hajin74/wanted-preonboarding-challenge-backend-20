package org.example.market.service;

import lombok.RequiredArgsConstructor;
import org.example.market.dto.user.CustomUserDetails;
import org.example.market.domain.User;
import org.example.market.repository.jpa.UserJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 조회
        User user = userRepository.findByUsername(username);

        if(user != null) {
            // UserDetails에 담아서 반환하면 AuthenticationManager가 검증
            return new CustomUserDetails(user);
        }

        return null;
    }
}