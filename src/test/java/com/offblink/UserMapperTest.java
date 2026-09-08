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
import java.util.HashMap;
import java.util.Map;

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

    // Map 多参数示例：一个 Map 装多个变量，键名必须与 XML 里 #{} 占位名一致
    @Test
    public void testFindPage() {
        System.out.println("========== 测试 Map 多参数分页查询 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 多个变量装进 Map：key 是字符串"变量名"，value 是值
            Map<String, Object> params = new HashMap<>();
            params.put("offset", 0);   // 从第几条开始（对应 XML 的 #{offset}）
            params.put("size", 3);     // 取几条（对应 XML 的 #{size}）

            List<User> users = mapper.findByPage(params);
            for (User user : users) {
                System.out.println(user);
            }
        }
    }

    // 聚合函数示例：COUNT(*) 返回单行单列的数字，代理方法返回 Integer，自动拆箱成 int
    @Test
    public void testFindCount() {
        System.out.println("========== 测试聚合函数 COUNT ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            int count = mapper.findCount();  // Integer 自动拆箱成 int
            System.out.println("用户总数：" + count);
        }
    }
    // 多聚合示例：一行多列数字，resultType="map" 接成 HashMap，key 就是 SQL 里的列别名
    @Test
    public void testFindStats() {
        System.out.println("========== 测试多聚合函数（一行多列） ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 整行统计结果是一个 Map：{cnt=..., totalId=..., maxId=...}
            Map<String, Object> stats = mapper.findStats();
            System.out.println("统计结果：" + stats);

            // 也可按别名单独取。注意：JDBC 类型映射 COUNT→Long、SUM→BigDecimal，不是 Integer
            System.out.println("用户总数 = " + stats.get("cnt") + "，id 之和 = " + stats.get("totalId"));
        }
    }



    @Test
    public void testCrudLifecycle() {
        System.out.println("========== 测试增改查删完整生命周期 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 1. 增：自增主键回填
            User user = new User();

            // 这就调用了user的本方法
            user.setUsername("测试用户");
            user.setPassword("123456");
            user.setEmail("test@qq.com");

            // 传入user对象，mapper.xml中括弧内不可乱写
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
