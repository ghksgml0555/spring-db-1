package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    public Member save(Member member){
        String sql = "insert into member(member_id, money) values (?, ?)";

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
        }
    }

    @Override
    public Member findById(String memberId) {
        //항상 ?로 파라미터를 바인딩하도록 PreparedStatement를 사용해준다.
        String sql = "select * from member where member_id = ?";

        //try-catch의 finally에서 con을 호출해야 하기때문에 밖에 선언
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            //executeUpdate는 데이터를 변경할 때 사용 select는 executeQuery
            //executeQuery()는 ResultSet을 반환(셀렉트의 결과를 담고있음)
            rs = pstmt.executeQuery();
            //rs.next()를 한번 해줘야 실제 데이터가 있는곳부터 시작한다.
            //rs.next()를했을때 데이터가 있으면 true없으면 false
            if(rs.next()){
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
        }finally {
            close(con,pstmt,rs);
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = " update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            //실행
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        }catch (SQLException e){
            throw exTranslator.translate("save", sql, e);
        }finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public void delete(String memberId)  {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            //실행
            pstmt.executeUpdate();
        }catch (SQLException e){
            throw exTranslator.translate("save", sql, e);
        }finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs){

        //JdbcUtils의 편리기능
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con, dataSource);
        //JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
