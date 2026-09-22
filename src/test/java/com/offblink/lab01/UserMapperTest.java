package com.offblink.lab01;

import com.offblink.entity.User;
import com.offblink.entity.Vo;
import com.offblink.lab01.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
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
            List<User> users = session.selectList("com.offblink.lab01.UserMapper.findAll");
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



    // ==================== 第 4 节：resultType 的非属性字段 vs resultMap ====================

    // ① resultType 自动映射的前提是「列名或别名 = 属性名」。
    //    这里 SQL 把 id 起了别名 uid，User 里没有 uid 属性 → 这列没人接手，查询结果 id 全为 null。
    @Test
    public void testFindAllWithColumnAlias() {
        System.out.println("========== ① resultType：列别名 uid 对不上属性名 id ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            List<User> users = mapper.findAllWithColumnAlias();
            System.out.println("命中 " + users.size() + " 条，第一条：" + users.get(0));
            System.out.println("id = " + users.get(0).getId()
                    + (users.get(0).getId() == null
                       ? "  ← 已丢字段（别名 uid 在 User 里找不到同名属性），resultType 只认名字对不对得上"
                       : "  ← 意外：竟然映射上了"));
        }
    }

    // ② 同一份数据、同样的思路，改用 VO：把别名起成 VO 的属性名 p1/p2/p3，自动映射就能对上
    @Test
    public void testFindAllByVoWithResultType() {
        System.out.println("========== ② resultType + VO：靠列别名对上属性名 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            List<Vo> list = mapper.findAllByVoWithResultType();
            System.out.println("命中 " + list.size() + " 条");
            for (Vo vo : list) {
                System.out.println(vo);
            }
        }
    }

    // ③ 还是 VO，但换成 resultMap：SQL 里列名原样写（SELECT *），映射关系写在 XML 的 userVoMap 里
    @Test
    public void testFindAllByVoMap() {
        System.out.println("========== ③ resultMap + VO：映射规则写在 resultMap 里 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            List<Vo> list = mapper.findAllByVoMap();
            System.out.println("命中 " + list.size() + " 条");
            for (Vo vo : list) {
                System.out.println(vo);
            }
            System.out.println("（与 ② 的结果一致：同一个 VO，一个靠别名、一个靠 resultMap）");
        }
    }

    // ==================== 第 4-2 节：动态 SQL（6 种标签 + 片段复用） ====================

    // 1. <if> + <where>：条件按参数值拼装；全空时 WHERE 自动消失（日志里看 Preparing 行对比）
    @Test
    public void testDynamicIfWhere() {
        System.out.println("========== 动态 SQL 1：<if> + <where> ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            User q1 = new User();
            q1.setUsername("zhang");
            System.out.println("--- 只按 username 查 ---");
            mapper.findUsersByCondition(q1).forEach(System.out::println);

            User q2 = new User();
            q2.setUsername("zhang");
            q2.setEmail("zhangsan@example.com");
            System.out.println("--- username + email 两个条件 ---");
            mapper.findUsersByCondition(q2).forEach(System.out::println);

            System.out.println("--- 一个条件都不给 → <where> 连 WHERE 一起省掉 ---");
            System.out.println("命中 " + mapper.findUsersByCondition(new User()).size() + " 条");
        }
    }

    // 2. <set>：只更新非空字段，自动去掉末尾逗号；用临时用户做实验，不碰种子数据
    @Test
    public void testDynamicSet() {
        System.out.println("========== 动态 SQL 2：<set> 选择性更新 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            User temp = new User();
            temp.setUsername("set测试用户");
            temp.setPassword("old_pass");
            temp.setEmail("set@offblink.test");
            mapper.addUser(temp);

            // 只给 password：username / email 不会出现在 <set> 里，数据库里保持原值
            User patch = new User();
            patch.setId(temp.getId());
            patch.setPassword("new_pass");
            System.out.println("更新影响行数：" + mapper.updateUserSelective(patch));

            User after = mapper.findById(temp.getId());
            System.out.println("更新后：" + after);
            System.out.println("password 已改 = " + "new_pass".equals(after.getPassword())
                    + "，username 未被动 = " + "set测试用户".equals(after.getUsername()));

            mapper.deleteUser(temp.getId());   // 清场
            session.commit();
        }
    }

    // 3. <choose>/<when>/<otherwise>：只命中第一个成立的分支，后面的全部跳过
    @Test
    public void testDynamicChoose() {
        System.out.println("========== 动态 SQL 3：<choose>/<when>/<otherwise> 互斥分支 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            // 同时给 id 和 username：id 的判断排在前面，命中后 username 那条被跳过
            User q1 = new User();
            q1.setId(1);
            q1.setUsername("lisi");
            System.out.println("--- id 与 username 同时给 → 只按 id（优先级最高），返回的不是 lisi ---");
            mapper.findUserPriority(q1).forEach(System.out::println);

            User q2 = new User();
            q2.setUsername("zhang");
            System.out.println("--- 不给 id → 退到 username 分支 ---");
            mapper.findUserPriority(q2).forEach(System.out::println);

            System.out.println("--- 什么都不给 → <otherwise> 兜底 1 = 1（WHERE 1 = 1 等价于不过滤） ---");
            System.out.println("命中 " + mapper.findUserPriority(new User()).size() + " 条");
        }
    }

    // 4. <foreach> 批量查询：ids 展开成 IN (1, 2, 3)
    @Test
    public void testDynamicForeachIn() {
        System.out.println("========== 动态 SQL 4：<foreach> 批量查询 IN ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            List<User> users = mapper.findByIds(Arrays.asList(1, 2, 3));
            System.out.println("命中 " + users.size() + " 条");
            users.forEach(System.out::println);
        }
    }

    // 5. <foreach> 批量插入：一条 INSERT 多组 VALUES，回查后自行清场
    @Test
    public void testDynamicForeachBatchInsert() {
        System.out.println("========== 动态 SQL 5：<foreach> 批量插入 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            long stamp = System.currentTimeMillis();
            User u1 = new User();
            u1.setUsername("batch_a_" + stamp);
            u1.setPassword("111111");
            u1.setEmail("batch_a@offblink.test");
            User u2 = new User();
            u2.setUsername("batch_b_" + stamp);
            u2.setPassword("222222");
            u2.setEmail("batch_b@offblink.test");

            // email 列上有唯一约束，两行必须给不同邮箱（同一条 INSERT 里重复也会整条失败）
            // 对应方法中的java.util.List
            System.out.println("批量插入影响行数：" + mapper.batchInsert(Arrays.asList(u1, u2)));

            // 回查：按 username 模糊捞回刚才这两条（顺带再验证一次 <if>+<where> 的单条件分支）
            User probe = new User();
            probe.setUsername("batch");
            List<User> inserted = mapper.findUsersByCondition(probe);
            System.out.println("回查命中 " + inserted.size() + " 条：" + inserted.get(0).getUsername()
                    + " / " + inserted.get(1).getUsername());

            for (User u : inserted) {   // 清场，不留脏数据
                mapper.deleteUser(u.getId());
            }
            session.commit();
        }
    }

    // 6. <trim>：<where> 的通用写法，自己指定前缀与要去掉的开头
    @Test
    public void testDynamicTrim() {
        System.out.println("========== 动态 SQL 6：<trim> 自定义裁剪 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            User q = new User();
            q.setUsername("zhang");
            q.setEmail("zhangsan@example.com");
            mapper.findUsersByTrim(q).forEach(System.out::println);
        }
    }

    // 7. <sql> + <include>：列清单与公共条件都来自片段，此处只做拼装
    @Test
    public void testDynamicInclude() {
        System.out.println("========== 动态 SQL 7：<sql> + <include> 片段复用 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);

            User q = new User();
            q.setUsername("zhang");
            mapper.findUsersWithInclude(q).forEach(System.out::println);
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
