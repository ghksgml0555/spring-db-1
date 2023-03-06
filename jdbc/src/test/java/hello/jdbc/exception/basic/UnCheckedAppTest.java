package hello.jdbc.exception.basic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

public class UnCheckedAppTest {

    @Test
    void unchecked(){
        Controller controller = new Controller();
        Assertions.assertThatThrownBy(()->controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller{
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service{
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic(){
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient{
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }
    static class Repository{
        public void call(){
            try {
                runSQL();
            } catch (SQLException e) {
                //체크예외를 잡아서 런타임예외로 바꿔서 던진다.
                //새로운 예외를 던질때 기존 예외를 던져주면 기존예외의 스택트레이스도 확인가능
                throw new RuntimeSQLException(e);
            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    static class RuntimeConnectException extends RuntimeException{
        public RuntimeConnectException(String message){
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException{
        //생성자를 이렇게 만들면 이전예외를 같이 넣을 수 있다.
        public RuntimeSQLException(Throwable cause){
            super(cause);
        }
    }
}
