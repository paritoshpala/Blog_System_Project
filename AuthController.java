package studentapp.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import studentapp.model.User;
import studentapp.repository.UserRepository;

import java.util.Objects;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String showSignup(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (!Objects.equals(user.getPassword(), user.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }

        if (user.getUsername() != null && !user.getUsername().isBlank()
                && userRepository.existsByUsername(user.getUsername().trim())) {
            bindingResult.rejectValue("username", "username.exists", "Username already exists.");
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()
                && userRepository.existsByEmail(user.getEmail().trim().toLowerCase())) {
            bindingResult.rejectValue("email", "email.exists", "Email already exists.");
        }

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        user.setFullName(user.getFullName().trim());
        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("registered", true);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }
}
