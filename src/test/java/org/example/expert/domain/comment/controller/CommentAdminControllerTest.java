package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { CommentAdminController.class })
class CommentAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private CommentAdminService commentAdminService;

    @Test
    void deleteComment_댓글을삭제한다() throws Exception {
        //given
        long commentId = 1L;
        doNothing().when(commentAdminService).deleteComment(commentId);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete("/admin/comments/" + commentId));
        //then
        resultActions.andDo(print())
                .andExpect(status().isOk());
    }

}