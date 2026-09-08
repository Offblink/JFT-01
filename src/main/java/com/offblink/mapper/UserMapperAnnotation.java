package com.offblink.mapper;

import com.offblink.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
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
}
