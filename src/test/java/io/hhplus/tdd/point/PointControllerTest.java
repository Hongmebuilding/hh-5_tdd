package io.hhplus.tdd.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.service.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointServiceImpl pointService;

    @InjectMocks
    private PointController pointController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("특정 유저의 포인트를 조회하는 기능")
    void point() throws Exception {
        long userId = 1L;
        long point = 2L;
        long updateMillis = 0L;
        UserPoint userPoint = new UserPoint(userId, point, updateMillis);

        when(pointService.getPoints(userId)).thenReturn(userPoint);

        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(point));
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회하는 기능")
    void history() throws Exception {
        long userId = 1L;
        List<PointHistory> historyList = List.of(
                new PointHistory(1L, userId, 1L, TransactionType.CHARGE, 1L),
                new PointHistory(2L, userId, 2L, TransactionType.USE, 2L),
                new PointHistory(3L, userId, 3L, TransactionType.CHARGE, 3L)
        );

        when(pointService.getPointHistories(userId)).thenReturn(historyList);

        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].amount").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[1].amount").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].userId").value(userId))
                .andExpect(jsonPath("$[2].amount").value(3L));
    }

    @Test
    @DisplayName("특정 유저의 포인트를 충전하는 기능")
    void charge() throws Exception {
        long userId = 1L;
        long amount = 2L;
        String content = objectMapper.writeValueAsString(amount);
        UserPoint userPoint = new UserPoint(userId, amount, 0L);

        when(pointService.chargePoints(userId, amount)).thenReturn(userPoint);

        mockMvc.perform(MockMvcRequestBuilders.patch("/point/{userId}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(amount));
    }

    @Test
    @DisplayName("특정 유저의 포인트를 사용하는 기능")
    void use() throws Exception {
        long userId = 1L;
        long amount = 1L;
        UserPoint userPoint = new UserPoint(userId, amount, 0L);
        String content = objectMapper.writeValueAsString(amount);

        when(pointService.usePoints(userId, amount)).thenReturn(userPoint);

        performPatchRequest(userId, "/use", content, amount);
    }

    private void performPatchRequest(long userId, String action, String content, long amount) throws Exception {
        mockMvc.perform(patch("/point/{id}" + action, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(amount));
    }
}
