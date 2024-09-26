package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    PointHistoryTable pointHistoryTable;

    @Mock
    UserPointTable userPointTable;

    @InjectMocks
    PointService pointService;

    long originPoint = 1000L;
    long id = 1L;

    UserPoint mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserPoint(id,originPoint,0);
        doReturn(mockUser).when(userPointTable).selectById(id);
    }

    @Nested
    @DisplayName("포인트 사용")
    class pointUsage {
        /*
    테스트 작성 이유 : 포인트 사용의 경우, 사용된 포인트 만큼 기존 포인트가 차감 되어야 하는 로직이 들어가므로 테스트 코드 구현
     */
        @Test
        @DisplayName("포인트 사용 정상 테스트")
        void testUsage() {
            long usePoint = 100L;
            UserPoint updateUser = new UserPoint(id, originPoint - usePoint, 0);

            doReturn(updateUser).when(userPointTable).insertOrUpdate(id, originPoint-usePoint);

            UserPoint resultUser = pointService.use(id, usePoint);

            assertEquals(updateUser.point(), resultUser.point());
        }

        /*
        테스트 작성 이유 : 포인트를 사용할 경우, 현재 보유 포인트 이상으로 포인트를 사용 하면 안됨
         */
        @Test
        @DisplayName("포인트 사용 한도 초과 테스트")
        void testUsageLimitExceeded() {
            long usePoint = 9999L;

            assertThrows(IllegalArgumentException.class, () -> {
                pointService.use(id, usePoint);
            });
        }
    }


    @Nested
    @DisplayName("포인트 충전")
    class pointChargeable{
        /*
    테스트 작성 이유 : 현재 포인트 + 충전 포인트 로직 검증
     */
        @Test
        @DisplayName("포인트 충전 테스트")
        void testChargeable() {
            long chargePoint = 50L;
            UserPoint updateUser = new UserPoint(id, originPoint + chargePoint, 0);

            doReturn(updateUser).when(userPointTable).insertOrUpdate(id, originPoint + chargePoint);

            UserPoint resultuser = pointService.charge(id, chargePoint);

            assertEquals(updateUser.point(), resultuser.point());
        }

        @Test
        @DisplayName("포인트 충전 한도 초과 테스트")
        void testPointChargeLimitExceeded() {
            long chargePoint = 999999999999L;

            assertThrows(IllegalArgumentException.class, ()->{
                pointService.charge(id, chargePoint);
            });
        }
    }

}