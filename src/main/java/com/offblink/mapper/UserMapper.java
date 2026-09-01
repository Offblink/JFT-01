package com.offblink.mapper;

import com.offblink.entity.User;

import java.util.List;

/**
 * 用户 Mapper 接口
 * 对应映射文件：resources/mapper/UserMapper.xml（namespace 必须与本接口全限定名一致）
 */
public interface UserMapper {

    /**
     * 查询所有用户
     */
    List<User> findAll();

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User findById(Integer id);
}
