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
 * 要点：@Before 复用 SqlSessionFactory；查询不需要 commit，增删改必须 commit
 */
public class UserMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
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

    @Test
    public void testFindByUsernameLike() {
        System.out.println("========== 测试用户名模糊查询 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
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
