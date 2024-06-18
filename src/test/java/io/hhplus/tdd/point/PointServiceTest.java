package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.CustomException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private PointService pointService;

    @Mock
    private UserPointRepository userPointRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(userPointRepository, pointHistoryRepository);
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

    @Test
    @DisplayName("포인트 충전 성공 테스트 케이스")
    void chargePoints() {
        // given
        long userId = 1L;
        long amount = 1000L;
        long updateMillis = 0L;
        UserPoint userPoint = new UserPoint(userId, amount, updateMillis);
        when(userPointRepository.insertOrUpdate(userId, amount)).thenReturn(userPoint);

        // when
        UserPoint realPoints = pointService.chargePoints(userId, amount);

        // then
        assertEquals(userPoint, realPoints);
        verify(userPointRepository, times(1)).insertOrUpdate(userId, amount);
    }

    @Test
    @DisplayName("잔고 부족 시 포인트 사용 실패 처리")
    void usePoints_noMoney() {
        // given
        long userId = 1L;
        long havingPoint = 700L;
        long usingPoint = 701L;
        long updateMillis = 0L;
        UserPoint userPoint = new UserPoint(userId, havingPoint, updateMillis);

        // when
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);

        // then
        assertThrows(CustomException.class, () -> {
            pointService.usePoints(userId, usingPoint);
        });
    }

    @Test
    @DisplayName("잔고 충분 시 포인트 사용 성공 테스트 케이스")
    void usePoints_enoughMoney() {
        // given
        long userId = 1L;
        long usingPoint = 699L;
        UserPoint currentUserPoint = new UserPoint(1L, 700L, 0L);
        UserPoint remainUserPoint = new UserPoint(1L, 1L, System.currentTimeMillis());

        // when
        when(userPointRepository.selectById(userId)).thenReturn(currentUserPoint);
        UserPoint userPoint = pointService.usePoints(userId, usingPoint);

        // then
        assertEquals(userPoint.point(), remainUserPoint.point());
    }

    @Test
    @DisplayName("포인트 사용 내역이 정확히 기록되고 조회되는지 확인하는 테스트 케이스")
    void chargePoints_success() {
        // given
        // when
        // then
    }
}