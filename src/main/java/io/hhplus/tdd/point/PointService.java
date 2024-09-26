package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    private final long MAX_POINT = PointPolicy.MAX_POINT;

    private final ConcurrentHashMap<Long, Lock> locks = new ConcurrentHashMap<>();

    //    포인트 조회
    public UserPoint lookUp(long id) {
        return userPointTable.selectById(id);
    }

    //    포인트 사용
    public UserPoint use(long id, long amount) {
        Lock lock = locks.computeIfAbsent(id, key -> new ReentrantLock());
        lock.lock();
        try {
            UserPoint selectUser = userPointTable.selectById(id);
            long originPoint = selectUser.point();
            long newPoint = originPoint - amount;
            if (originPoint < amount) {
                throw new IllegalArgumentException("포인트가 부족합니다.");
            }
            UserPoint updatedUser = userPointTable.insertOrUpdate(id, newPoint);
            historyInsert(updatedUser.id(), updatedUser.point(), TransactionType.USE, System.currentTimeMillis());
            return updatedUser;
        } finally {
            lock.unlock();
        }

    }

    //    포인트 내역 조회
    public List<PointHistory> historyLookup(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    //    포인트 충전
    public UserPoint charge(long id, long amount) {
        Lock lock = locks.computeIfAbsent(id, key -> new ReentrantLock());
        lock.lock();
        try {
            UserPoint selectUser = userPointTable.selectById(id);
            long originPoint = selectUser.point();
            long newPoint = originPoint + amount;
            if (newPoint > MAX_POINT) {
                throw new IllegalArgumentException("포인트 최대 한도를 초과할 수 없습니다.");
            }
            UserPoint updatedUser = userPointTable.insertOrUpdate(id, newPoint);
            historyInsert(updatedUser.id(), updatedUser.point(), TransactionType.CHARGE, System.currentTimeMillis());
            return updatedUser;
        } finally {
            lock.unlock();
        }

    }

    //    포인트 내역 추가
    private PointHistory historyInsert(long userId, long amount, TransactionType type,  long point) {
        return pointHistoryTable.insert(userId, amount, type, point);
    }

}
