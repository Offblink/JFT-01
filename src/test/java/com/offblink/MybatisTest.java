package com.offblink;

import com.offblink.entity.User;
import com.offblink.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * MyBatis 首个测试：验证配置加载、Mapper 代理与 SQL 执行链路
 */
public class MybatisTest {

    @Test
    public void testFindAll() throws IOException {
        // 1. 加载全局配置文件
        InputStream is = Resources.getResourceAsStream("mybatis-config.xml");

        // 2. 构建 SqlSessionFactory（全局唯一）
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);

        // 3. 开启 SqlSession（非线程安全，用完必须关闭）
        SqlSession session = factory.openSession();

        // 4. 获取 Mapper 动态代理对象
        UserMapper userMapper = session.getMapper(UserMapper.class);

        // 5. 执行查询
        List<User> users = userMapper.findAll();
        for (User user : users) {
            System.out.println(user);
        }

        // 6. 关闭资源
        session.close();
        is.close();
    }

    @Test
    public void testFindById() throws IOException {
        InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
        try (SqlSession session = factory.openSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);
            User user = userMapper.findById(1);
            System.out.println(user);
        }
        is.close();
    }
}
