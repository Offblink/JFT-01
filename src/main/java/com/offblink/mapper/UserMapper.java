package com.offblink.mapper;

import com.offblink.entity.User;

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
}
