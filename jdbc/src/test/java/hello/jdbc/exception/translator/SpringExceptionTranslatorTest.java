package hello.jdbc.exception.translator;

import com.zaxxer.hikari.util.DriverDataSource;
import hello.jdbc.connection.ConnectionConst;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init(){
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode(){
        //문법이 잘못된 sql
        String sql = "select bad grammer";

        try{
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e){
            //h2 db에서 sql 문법이 잘못됐을시 에러코드 > 42122 (김영한도 에러코드 다 못외움)
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);
            int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            log.info("error", e);
        }
    }

    @Test
    void exceptionTranslator() {
        String sql = "select bad grammer";

        try{
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        }catch (SQLException e){
            Assertions.assertThat(e.getErrorCode()).isEqualTo(42122);

            SQLErrorCodeSQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);

            DataAccessException resultEx = exTranslator.translate("select", sql, e);
            log.info("resultEx", resultEx);
            Assertions.assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
        }
    }
}
