package com.chapter01;

import com.entity.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserMapperTest {

    private SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;

    @Before
    public void init() throws Exception {
        InputStream is = Resources.getResourceAsStream("chapter01/mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    @After
    public void destroy() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    // ==================== XML方式的SQL查询 ====================

    @Test
    public void testFindAllByXml() {
        System.out.println("========== 测试查询所有用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        List<User> users = sqlSession.selectList("com.chapter01.UserMapper.findAllByxml");
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testFindByIdByXml() {
        System.out.println("========== 测试根据ID查询用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        User user = sqlSession.selectOne("com.chapter01.UserMapper.findByIdXML", 7);
        System.out.println(user);
    }

    /**
     * 测试查询聚合函数
     */
    @Test
    public void testFindCountByXml() {
        System.out.println("========== 测试查询聚合函数 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        int count = sqlSession.selectOne("com.chapter01.UserMapper.findCount");
        System.out.println(count);
    }




    @Test
    public void testFindByUsernameByXml() {
        System.out.println("========== 测试根据姓名查询用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        List<User> users = sqlSession.selectList("com.chapter01.UserMapper.findByUsernameXML", "123456");
        System.out.println(users);
    }

    @Test
    public void testInsertByXml() {
        System.out.println("========== 测试添加用户 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();
        User user = new User();
        user.setUsername("TomCat");
        user.setPassword("123456");
        user.setEmail("test1234@qq.com");
        int rows = sqlSession.insert("com.chapter01.UserMapper.addUserxml", user);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
    }


    @Test
    public void testFindByNameAndPass() {
        System.out.println("========== 测试多条件查询 (XML方式) ==========");
        sqlSession = sqlSessionFactory.openSession();

        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("pass111");

        List<User> users = sqlSession.selectList("com.chapter01.UserMapper.findByNameAndPass", user);
        System.out.println(users);
    }

    @Test
    public void testFindByMap() {
        System.out.println("========== 测试Map多条件查询 ==========");
        sqlSession = sqlSessionFactory.openSession();
        Map<String, Object> map = new HashMap<>();
        map.put("abc", "123456");
        map.put("username", "lisi");
        List<User> users = sqlSession.selectList("com.chapter01.UserMapper.findByMap", map);
        System.out.println(users);
    }

    // ==================== 接口方式的测试方法 ====================

    @Test
    public void testFindAll() {
        System.out.println("========== 测试查询所有用户 ==========");
        sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        List<User> users = userMapper.findAll();
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void testFindById() {
        System.out.println("========== 测试根据ID查询用户 ==========");
        sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.findById(1);
        System.out.println(user);
    }

    @Test
    public void testAddUser() {
        System.out.println("========== 测试添加用户 ==========");
        sqlSession = sqlSessionFactory.openSession();
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
    }

    @Test
    public void testUpdateUser() {
        System.out.println("========== 测试更新用户 ==========");
        sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.findById(1);
        user.setUsername("更新后的用户名");
        user.setEmail("update@qq.com");
        user.setUpdateTime(new Date());
        int rows = userMapper.updateUser(user);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
    }

    @Test
    public void testDeleteUser() {
        System.out.println("========== 测试删除用户 ==========");
        sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int rows = userMapper.deleteUser(1);
        System.out.println("影响行数：" + rows);
        sqlSession.commit();
    }

    @Test
    public void testFindByUsernameLike() {
        System.out.println("========== 测试根据用户名模糊查询 ==========");
        sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        List<User> users = userMapper.findByUsernameLike("z");
        for (User user : users) {
            System.out.println(user);
        }
    }
}