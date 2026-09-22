package com.offblink.mapper;

import com.offblink.entity.Dept;

/**
 * 第 5 章：部门侧的关联查询（本项目的 XML 版实现）
 * 对应老师的 {@code com.chapter05.Chapter05DeptMapper} 里的 XML 部分。
 */
public interface DeptRelationMapper {

    /**
     * 一对多：查一个部门，顺带把它下面的所有员工装进 Dept.emps。
     * 用一条 LEFT JOIN + &lt;collection ofType="Emp"&gt;（嵌套结果），SQL 只有 1 条。
     */
    Dept one2manyByXml(Integer deptId);
}
