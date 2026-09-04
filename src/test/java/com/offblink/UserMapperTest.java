package com.offblink;

import com.offblink.entity.User;
import com.offblink.mapper.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * MyBatis CRUD 测试
 * 对每个方法和生命周期进行测试
 */
public class UserMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    // 首先执行init方法初始化
    @Before
    public void init() throws Exception {
        // 首先读入主配置文件
        InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    @Test
    public void testFindAll() {
        System.out.println("========== 测试查询所有用户 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 两种调用方式：getMapper 代理（推荐，类型安全） vs 字符串直调（无类型检查）
            List<User> users = session.selectList("com.offblink.mapper.UserMapper.findAll");
            for (User user : users) {
                System.out.println(user);
            }
        }
    }

    // 以此方法为例
    @Test
    public void testFindByUsernameLike() {
        System.out.println("========== 测试用户名模糊查询 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {

            // 运行时造类（代理）
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 将UserMapper对象调用方法的结果，赋给用户列表变量，并逐一输出
            List<User> users = mapper.findByUsernameLike("zhang");
            for (User user : users) {
                System.out.println(user);
            }
        }
    }

    @Test
    public void testCrudLifecycle() {
        System.out.println("========== 测试增改查删完整生命周期 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 1. 增：自增主键回填
            User user = new User();
            user.setUsername("测试用户");
            user.setPassword("123456");
            user.setEmail("test@qq.com");
            int rows = mapper.addUser(user);
            System.out.println("新增影响行数：" + rows + "，回填主键 id = " + user.getId());

            // 2. 改：使用刚插入的 id（不硬编码，避免测试顺序依赖）
            user.setUsername("更新后的用户名");
            user.setEmail("update@qq.com");
            rows = mapper.updateUser(user);
            System.out.println("更新影响行数：" + rows);

            // 3. 查：确认更新生效
            User updated = mapper.findById(user.getId());
            System.out.println("更新后查询：" + updated);

            // 4. 删：清理测试数据
            rows = mapper.deleteUser(user.getId());
            System.out.println("删除影响行数：" + rows);

            session.commit();  // 增删改必须提交事务，否则不落库
        }
    }
}
