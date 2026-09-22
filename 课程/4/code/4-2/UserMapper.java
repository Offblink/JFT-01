package com.chapter03;

import com.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户 Mapper 接口（第3节：ResultMap 与 动态 SQL）
 *
 * 本节聚焦两大核心知识点：
 *
 * ── ResultMap（XML 方式 & 注解方式）──
 *   · XML:  在 UserMapper.xml 中定义 <resultMap id="userResultMap">
 *   · 注解: @Results 直接在方法上定义 / @ResultMap 引用已有 ResultMap
 *   · 跨方式: 注解 SQL 通过 @ResultMap("userResultMap") 引用 XML ResultMap
 *
 * ── 动态 SQL（两种方式）──
 *   · 注解方式：<script> 标签包裹 XML 动态标签（MyBatis 3.x 支持）
 *   · XML 方式：完整的 6 种动态标签（<if> <where> <set> <choose> <foreach> <trim>）
 *
 * 小贴士：
 *   注解写动态 SQL 很丑 —— 全部堆在一行字符串里，转义麻烦，可读性差。
 *   实际项目中复杂动态 SQL 99% 写 XML，注解适合简单 SQL。
 */
public interface UserMapper {

    // ==================== ResultMap（注解方式）====================

    /**
     * 注解方式 1: 直接在方法上定义匿名 ResultMap
     * 适合只给当前方法用的简单映射
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    @Results({
            @Result(property = "id",       column = "id",           id = true),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email",    column = "email"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    User findByIdAnno(Integer id);

    /**
     * 注解方式 2: 在方法上定义【命名 ResultMap】，供 @ResultMap 重复引用
     * 适合多个方法共用同一套映射
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    @Results(id = "annoUserMap", value = {
            @Result(property = "id",       column = "id",           id = true),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "email",    column = "email"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    User findByIdWithAnnoMap(Integer id);

    /**
     * 注解方式 3: @ResultMap 引用【注解定义的命名 ResultMap】
     */
    @Select("SELECT * FROM user")
    @ResultMap("annoUserMap")
    List<User> findAllByAnnoMap();

    /**
     * 注解方式 4: @ResultMap 引用【XML 中定义的 ResultMap】
     * 跨方式引用 —— 注解写 SQL，但映射规则复用 XML 的 userResultMap
     */
    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{kw}, '%')")
    @ResultMap("userResultMap")
    List<User> findLikeByXmlMap(@Param("kw") String keyword);

    // ==================== 动态 SQL（注解方式 —— <script> 包裹）====================

    /**
     * 注解方式的动态 SQL：用 <script> 标签把 XML 动态标签包在 @Select 字符串里
     *
     * 语法规则：
     *   · 整个 SQL 字符串用 <script>...</script> 包裹
     *   · 内部可以写任意 MyBatis 动态标签：<if> <where> <set> <choose> <foreach> <trim>
     *   · 注意：XML 中的双引号 " 在 Java 字符串里要转义成 \"，或者用单引号 '
     *
     * 下面演示注解版的 <if> + <where> 条件组合查询，对比 XML 版 findUsersByCondition
     */
    @Select("<script>" +
            "SELECT * FROM user " +
            "<where>" +
            "  <if test='username != null and username != \"\"'>" +
            "    AND username LIKE CONCAT('%', #{username}, '%')" +
            "  </if>" +
            "  <if test='email != null and email != \"\"'>" +
            "    AND email = #{email}" +
            "  </if>" +
            "  <if test='id != null'>" +
            "    AND id = #{id}" +
            "  </if>" +
            "</where>" +
            "</script>")
    @ResultMap("userResultMap")
    List<User> findUsersAnnoDynamic(User user);

    // ==================== 动态 SQL（XML 方式）====================

    /**
     * 1. <if> + <where>  多条件任意组合
     *    <where> 自动去掉首个多余的 AND/OR
     *    所有条件都为空时自动去掉 WHERE 子句
     */
    List<User> findUsersByCondition(User user);

    /**
     * 2. <set>  选择性更新
     *    只更新非空字段，自动去掉末尾多余的逗号
     */
    int updateUserSelective(User user);

    /**
     * 3. <choose>/<when>/<otherwise>  互斥分支（if-else if-else）
     *    只取优先级最高的那个条件
     */
    List<User> findUserPriority(User user);

    /**
     * 4. <foreach>  批量查询（IN 条件）
     */
    List<User> findByIds(@Param("ids") List<Integer> ids);

    /**
     * 5. <foreach>  批量插入
     */
    int batchInsert(List<User> users);

    /**
     * 6. <trim>  自定义裁剪（prefix / suffix / prefixOverrides / suffixOverrides）
     *    比 <where> / <set> 更灵活
     */
    List<User> findUsersByTrim(User user);
}