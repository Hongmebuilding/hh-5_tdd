package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;
import io.hhplus.tdd.point.exception.CustomException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.util.TaskUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final TaskUtil taskUtil;

    @Override
    public UserPoint getPoints(long userId) {
        return userPointRepository.selectById(userId);
    }

    @Override
    public List<PointHistory> getPointHistories(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }

    @Override
    public UserPoint chargePoints(long userId, long amount) throws ExecutionException, InterruptedException {
        Future<?> task = taskUtil.executeTask(userId, () -> {
            this.userPointRepository.insertOrUpdate(userId,amount + userPointRepository.selectById(userId).point());
            this.pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        });
        task.get();
        return this.userPointRepository.selectById(userId);
    }

    @Override
    public UserPoint usePoints(long userId, long amount) throws ExecutionException, InterruptedException {
        Future<?> task = taskUtil.executeTask(userId, () -> {
            long currentBalance = userPointRepository.selectById(userId).point();
            if(currentBalance < amount) {
                throw new CustomException("잔고 부족!");
            }
            UserPoint remainUserPoint = userPointRepository.insertOrUpdate(userId, currentBalance - amount);
            if(remainUserPoint == null) throw new CustomException("사용 실패");
            pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
        });
        task.get();
        return this.userPointRepository.selectById(userId);
    }
}
