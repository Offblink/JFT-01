package com.offblink.mapper;

import com.offblink.entity.Emp;

import java.util.List;

/**
 * 第 5 章：员工侧的关联查询（本项目的 XML 版实现）
 * 对应老师的 {@code com.chapter05.Chapter05EmpMapper} 里的 XML 部分。
 * <p>
 * 为什么另起一个接口而不是塞进 {@link EmpMapper}：{@code EmpMapper} 继承 BaseMapper&lt;Emp&gt;，
 * 是实验一的载体（基础 CRUD / 动态 SQL），本章只做连表查询，两者混在一个文件里
 * 以后想单独找"关联映射"那几条语句就得全文件翻。XML 与接口同包同名放在
 * {@code resources/com/offblink/mapper/EmpRelationMapper.xml}，由 &lt;mapper class&gt; 自动发现。
 * <p>
 * 三个方法对应老师截图里的三条链路（一对一/多对一/多对多），注解版（@One/@Many）本章不做。
 */
public interface EmpRelationMapper {

    /**
     * 一对一：查一个员工，顺带把他的部门装进 Emp.deptInfo。
     * 用一条 LEFT JOIN + &lt;association&gt;（嵌套结果），SQL 只有 1 条。
     */
    Emp one2oneByXml(Integer empId);

    /**
     * 多对一：一次查出所有员工，每人带一个部门对象。
     * 与 {@link #one2oneByXml} 共用 resultMap，唯一区别是没有 WHERE —— 这正是
     * "一对一和多对一在映射层写法相同、只是发起查询的那一侧不同"的含义。
     */
    List<Emp> many2oneByXml();

    /**
     * 多对多：查一个员工 + 他掌握的技能集合。
     * 关系藏在中间表 ssm_emp.employer_skill 里，所以要两次 LEFT JOIN
     * （employer → employer_skill → skill）+ &lt;collection&gt;。
     */
    Emp many2manyByXml(Integer empId);
}
