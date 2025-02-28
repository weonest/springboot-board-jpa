package com.jpaboard.domain.post.application;

import com.jpaboard.domain.post.Post;
import com.jpaboard.domain.post.PostConverter;
import com.jpaboard.domain.post.dto.request.PostCreateRequest;
import com.jpaboard.domain.post.dto.request.PostSearchRequest;
import com.jpaboard.domain.post.dto.request.PostUpdateRequest;
import com.jpaboard.domain.post.dto.response.PostPageResponse;
import com.jpaboard.domain.post.dto.response.PostResponse;
import com.jpaboard.domain.post.infrastructure.PostRepository;
import com.jpaboard.domain.user.User;
import com.jpaboard.domain.user.application.UserServiceImpl;
import com.jpaboard.domain.user.infrastructure.UserRepository;
import com.jpaboard.global.exception.PostNotFoundException;
import com.jpaboard.global.exception.UserNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createPost(PostCreateRequest request) {
        User user = userRepository.findById(request.userId()).orElseThrow(UserNotFoundException::new);
        Post post = PostConverter.convertRequestToEntity(request, user);
        return postRepository.save(post).getId();
    }

    public PostResponse findPostById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(PostNotFoundException::new);
        return PostConverter.convertEntityToResponse(post);
    }

    public PostPageResponse findPosts(Pageable pageable) {
        Page<PostResponse> posts = postRepository.findAll(pageable)
                .map(PostConverter::convertEntityToResponse);
        return PostConverter.convertEntityToPageResponse(posts);
    }

    public PostPageResponse findPostsByCondition(PostSearchRequest request, Pageable pageable) {
        Page<PostResponse> posts = postRepository.findAllByCondition(request.title(), request.content(), request.keyword(), pageable)
                .map(PostConverter::convertEntityToResponse);
        return PostConverter.convertEntityToPageResponse(posts);
    }

    @Transactional
    public void updatePost(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id).orElseThrow(PostNotFoundException::new);
        post.update(request.title(), request.content());
    }

    @Transactional
    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

}
