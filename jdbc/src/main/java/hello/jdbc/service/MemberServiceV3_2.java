package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {

    //private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        //TransactionTemplate을 사용하려면 PlatformTransactionManager가 필요
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }


    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        /*executeWithoutResult안에서 트랜잭션을 시작하고 그다음에 비즈니스로직을 수행하고
        executeWithoutResult가 끝났을때 비즈니스로직이 성공적으로 반환된다면 커밋, 예외시 롤백을 한다
        라고 일단 이해하기
         */
        txTemplate.executeWithoutResult((status) -> {
            //비즈니스로직
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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
