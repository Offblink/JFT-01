package com.offblink.lab01;

import com.offblink.entity.User;
import com.offblink.entity.Vo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户 Mapper 接口（注解方式，第 3 节）
 * SQL 直接写在注解里，无需对应 XML；需在 mybatis-config.xml 用 <mapper class> 注册
 */
public interface UserMapperAnnotation {

    /** 查询所有用户 */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user")
    List<User> findAll();

    /** 根据 ID 查询用户 */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user WHERE id = #{id}")
    User findById(Integer id);

    /** 添加用户：@Options 对应 XML 版的 useGeneratedKeys + keyProperty（自增主键回填） */
    @Insert("INSERT INTO user (username, password, email) VALUES (#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addUser(User user);

    /** 更新用户 */
    @Update("UPDATE user SET username = #{username}, password = #{password}, email = #{email} WHERE id = #{id}")
    int updateUser(User user);

    /** 删除用户 */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteUser(Integer id);

    /** 多参数查询：@Param 给每个参数命名，SQL 按名引用；不写会报 Parameter 'xxx' not found（可用参数只有 arg0/param1...） */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user "
            + "WHERE username = #{name} AND email = #{email}")
    List<User> findByNameAndEmail(@Param("name") String username, @Param("email") String email);

    /** 模糊查询（安全版）：CONCAT 拼通配符 + #{}，整个值走预编译 */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user "
            + "WHERE username LIKE CONCAT('%', #{keyword}, '%')")
    List<User> findByNameLikeSafe(@Param("keyword") String keyword);

    /** 模糊查询（危险版，仅作 #{} 对比教学）：${} 直接拼进 SQL，生产代码禁用 */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user "
            + "WHERE username LIKE '%${keyword}%'")
    List<User> findByNameLikeUnsafe(@Param("keyword") String keyword);

    /** ${} 的正当用途：动态表名——表名无法参数化只能拼接，但值必须来自代码白名单而非用户输入 */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM ${tableName}")
    List<User> findAllByTableName(@Param("tableName") String tableName);

    /** 登录查询（危险版）：${} 字符串拼接，SQL 注入演示专用 */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user "
            + "WHERE username = '${username}' AND password = '${password}'")
    User loginUnsafe(@Param("username") String username, @Param("password") String password);

    /** 登录查询（安全版）：#{} 预编译，注入无效 */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user "
            + "WHERE username = #{username} AND password = #{password}")
    User loginSafe(@Param("username") String username, @Param("password") String password);

    // ==================== 第 4 节：注解版 resultMap（@Results / @ResultMap） ====================

    /**
     * 注解方式 1：@Results 直接贴在方法上，是一个「匿名 ResultMap」，只对本方法生效。
     * @Result(property = 属性名, column = 列名, id = true) —— id = true 等价 XML 的 &lt;id&gt;，声明主键；
     * 不写 id = true 的就是普通字段，等价 XML 的 &lt;result&gt;。
     * 这里映射成 VO，效果和 XML 的 findAllByVoMap（userVoMap）完全一致。
     */
    @Select("SELECT id, username, password FROM user")
    @Results({
            @Result(property = "p1", column = "id", id = true), // 主键
            @Result(property = "p2", column = "username"),
            @Result(property = "p3", column = "password")
    })
    List<Vo> findAllVoByAnnoMap();

    /**
     * 注解方式 2：@Results 带上 id（命名 ResultMap），供其他查询用 @ResultMap 重复引用 ——
     * 适合多个方法共用同一套映射，改一处、其他方法跟着生效（比每个方法抄一遍 @Results 可靠）。
     */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user WHERE id = #{id}")
    @Results(id = "annoUserMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email", column = "email"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    User findByIdWithAnnoMap(Integer id);

    /** @ResultMap 引用上面命名的 annoUserMap：不用再抄一遍那串 @Result */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user ORDER BY id")
    @ResultMap("annoUserMap")
    List<User> findAllWithAnnoMap();

    // ==================== 第 4-2 节：跨方式引用 XML ResultMap + 注解版动态 SQL ====================

    /**
     * 注解方式 4：@ResultMap 引用【XML 里定义的】ResultMap —— 跨方式引用。
     * SQL 写在注解里，column → 属性 的映射规则却复用 XML 的 userResultMap，
     * 好处是映射规则只维护一份，XML 线与注解线共用。
     *
     * 注意这里的 id 必须写全限定名：@ResultMap 的值会先套上「当前接口所在命名空间」前缀
     * （写成 userResultMap 会被解析成 ...UserMapperAnnotation.userResultMap，直接报
     *  "Result Maps collection does not contain value"）。带点号的写法会被原样使用，才能指到
     * UserMapper 命名空间里的那个 resultMap。同一接口内引用自己定义的 @Results(id=...) 才可以用短名。
     */
    @Select("SELECT id, username, password, email, created_at, updated_at FROM user "
            + "WHERE username LIKE CONCAT('%', #{keyword}, '%')")
    @ResultMap("com.offblink.lab01.UserMapper.userResultMap")
    List<User> findByNameLikeByXmlMap(@Param("keyword") String keyword);

    /**
     * 注解版动态 SQL：用 &lt;script&gt; 把 XML 动态标签包进 @Select 字符串（MyBatis 3.x 支持）。
     * 功能与 XML 版的 findUsersByCondition 完全一致，但全挤在字符串里、引号还要转义、可读性差——
     * 复杂动态 SQL 写 XML，注解只留给简单语句，这正是本节要得出的结论。
     */
    @Select("<script>"
            + "SELECT id, username, password, email, created_at, updated_at FROM user "
            + "<where>"
            + "  <if test='username != null and username != \"\"'>"
            + "    AND username LIKE CONCAT('%', #{username}, '%')"
            + "  </if>"
            + "  <if test='email != null and email != \"\"'>"
            + "    AND email = #{email}"
            + "  </if>"
            + "</where>"
            + "</script>")
    @ResultMap("com.offblink.lab01.UserMapper.userResultMap")
    List<User> findUsersAnnoDynamic(User user);
}
