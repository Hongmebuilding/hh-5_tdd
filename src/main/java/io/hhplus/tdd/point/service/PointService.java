package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.aggregate.entity.PointHistory;
import io.hhplus.tdd.point.aggregate.entity.UserPoint;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface PointService {
    UserPoint getPoints(long userId);

    List<PointHistory> getPointHistories(long userId);

    UserPoint chargePoints(long userId, long amount) throws ExecutionException, InterruptedException;

    UserPoint usePoints(long userId, long amount) throws ExecutionException, InterruptedException;
}
