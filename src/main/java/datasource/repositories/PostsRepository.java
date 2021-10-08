package datasource.repositories;

import entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

public interface PostsRepository extends JpaRepository<Post, Integer> {
}
