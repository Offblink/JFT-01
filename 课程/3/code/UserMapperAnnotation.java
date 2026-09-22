package com.chapter01;

import com.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户 Mapper 接口（注解方式）
 *
 * 本节演示：
 * 1. 使用注解编写 SQL（无需 XML）
 * 2. 注解方式的日志记录
 * 3. 注解与 XML 方式的对比
 *
 * @version 1.0
 */
public interface UserMapperAnnotation {

    /**
     * 查询所有用户（注解方式）
     * 日志输出：SQL 语句会正常记录
     */
    @Select("SELECT * FROM user")
    List<User> findAll();

    /**
     * 根据ID查询用户（注解方式）
     * 日志输出：会显示参数值
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    /**
     * 添加用户（注解方式）
     * 日志输出：会显示插入的参数
     */
    @Insert("INSERT INTO user(username, password, email) VALUES(#{username}, #{password}, #{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addUser(User user);

    /**
     * 更新用户（注解方式）
     * 日志输出：会显示更新的参数
     */
    @Update("UPDATE user SET username = #{username}, password = #{password}, email = #{email} WHERE id = #{id}")
    int updateUser(User user);

    /**
     * 删除用户（注解方式）
     * 日志输出：会显示删除的参数
     */
    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteUser(Integer id);

    /**
     * 根据用户名模糊查询（注解方式）
     * 注意：${value} 是字符串拼接，有 SQL 注入风险
     * 日志输出：会显示完整的 SQL 语句
     */
    @Select("SELECT * FROM user WHERE username = '%${value}%'")
    List<User> findByUsernameLike(String username);
}
