package com.offblink.lab01;

import com.offblink.entity.User;
import com.offblink.entity.Vo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户 Mapper 接口，映射 resources/com/offblink/lab01/UserMapper.xml
 * （与接口同包同名，由 mybatis-config.xml 的 {@code <package>} 扫包注册）
 */

public interface UserMapper {

    /** 查询所有用户 */
    List<User> findAll();

    /** 根据 ID 查询用户 */
    User findById(Integer id);

    /** 用户名模糊查询 */
    List<User> findByUsernameLike(String keyword);

    /** 分页查询：用 Map 传多个参数，键名与 XML 里 #{} 占位名一一对应 */
    List<User> findByPage(Map<String, Object> params);

    /** 聚合查询：用户总数（COUNT(*) 单行单列，返回数字包装类） */
    Integer findCount();

    /** 多聚合查询：一行多列统计值，用 resultType="map" 接成 HashMap（key=列别名） */
    Map<String, Object> findStats();

    /** 添加用户（自增主键回填到 user.id） */
    int addUser(User user);

    /** 更新用户 */
    int updateUser(User user);

    /** 删除用户 */
    int deleteUser(Integer id);

    // ==================== 第 4 节：resultType 与 resultMap 对照 ====================

    /** ① 列别名演示：SQL 把 id 起了别名 uid，与 User 的属性名对不上 → 自动映射丢字段（id = null） */
    List<User> findAllWithColumnAlias();

    /** ② resultType + VO：靠列别名（p1/p2/p3）对上 VO 属性名才能装得进，别名即契约 */
    List<Vo> findAllByVoWithResultType();

    /** ③ resultMap + VO：列名原样写，映射规则写在 XML 的 userVoMap 里（与 ② 同一份数据、两种写法） */
    List<Vo> findAllByVoMap();

    // ==================== 第 4-2 节：动态 SQL（XML 方式，6 种标签 + 片段复用） ====================

    /** 1. <if> + <where>：有哪个条件查哪个，全空时 <where> 连 WHERE 一起省掉 */
    List<User> findUsersByCondition(User user);

    /** 2. <set>：只更新非空字段，自动去掉末尾逗号（至少传一个待改字段，否则拼不出 SET） */
    int updateUserSelective(User user);

    /** 3. <choose>/<when>/<otherwise>：互斥分支，按 id → username → email 的优先级取第一个成立的 */
    List<User> findUserPriority(User user);

    /** 4. <foreach> 批量查询：ids 展开成 IN (…) */
    List<User> findByIds(@Param("ids") List<Integer> ids);

    /** 5. <foreach> 批量插入：一条 INSERT 拼出多组 VALUES */
    int batchInsert(List<User> users);

    /** 6. <trim>：自定义裁剪，<where>/<set> 的通用写法 */
    List<User> findUsersByTrim(User user);

    /** 7. <sql> + <include>：列清单与公共条件片段复用 */
    List<User> findUsersWithInclude(User user);
}
