package io.hhplus.tdd.point;

import io.hhplus.tdd.point.exception.CustomException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ReentrantLock lock = new ReentrantLock();

    public UserPoint getPoints(long userId) {
        return userPointRepository.selectById(userId);
    }

    public List<PointHistory> getPointHistories(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }

    public UserPoint chargePoints(long userId, long amount) {
        lock.lock();
        try{
            UserPoint userPoint = getPoints(userId);
            userPoint = userPointRepository.insertOrUpdate(userId, userPoint.point() + amount);
            pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
            return userPoint;
        }finally {
            lock.unlock();
        }
    }

    public UserPoint usePoints(long userId, long amount) {
        long currentBalance = userPointRepository.selectById(userId).point();
        if(currentBalance < amount) {
            throw new CustomException("잔고 부족!");
        }
        UserPoint remainUserPoint = userPointRepository.insertOrUpdate(userId, currentBalance - amount);
        if(remainUserPoint == null) throw new CustomException("사용 실패");
        pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return remainUserPoint;
    }
}
