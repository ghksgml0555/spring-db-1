package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JdbcTemplate 사용
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member){
        String sql = "insert into member(member_id, money) values (?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;
        /* 위의 코드로 아래 코드가 모두 필요없어 진다. (심지어 예외변환까지) 커넥션 닫기, 동기화도 해준다.
        Connection con = null;
        //PreparedStatement를 가지고 db에 쿼리를 날린다.
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            //실행
            pstmt.executeUpdate();
            return member;
        }catch (SQLException e){
            //얘가 반환하는게 익셉션이라서 바로 throw가능
            throw exTranslator.translate("save", sql, e);
        }finally {
            close(con, pstmt, null);
        }*/
    }

    @Override
    public Member findById(String memberId) {
        //항상 ?로 파라미터를 바인딩하도록 PreparedStatement를 사용해준다.
        String sql = "select * from member where member_id = ?";
        //한건 조회할때는 queryForObject() 사용 2번째 쿼리결과를 어떻게 member로 만들지 매핑정보 넣어야한다. >만들것
        Member member = template.queryForObject(sql, memberRowMapper(), memberId);
        return member;
    }

    //이 메서드는 대략 이렇게 되는구나라고 이해 rs는 ResultSet으로 이해
    //sql결과를 member로 만들어서 반환시키는 함수
    private RowMapper<Member> memberRowMapper(){
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    @Override
    public void update(String memberId, int money) {
        String sql = " update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }

    @Override
    public void delete(String memberId)  {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }


}
