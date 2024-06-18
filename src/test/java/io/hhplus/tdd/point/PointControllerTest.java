package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.when;

@WebMvcTest(PointController.class)
class PointControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private PointService pointService;

    @InjectMocks
    private PointController pointController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(pointController).build();
    }

    @Test
    @DisplayName("특정 유저의 포인트를 조회하는 기능")
    void point() throws Exception {
        // given
        long userId = 1L;
        long point = 2L;
        long updateMillis = 0L;
        UserPoint userPoint = new UserPoint(userId, point, updateMillis);

        // when
        when(pointService.getPoints(userId)).thenReturn(userPoint);

        // then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회하는 기능")
    void history() throws Exception {
        // given
        long userId = 1L;
        List<PointHistory> historyList = new ArrayList<>();
        historyList.add(new PointHistory(1L, userId, 1L, TransactionType.CHARGE, 1L));
        historyList.add(new PointHistory(2L, userId, 2L, TransactionType.USE, 2L));
        historyList.add(new PointHistory(3L, userId, 3L, TransactionType.CHARGE, 3L));

        // when
        when(pointService.getPointHistories(userId)).thenReturn(historyList);

        // then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 유저의 포인트를 충전하는 기능")
    void charge() throws Exception {
        // given
        long userId = 1L;
        long amount = 2L;
        long updateMillis = 0L;
        UserPoint userPoint = new UserPoint(userId, amount, updateMillis);
        String content = objectMapper.writeValueAsString(amount);

        // when
        when(pointService.chargePoints(userId, amount)).thenReturn(userPoint);

        // then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 유저의 포인트를 사용하는 기능")
    void use() throws Exception {
        // given
        long userId = 1L;
        long amount = 1L;
        long updateMillis = 0L;
        UserPoint userPoint = new UserPoint(userId, amount, updateMillis);
        String content = objectMapper.writeValueAsString(amount);

        // when
        when(pointService.usePoints(userId, amount)).thenReturn(userPoint);

        // then
        mockMvc.perform(patch("/point/{id}/use", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        ).andExpect(status().isOk());
    }
}
