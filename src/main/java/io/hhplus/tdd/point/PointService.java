package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.CustomException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public UserPoint getPoints(long userId) {
        return userPointRepository.selectById(userId);
    }

    public List<PointHistory> getPointHistories(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }

    public UserPoint chargePoints(long userId, long amount) {
        return userPointRepository.insertOrUpdate(userId, amount);
    }

    public UserPoint usePoints(long userId, long amount) {
        long currentBalance = userPointRepository.selectById(userId).point();
        if(currentBalance < amount) {
            throw new CustomException("잔고 부족!");
        }
        long remainingPoints = currentBalance - amount;
        UserPoint remainUserPoint = userPointRepository.insertOrUpdate(userId, remainingPoints);
        pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return remainUserPoint;
    }
}
