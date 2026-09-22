package com.chapter03;

import com.entity.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UserMapperTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        String resource = "chapter03/mybatis-config.xml";
        InputStream is = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    // ═══════════════════════════════════════════════════
    // 第一部分：ResultMap（注解方式）
    // ═══════════════════════════════════════════════════

    @Test
    public void testAnnoAnonymousResultMap() {
        System.out.println("========== ResultMap 注解方式 1: 匿名 @Results ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = mapper.findByIdAnno(1);
        System.out.println(user);

        sqlSession.close();
    }

    @Test
    public void testAnnoNamedResultMap() {
        System.out.println("========== ResultMap 注解方式 2: 命名 @Results(id=\"annoUserMap\") ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = mapper.findByIdWithAnnoMap(2);
        System.out.println(user);

        sqlSession.close();
    }

    @Test
    public void testAnnoRefAnnoResultMap() {
        System.out.println("========== ResultMap 注解方式 3: @ResultMap 引用【注解定义的命名 ResultMap】 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        List<User> users = mapper.findAllByAnnoMap();
        users.forEach(System.out::println);

        sqlSession.close();
    }

    @Test
    public void testAnnoRefXmlResultMap() {
        System.out.println("========== ResultMap 注解方式 4: @ResultMap 引用【XML 定义的 ResultMap】 ==========");
        System.out.println("         SQL 是注解写的，但 column→property 映射复用 XML 的 userResultMap");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        List<User> users = mapper.findLikeByXmlMap("admin");
        users.forEach(System.out::println);

        sqlSession.close();
    }

    // ═══════════════════════════════════════════════════
    // 第二部分：动态 SQL（两种方式对比）
    // ═══════════════════════════════════════════════════

    @Test
    public void testAnnoDynamicScript() {
        System.out.println("========== 动态 SQL（注解方式）: <script> + <if> + <where> ==========");
        System.out.println("         对比后面的 XML 版 testDynamicIfWhere，功能完全一致");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        // 场景1: 只传 username
        User q1 = new User();
        q1.setUsername("admin");
        System.out.println("--- 只按 username 查 ---");
        mapper.findUsersAnnoDynamic(q1).forEach(System.out::println);

        // 场景2: username + email
        User q2 = new User();
        q2.setUsername("admin");
        q2.setEmail("admin@qq.com");
        System.out.println("--- 按 username + email 查 ---");
        mapper.findUsersAnnoDynamic(q2).forEach(System.out::println);

        // 场景3: 什么都不传 → <where> 自动去掉 WHERE
        User q3 = new User();
        System.out.println("--- 无条件（<where> 自动处理空条件） ---");
        mapper.findUsersAnnoDynamic(q3).forEach(System.out::println);

        sqlSession.close();
    }

    @Test
    public void testDynamicIfWhere() {
        System.out.println("========== 动态 SQL 1: <if> + <where> 条件组合查询 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        // 场景1: 只传 username
        User q1 = new User();
        q1.setUsername("admin");
        q1.setId(4);
        System.out.println("--- 只按 username 查 ---");
        mapper.findUsersByCondition(q1).forEach(System.out::println);

        // 场景2: username + email
        User q2 = new User();
        q2.setUsername("admin");
        q2.setEmail("admin@qq.com");
        System.out.println("--- 按 username + email 查 ---");
        mapper.findUsersByCondition(q2).forEach(System.out::println);

        // 场景3: 什么都不传 → <where> 自动去掉 WHERE
        User q3 = new User();
        System.out.println("--- 无条件（<where> 自动处理空条件） ---");
        mapper.findUsersByCondition(q3).forEach(System.out::println);

        sqlSession.close();
    }

    @Test
    public void testDynamicSet() {
        System.out.println("========== 动态 SQL 2: <set> 选择性更新 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        // 只更新 password，其余字段不动
        User user = new User();
        user.setId(7);
        user.setPassword("newpass_456");
        user.setUpdateTime(new Date());

        int rows = mapper.updateUserSelective(user);
        System.out.println("更新影响行数：" + rows);

        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testDynamicChoose() {
        System.out.println("========== 动态 SQL 3: <choose>/<when>/<otherwise> 互斥分支 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        // 同时传 id 和 username → id 优先，username 被忽略
        User q1 = new User();
        q1.setId(1);
      //  q1.setUsername("admin");
        System.out.println("--- id 优先（同时传 id 和 username，只按 id 查） ---");
        mapper.findUserPriority(q1).forEach(System.out::println);

        // 不传 id → 按 username
        User q2 = new User();
        q2.setUsername("admin");
        System.out.println("--- 只传 username ---");
        mapper.findUserPriority(q2).forEach(System.out::println);

        // 什么都不传 → <otherwise> 兜底
        User q3 = new User();
        System.out.println("--- 什么都不传 → <otherwise> 兜底 ---");
        System.out.println("命中数量：" + mapper.findUserPriority(q3).size());

        sqlSession.close();
    }

    @Test
    public void testDynamicForeachIn() {
        System.out.println("========== 动态 SQL 4: <foreach> 批量查询（IN 条件） ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        List<User> users = mapper.findByIds(Arrays.asList(1, 2, 3));
        users.forEach(System.out::println);

        sqlSession.close();
    }

    @Test
    public void testDynamicForeachBatchInsert() {
        System.out.println("========== 动态 SQL 5: <foreach> 批量插入 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User u1 = new User();
        u1.setUsername("batch_p_" + System.currentTimeMillis());
        u1.setPassword("111111");
        u1.setEmail("p@test.com");
        u1.setCreateTime(new Date());
        u1.setUpdateTime(new Date());

        User u2 = new User();
        u2.setUsername("batch_q_" + System.currentTimeMillis());
        u2.setPassword("222222");
        u2.setEmail("q@test.com");
        u2.setCreateTime(new Date());
        u2.setUpdateTime(new Date());

        int rows = mapper.batchInsert(Arrays.asList(u1, u2));
        System.out.println("批量插入影响行数：" + rows);

        sqlSession.commit();
        sqlSession.close();
    }

    @Test
    public void testDynamicTrim() {
        System.out.println("========== 动态 SQL 6: <trim> 自定义裁剪 ==========");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User q = new User();
        q.setUsername("admin");

        mapper.findUsersByTrim(q).forEach(System.out::println);

        sqlSession.close();
    }

}
