package com.jpaboard.domain.post.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpaboard.domain.post.application.PostService;
import com.jpaboard.domain.post.dto.request.PostCreateRequest;
import com.jpaboard.domain.post.dto.request.PostSearchRequest;
import com.jpaboard.domain.post.dto.request.PostUpdateRequest;
import com.jpaboard.domain.user.application.UserService;
import com.jpaboard.domain.user.dto.request.UserCreationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
class PostControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    Long userId;

    Long postId;

    @BeforeEach
    void setUp() {
        userId = userService.createUser(new UserCreationRequest("tester", 25, "축구"));
        postId = postService.createPost(new PostCreateRequest(userId, "테스트 제목1", "테스트 본문1"));
        postService.createPost(new PostCreateRequest(userId, "테스트 제목2", "테스트 본문2"));
        postService.createPost(new PostCreateRequest(userId, "테스트 제목3", "테스트 본문3"));
    }

    @Test
    void post_create_test() throws Exception {
        PostCreateRequest request = new PostCreateRequest(userId, "게시글 생성 테스트", "테스트 본문");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("post-create",

                        requestFields(
                                fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저 Id"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("본문")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("리다이렉션 URI")
                        )));
    }

    @ParameterizedTest
    @CsvSource(value = {"제목1, null, null", "null, 본문2, null", "null, null, 테스트", "null, null, null"}, nullValues = "null")
    void find_by_condition_test(String title, String content, String keyword) throws Exception {
        LinkedMultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("title", title);
        paramMap.add("content", content);
        paramMap.add("keyword", keyword);

        PostSearchRequest request = new PostSearchRequest(title, content, keyword);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/posts")
                        .params(paramMap)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("post-find",
                        pathParameters(
                                parameterWithName("title").description("제목").optional(),
                                parameterWithName("content").description("본문").optional(),
                                parameterWithName("keyword").description("제목 + 본문").optional()
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목").optional(),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("본문").optional(),
                                fieldWithPath("keyword").type(JsonFieldType.STRING).description("제목 + 본문").optional()
                        ),
                        responseFields(
                                fieldWithPath("content.[].id").type(JsonFieldType.NUMBER).description("게시글 Id"),
                                fieldWithPath("content.[].name").type(JsonFieldType.STRING).description("작성자"),
                                fieldWithPath("content.[].title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content.[].content").type(JsonFieldType.STRING).description("본문"),
                                fieldWithPath("content.[].createAt").type(JsonFieldType.STRING).description("생성일"),
                                fieldWithPath("content.[].updateAt").type(JsonFieldType.STRING).description("수정일"),

                                fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER).description("전체 데이터 개수"),
                                fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("전체 요소 개수"),
                                fieldWithPath("pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("pageSize").type(JsonFieldType.NUMBER).description("한 페이지당 조회할 데이터 개수"),
                                fieldWithPath("isFirstPage").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지인지 여부"),
                                fieldWithPath("hasNextPage").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                                fieldWithPath("isLastPage").type(JsonFieldType.BOOLEAN).description("마지막 페이지인지 여부")
                        )));
    }

    @Test
    void post_update_test() throws Exception {
        PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 본문");

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("post-update", pathParameters(
                                parameterWithName("id").description("게시글 Id")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("본문")
                        )));
    }

    @Test
    void post_delete_test() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("post-delete", pathParameters(
                        parameterWithName("id").description("게시글 Id")
                )));
    }

}
