package com.jurado.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:5173") // Match your frontend URL here
public class PostController {

    @Autowired
    private PostRepository postRepository;

    // GET all posts
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return ResponseEntity.ok(posts);
    }

    // GET post by ID
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST create a single post (author required)
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        try {
            System.out.println("Received post: " + post);

            if (post.getAuthor() == null || post.getAuthor().trim().isEmpty()) {
                System.out.println("Author is missing or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Author field is required");
            }

            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                System.out.println("Content is missing or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Content field is required");
            }

            // Save post; createdAt is set automatically in entity via @PrePersist
            Post savedPost = postRepository.save(post);

            System.out.println("Saved post with ID: " + savedPost.getId());
            return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating post: " + e.getMessage());
        }
    }

    // PUT update a post by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Post updatedPost) {
        try {
            return postRepository.findById(id)
                    .map(existingPost -> {
                        existingPost.setContent(updatedPost.getContent());
                        existingPost.setImageUrl(updatedPost.getImageUrl());
                        Post savedPost = postRepository.save(existingPost);
                        return ResponseEntity.ok(savedPost);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating post: " + e.getMessage());
        }
    }

    // DELETE a post by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
