package io.hhplus.tdd.point;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * 포인트 컨트롤러 유닛 검사
 *
 * 유효성 검사
 *  1. 포인트 사용시 유효성 검사
 *  2. 포인트 충전시 유효성 검사
 */

@ExtendWith(MockitoExtension.class)
public class PointControllerUnitTest {

    @Mock
    PointService pointService;

    @InjectMocks
    PointController pointController;

    final long id = 1L;
    final long originPoint = 1000L;

    @BeforeEach
    void setUp() {

    }

    @Nested
    @DisplayName("유효성 검사")
    class validation {
        @Test
        @DisplayName("포인트 충전 정상 테스트")
        void testChargeValidation() {
            long point = 100;
            UserPoint mockUser = new UserPoint(id, originPoint + point, 0);

            doReturn(mockUser).when(pointService).charge(id, point);

            UserPoint resultUser = pointController.charge(id, point);

            assertEquals(mockUser.point(), resultUser.point());
        }

        @Test
        @DisplayName("포인트 충전 유효성 에러 테스트")
        void testChargeValidationError() {
            long point = -100;

            assertThrows(IllegalArgumentException.class, ()->{
                pointController.charge(id, point);
            });

        }

        @Test
        @DisplayName("포인트 사용 정상 테스트")
        void testUseValidation() {
            long point = 100;
            UserPoint mockUser = new UserPoint(id, originPoint - point, 0);

            doReturn(mockUser).when(pointService).use(id, point);

            UserPoint resultUser = pointController.use(id, point);

            assertEquals(mockUser.point(), resultUser.point());
        }

        @Test
        @DisplayName("포인트 사용 유효성 에러 테스트")
        void testUseValidationError() {
            long point = -100;

            assertThrows(IllegalArgumentException.class, ()->{
                pointController.charge(id, point);
            });

        }
    }


}
