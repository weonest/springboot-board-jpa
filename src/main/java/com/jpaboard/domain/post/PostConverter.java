package com.jpaboard.domain.post;

import com.jpaboard.domain.post.dto.request.PostCreateRequest;
import com.jpaboard.domain.post.dto.response.PostPageResponse;
import com.jpaboard.domain.post.dto.response.PostResponse;
import com.jpaboard.domain.user.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostConverter {

    public static Post convertRequestToEntity(PostCreateRequest request, User user) {
        return Post.builder()
                .title(request.title())
                .content(request.content())
                .user(user)
                .build();
    }

    public static PostResponse convertEntityToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .create_at(post.getCreateAt())
                .update_at(post.getUpdateAt())
                .build();
    }

    public static PostPageResponse convertEntityToPageResponse(Page<PostResponse> posts) {
        return PostPageResponse.builder()
                .content(posts.getContent())
                .numberOfElements(posts.getNumberOfElements())
                .totalElements(posts.getTotalElements())
                .pageNumber(posts.getNumber())
                .pageSize(posts.getPageable().getPageSize())
                .isFirstPage(posts.isFirst())
                .hasNextPage(posts.hasNext())
                .isLastPage(posts.isLast())
                .build();

    }
}
