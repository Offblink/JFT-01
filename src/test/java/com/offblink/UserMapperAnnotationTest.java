package com.offblink;

import com.offblink.entity.User;
import com.offblink.entity.Vo;
import com.offblink.mapper.UserMapperAnnotation;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * 注解方式 Mapper 测试（第 3 节）
 * 重点：注解 CRUD / @Param 多参数 / #{} 与 ${} 对比 / SQL 注入攻防演示
 */
public class UserMapperAnnotationTest {

    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws Exception {
        InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    }

    @Test
    public void testFindAll() {
        System.out.println("========== 注解版：查询所有用户 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);

            List<User> users = mapper.findAll();
            for (User user : users) {
                System.out.println(user);
            }
        }
    }

    @Test
    public void testCrudLifecycle() {
        System.out.println("========== 注解版：增改查删生命周期 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);

            // 增：@Options 主键回填，与 XML 版 useGeneratedKeys 等价
            User user = new User();
            user.setUsername("注解测试用户");
            user.setPassword("654321");
            user.setEmail("annotation@qq.com");
            int rows = mapper.addUser(user);
            System.out.println("新增影响行数：" + rows + "，回填主键 id = " + user.getId());

            // 改
            user.setEmail("updated@qq.com");
            rows = mapper.updateUser(user);
            System.out.println("更新影响行数：" + rows);

            // 查
            System.out.println("更新后查询：" + mapper.findById(user.getId()));

            // 删（清场）
            rows = mapper.deleteUser(user.getId());
            System.out.println("删除影响行数：" + rows);

            session.commit();
        }
    }

    @Test
    public void testFindByNameAndEmail() {
        System.out.println("========== 注解版：@Param 多参数查询 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);
            List<User> users = mapper.findByNameAndEmail("zhangsan", "zhangsan@example.com");
            System.out.println("命中 " + users.size() + " 条：" + users);
        }
    }

    @Test
    public void testLikeSafeVsUnsafe() {
        System.out.println("========== #{} 与 ${} 模糊查询对比 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);
            // 两者结果相同，差异在日志：safe 版 Preparing 是 ?，Parameters 有值；unsafe 版值直接拼进 Preparing
            System.out.println("安全版(#{}): " + mapper.findByNameLikeSafe("zhang"));
            System.out.println("危险版(${}): " + mapper.findByNameLikeUnsafe("zhang"));
        }
    }

    @Test
    public void testSqlInjectionDemo() {
        System.out.println("========== SQL 注入攻防演示 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);

            // 正常登录（zhangsan 的真实密码）
            User normal = mapper.loginSafe("zhangsan", "pass123");
            System.out.println("正常登录：" + normal);

            // 注入攻击：' 注入闭合引号，-- 注释掉密码校验，只输错密码！
            String injection = "zhangsan' -- ";
            User hacked = mapper.loginUnsafe(injection, "错误密码");
            System.out.println("危险版(${}) 注入结果：" + hacked + "  ← 密码错误却登录成功，攻击成立");

            User blocked = mapper.loginSafe(injection, "错误密码");
            System.out.println("安全版(#{}) 注入结果：" + blocked + "  ← 整串当作普通字符串匹配，攻击失败");
        }
    }

    @Test
    public void testDynamicTableName() {
        System.out.println("========== ${} 正当用途：动态表名 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);
            // 表名无法用 ? 参数化，只能拼接；值必须来自代码白名单
            List<User> users = mapper.findAllByTableName("user");
            System.out.println("动态表名查询命中 " + users.size() + " 条");
        }
    }

    // ==================== 第 4 节：注解版 resultMap（@Results / @ResultMap） ====================

    // 注解方式 1：@Results 直接贴在方法上（匿名 ResultMap），只对本方法生效；
    // @Result(property, column, id = true) 里的 id = true 等价 XML 的 <id> 主键映射。
    @Test
    public void testFindAllVoByAnnoMap() {
        System.out.println("========== 注解方式1：@Results 匿名映射成 VO ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);

            List<Vo> list = mapper.findAllVoByAnnoMap();
            System.out.println("命中 " + list.size() + " 条");
            for (Vo vo : list) {
                System.out.println(vo);
            }
        }
    }

    // 注解方式 2：@Results 带 id 命名后，其他方法用 @ResultMap 引用同一套映射
    @Test
    public void testFindByIdWithAnnoMap() {
        System.out.println("========== 注解方式2：命名 ResultMap（annoUserMap） ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);

            // 映射到 User：created_at → createdAt、updated_at → updatedAt 也由 @Result 显式声明
            User user = mapper.findByIdWithAnnoMap(1);
            System.out.println("findByIdWithAnnoMap(1) = " + user);
        }
    }

    // @ResultMap("annoUserMap") 复用上面那套映射，不用再抄一遍 @Result
    @Test
    public void testFindAllWithAnnoMap() {
        System.out.println("========== @ResultMap 复用命名映射 ==========");
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapperAnnotation mapper = session.getMapper(UserMapperAnnotation.class);

            List<User> users = mapper.findAllWithAnnoMap();
            System.out.println("命中 " + users.size() + " 条，第一条 = " + users.get(0));
        }
    }
}
