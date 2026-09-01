package com.offblink.mapper;

import com.offblink.entity.User;

import java.util.List;

/**
 * 用户 Mapper 接口
 * 对应映射文件：resources/mapper/UserMapper.xml（namespace 必须与本接口全限定名一致）
 */
public interface UserMapper {

    /** 查询所有用户 */
    List<User> findAll();

    /** 根据 ID 查询用户 */
    User findById(Integer id);

    /** 用户名模糊查询 */
    List<User> findByUsernameLike(String keyword);

    /** 添加用户（自增主键回填到 user.id） */
    int addUser(User user);

    /** 更新用户 */
    int updateUser(User user);

    /** 删除用户 */
    int deleteUser(Integer id);
}
