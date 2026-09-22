package com.chapter02;

import com.entity.User;
import com.entity.Vo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class UserMapperAnnotationTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        String resource = "chapter02/mybatis-config.xml";
        InputStream is = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    @Test
    //编写一个测试ResultType的 非属性字段的selectAll的方法
    public void testFindAllByResultType() {
        System.out.println("========== 测试注解方式查询所有用户 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();
               List<User> users =  sqlSession.selectList("com.chapter02.UserMapper.selectAll");
        for (User user : users) {
            System.out.println(user);
        }

        sqlSession.close();
    }

    @Test
    public void testFindAll() {
        System.out.println("========== 测试注解方式查询所有用户 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapperAnnotation userMapper = sqlSession.getMapper(UserMapperAnnotation.class);

        List<User> users = userMapper.findAll();
        for (User user : users) {
            System.out.println(user);
        }

        sqlSession.close();


    }
    @Test
    public void testFindAllByVo() {
               SqlSession sqlSession = sqlSessionFactory.openSession();
        List<Vo> users =  sqlSession.selectList("com.chapter02.UserMapper.selectAllByVo");
        for (Vo user : users) {
            System.out.println(user);
        }

        sqlSession.close();


    }


    @Test
    public void testFindById() {
        System.out.println("========== 测试注解方式根据ID查询 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapperAnnotation userMapper = sqlSession.getMapper(UserMapperAnnotation.class);

        User user = userMapper.findById(1);
        System.out.println(user);

        sqlSession.close();
    }

    @Test
    public void testAddUser() {
        System.out.println("========== 测试注解方式添加用户 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapperAnnotation userMapper = sqlSession.getMapper(UserMapperAnnotation.class);

        User user = new User();
        user.setUsername("注解测试");
        user.setPassword("123456");
        user.setEmail("annotation@qq.com");
        user.setCreateTime(new Date());

        int rows = userMapper.addUser(user);
        System.out.println("影响行数：" + rows);
        System.out.println("自增主键：" + user.getId());

        sqlSession.commit();
        sqlSession.close();
    }
}