package io.hhplus.tdd.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PointConcurrencyTest {

    @Autowired
    PointService pointService;

    @BeforeEach
    public void setUp() {
    }

    /**
     * 포인트 사용 요청이 동시에 여러번 들어 왔을때, 누락 되는 요청 없이 처리 되는지 테스트
     *
     * @throws InterruptedException
     */
    @Test
    @DisplayName("동시에 한 아이디에 포인트 사용 요청이 왔을때 전부 정상적으로 처리 된다.")
    void use_100_request() throws InterruptedException {
        long id = 1L;

        // given
        final int threadCount = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(25);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        pointService.charge(id, 9999L);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(id, 1L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        UserPoint resultUser = pointService.lookUp(id);

        // then
        assertEquals(9899, resultUser.point());
    }

    /**
     * 포인트 충전 요청이 동시에 여러번 들어 왔을때, 누락 되는 요청 없이 처리 되는지 테스트
     * @throws InterruptedException
     */

    @Test
    @DisplayName("동시에 한 아이디에 포인트 사용 요청이 왔을때 전부 정상적으로 처리 된다.")
    void charge_100_request() throws InterruptedException {
        long id = 2L;

        final int threadCount = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(25);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        pointService.charge(id, 10L);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.charge(id, 1L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        UserPoint resultUser = pointService.lookUp(id);

        // then
        assertEquals(110L, resultUser.point());
    }

    @Test
    @DisplayName("한 아이디에 충전과 사용이 여러 번 요청되었을 때, 정상적으로 처리되어야 한다.")
    void sequential_charge_and_use_test() {
        long id = 3L;

        long[] points = {500L, 100L, 200L, 20L, 30L, 1000L};
        long[] amounts = {500L, 400L, 600L, 580L, 550L, 1550L};

        List<Runnable> tasks = Arrays.asList(
                () -> pointService.charge(id, points[0]),
                () -> pointService.use(id, points[1]),
                () -> pointService.charge(id, points[2]),
                () -> pointService.use(id, points[3]),
                () -> pointService.use(id, points[4]),
                () -> pointService.charge(id, points[5])
        );

        for (Runnable task : tasks) {
            task.run();
        }

        List<PointHistory> history = pointService.historyLookup(id);
        System.out.println(history);
        for (int i = 0; i < history.size(); i++) {
            assertEquals(amounts[i], history.get(i).amount());
        }
    }

}
