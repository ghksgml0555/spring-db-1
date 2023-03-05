package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

    //jdbc관련을 가져다 쓰는게 문제(datasource)
    //private final DataSource dataSource; > PlatformTransactionManager를 주입받는다.
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작
        //Connection con = dataSource.getConnection();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{
            //con.setAutoCommit(false); //트랜잭션 시작 > 위에서 이미 시작됨
            //비즈니스 로직
            bizLogic(fromId, toId, money);
            //정상 수행됐다면 커밋
            transactionManager.commit(status);
        }catch (Exception e){
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }/*finally {
                release(con);
        }트랜잭션매니저는 커밋, 롤백시에 릴리즈를 알아서 해준다.*/
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney()- money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney()+ money);
    }

    private void release(Connection con) {
        if(con!=null) {
            try {
                con.setAutoCommit(true);//보통 오토커밋모드이기 때문에 돌려놓고
                con.close(); //커넥션 풀을 사용하면 close시 풀로 돌아간다.
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
