package io.hhplus.tdd.point.concurrency;

import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PointServiceTest {

    @Autowired
    PointService pointService;

    @Test
    @DisplayName("동시에 100개의 요청으로 포인트를 사용 한다.")
    void use_100_request() throws InterruptedException {
        // given
        final int threadCount = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        pointService.charge(1L, 9999L);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(1L, 1L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        UserPoint resultUser = pointService.lookUp(1L);

        // then
        assertEquals(9899, resultUser.point());
    }

    @Test
    @DisplayName("동시에 100개의 요청으로 포인트를 충전 한다.")
    void charge_100_request() throws InterruptedException {
        // given
        final int threadCount = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        pointService.charge(1L, 10L);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.charge(1L, 1L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        UserPoint resultUser = pointService.lookUp(1L);

        // then
        assertEquals(110L, resultUser.point());
    }
}
