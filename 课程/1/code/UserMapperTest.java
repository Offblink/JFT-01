package com.chapter01;


import com.entity.User;
import com.chapter01.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class UserMapperTest { //创建一个测试类
    /*
    * mybatis中的核心类
    * SqlSessionFactory：数据库连接工厂
    * SqlSession：数据库连接session 类似connection对象
    * */

    private SqlSessionFactory sqlSessionFactory;  //定义一个Mybatis的连接工厂

    @Before  // 这是一个注解的书写 在测试方法执行前执行
    public void init() throws Exception {
        InputStream is = Resources.getResourceAsStream("chapter01/mybatis-config.xml");
        //读取当前项目的数据库的连接文件mybatis-config.xml
        //该文件中包含数据库的连接和该项目的执行sqlMapper文件
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
        //创建一个Mybatis的连接工厂
    }

    @Test //测试方法的注解
    public void testFindAll() {
        System.out.println("========== 测试查询所有用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession(); //打开一个数据库连接session 等同于connection

        //注解方式的sql方法
        //UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
       // List<User> users = userMapper.findAll();
        

        //直接读取Mapper文件中的xml声明的sql方法
        List<User> users =sqlSession.selectList("com.chapter01.UserMapper.findAllByxml");

        for (User user : users) {
            System.out.println(user);
        }
        sqlSession.close();
    }

    @Test
    public void testFindByName() {
        System.out.println("========== 测试根据用户名查询 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        User testUser = sqlSession.selectOne("com.chapter01.UserMapper.testfind");
        System.out.println(testUser);
        sqlSession.close();
    }

    @Test
    public void testFindById() {
        System.out.println("========== 测试根据ID查询用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.findById(1);
        System.out.println(user);
        sqlSession.close();
    }

    @Test
    public void testAddUser() {
        System.out.println("========== 测试添加用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = new User();
        user.setUsername("测试用户");
        user.setPassword("123456");
        user.setEmail("test@qq.com");
        user.setCreateTime(new Date());
        int rows = userMapper.addUser(user);
        System.out.println("影响行数：" + rows);
        System.out.println("自增主键：" + user.getId());
        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testUpdateUser() {
        System.out.println("========== 测试更新用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.findById(1);
        user.setUsername("更新后的用户名");
        user.setEmail("update@qq.com");
        user.setUpdateTime(new Date());
        int rows = userMapper.updateUser(user);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testDeleteUser() {
        System.out.println("========== 测试删除用户 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int rows = userMapper.deleteUser(1);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testFindByUsernameLike() {
        System.out.println("========== 测试根据用户名模糊查询 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        List<User> users = userMapper.findByUsernameLike("测");
        for (User user : users) {
            System.out.println(user);
        }
        sqlSession.close();
    }
}