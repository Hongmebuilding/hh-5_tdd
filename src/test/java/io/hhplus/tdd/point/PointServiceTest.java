package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.exception.CustomException;
import io.hhplus.tdd.point.repository.impl.PointHistoryRepositoryImpl;
import io.hhplus.tdd.point.repository.impl.UserPointRepositoryImpl;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.PointServiceImpl;
import io.hhplus.tdd.util.TaskUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private PointService pointService;
    private UserPointRepositoryImpl userPointRepository;
    private PointHistoryRepositoryImpl pointHistoryRepository;
    private TaskUtil taskUtil;

    @BeforeEach
    public void setUp() {
        taskUtil = new TaskUtil();
        userPointRepository = new UserPointRepositoryImpl(new UserPointTable());
        pointHistoryRepository = new PointHistoryRepositoryImpl(new PointHistoryTable());
        pointService = new PointServiceImpl(userPointRepository, pointHistoryRepository, taskUtil);
    }

    @Test
    @DisplayName("동시 요청에 대한 충전 순차 처리 확인 테스트 케이스")
    void chargeConcurrentPoints() throws InterruptedException {
        // given
        long userId = 1L;
        int numThreads = 10;
        long chargingPoints = 100L;
        long expectedTotalAmount = numThreads * chargingPoints;
        CountDownLatch countDownLatch = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // when
        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                        try {
                            try {
                                pointService.chargePoints(userId, chargingPoints);
                            } catch (ExecutionException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
            );
        }
        countDownLatch.await();
        executorService.shutdown();

        // then
        final Long afterPoint = pointService.getPoints(userId).point();
        assertEquals(expectedTotalAmount, afterPoint);
    }

    @Test
    @DisplayName("동시 요청에 대한 포인트 사용 순차 처리 확인 테스트 케이스")
    void useConcurrentPoints() throws InterruptedException {
        // given
        long userId = 1L;
        long initialPoints = 1000L;
        long usingPoints = 100L;
        int numThreads = 10;
        long expectedRemainingPoints = initialPoints - numThreads * usingPoints;
        userPointRepository.insertOrUpdate(userId, initialPoints);
        CountDownLatch countDownLatch = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // when
        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    try {
                        pointService.usePoints(userId, usingPoints);
                    } catch (ExecutionException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();

        // then
        final Long afterPoint = pointService.getPoints(userId).point();
        assertEquals(expectedRemainingPoints, afterPoint);
    }


}