package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw() {
        Service service = new Service();
        Assertions.assertThatThrownBy(()->service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }

    //익셉션을 상속받으면 체크드 예외가 된다.
    static class MyCheckedException extends Exception{
        public MyCheckedException(String message){
            super(message);
        }
    }

    /**
     * Checked 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘중 하나를 필수로 선택해야 한다.
     */
    static class Service{
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch(){
            //올라온 예외를 처리하거나 던져야하는데 이걸 컴파일러가 체크해준다 > 체크예외
            try {
                repository.call();
            } catch (MyCheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 체크 예외를 잡지않고 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야한다.
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }
    static class Repository{
        //체크예외는 잡거나 던져야하는데 던질거면 throws MyCheckedException처럼 선언해줘야한다.
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }

}