package studentapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import studentapp.model.Blog;
import studentapp.model.User;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    List<Blog> findByAuthorOrderByCreatedAtDesc(User author);
}
