package com.chapter01;

import com.entity.User;
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
        String resource = "chapter01/mybatis-config-annotation.xml"; //读取annotation的配置文件路径
        InputStream is = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    @Test
    public void testFindAll() {
        System.out.println("========== 测试注解方式查询所有用户 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();
        //获取定义的UserMapperAnnotation接口的实现类
        UserMapperAnnotation userMapper = sqlSession.getMapper(UserMapperAnnotation.class);

        List<User> users = userMapper.findAll();
        for (User user : users) {
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
        user.setUsername("Test_annotation_add");
        user.setPassword("123456");
        user.setEmail("anno@qq.com");
        user.setCreateTime(new Date());

        int rows = userMapper.addUser(user);
        System.out.println("影响行数：" + rows);
        System.out.println("自增主键：" + user.getId());

        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testUpdateUser() {
        System.out.println("========== 测试注解方式更新用户 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapperAnnotation userMapper = sqlSession.getMapper(UserMapperAnnotation.class);

        User user = new User();
        user.setId(1);
        user.setUsername("Test_annotation_update");
        user.setPassword("123456");
        user.setEmail("anno11@qq.com");
        user.setUpdateTime(new Date());

        int rows = userMapper.updateUser(user);
        System.out.println("影响行数：" + rows);

        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testDeleteUser() {
        System.out.println("========== 测试注解方式删除用户 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapperAnnotation userMapper = sqlSession.getMapper(UserMapperAnnotation.class);

        int rows = userMapper.deleteUser(1);
        System.out.println("影响行数：" + rows);

        sqlSession.commit();
        sqlSession.close();
    }

    //根据用户名模糊查询 #{} 和 ${} 的区别
    //#{value} 是参数绑定，不会被 SQL 注入  使用的是?占位符
    //${value} 是字符串拼接，有 SQL 注入风险  '%${value}%'
    // select * from user where uname='user' and password='123456' or 1=1
    @Test
    public void testFindByUsername() {
        System.out.println("========== 测试注解方式根据用户名模糊查询 ==========");

        SqlSession sqlSession = sqlSessionFactory.openSession();

        UserMapperAnnotation userMapper = sqlSession.getMapper(UserMapperAnnotation.class);

        List<User> users = userMapper.findByUsernameLike("z");
        for (User user : users) {
            System.out.println(user);
        }



        sqlSession.close();
    }

}