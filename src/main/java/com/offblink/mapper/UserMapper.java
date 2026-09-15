package com.offblink.mapper;

import com.offblink.entity.User;
import com.offblink.entity.Vo;

import java.util.List;
import java.util.Map;

/**
 * 用户 Mapper 接口，映射 resources/mapper/UserMapper.xml
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
}
