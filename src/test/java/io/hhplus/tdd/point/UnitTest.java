package io.hhplus.tdd.point;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.PointServiceImpl;
import io.hhplus.tdd.util.TaskUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UnitTest {

    private PointService pointService;

    @Mock
    private UserPointRepository userPointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    private TaskUtil taskUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pointService = new PointServiceImpl(userPointRepository, pointHistoryRepository, taskUtil);
    }

    @Test
    void getPoints() {
        // given
        long userId = 1L;
        long point = 700L;
        long updateMillis = 0L;
        UserPoint userPoint = new UserPoint(userId, point, updateMillis);
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        // when
        UserPoint realPoints = pointService.getPoints(userId);

        // then
        assertEquals(userPoint, realPoints);
        verify(userPointRepository, times(1)).selectById(userId);
    }

    @Test
    @DisplayName("포인트 사용 내역이 정확히 기록되고 조회되는지 확인하는 테스트 케이스")
    void getPointHistories() {
        // given
        long userId = 1L;
        long updateMillis = 0L;
        List<PointHistory> pointHistories = List.of(
                new PointHistory(1L, userId, 100L, TransactionType.CHARGE, updateMillis),
                new PointHistory(1L, userId, -100L, TransactionType.USE, updateMillis)
        );
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistories);

        // when
        List<PointHistory> realPointHistories = pointService.getPointHistories(userId);

        // then
        assertEquals(pointHistories, realPointHistories);
        verify(pointHistoryRepository, times(1)).selectAllByUserId(userId);
    }
}
