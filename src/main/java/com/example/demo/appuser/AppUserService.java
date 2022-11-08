package com.example.demo.appuser;

import com.example.demo.registration.token.ConfirmationToken;
import com.example.demo.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final String USER_NOT_FOUND = "user with email %s not found";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder encoder;

    private final ConfirmationTokenService tokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND, email)));
    }

    public String signUpUser(AppUser user) {
        boolean isExists = appUserRepository.findByEmail(user.getEmail()).isPresent();
        if (isExists) {
            throw new IllegalStateException("email ya adquirido");
        }

        String econdePass = encoder.encode(user.getPassword());

        user.setPassword(econdePass);
        appUserRepository.save(user);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationTokentoken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );

        tokenService.saveToken(confirmationTokentoken);

        return token;

        // Falta enviar correo

    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
