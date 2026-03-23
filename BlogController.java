package studentapp.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import studentapp.model.Blog;
import studentapp.model.User;
import studentapp.repository.BlogRepository;
import studentapp.repository.UserRepository;

@Controller
public class BlogController {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    public BlogController(BlogRepository blogRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getLoggedInUser(authentication);
        model.addAttribute("currentUser", user);
        model.addAttribute("blogs", blogRepository.findByAuthorOrderByCreatedAtDesc(user));
        if (!model.containsAttribute("blog")) {
            model.addAttribute("blog", new Blog());
        }
        return "dashboard";
    }

    @PostMapping("/blogs")
    public String createBlog(@Valid @ModelAttribute("blog") Blog blog,
                             BindingResult bindingResult,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("blogs", blogRepository.findByAuthorOrderByCreatedAtDesc(user));
            return "dashboard";
        }

        blog.setTitle(blog.getTitle().trim());
        blog.setContent(blog.getContent().trim());
        blog.setAuthor(user);
        blogRepository.save(blog);
        redirectAttributes.addFlashAttribute("successMessage", "Blog created successfully.");
        return "redirect:/dashboard";
    }

    @PostMapping("/blogs/{id}/delete")
    public String deleteBlog(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(authentication);
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found."));

        if (blog.getAuthor().getId().equals(user.getId())) {
            blogRepository.delete(blog);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Blog deleted successfully.");
        return "redirect:/dashboard";
    }

    private User getLoggedInUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found."));
    }
}
