package test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MybatisTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            String resource = "mybatisConfig.xml";
            InputStream inputStream;
            inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession session = sqlSessionFactory.openSession();
            System.out.println(session.selectList("Hogehoge.select"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}