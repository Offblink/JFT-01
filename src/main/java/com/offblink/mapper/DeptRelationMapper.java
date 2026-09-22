package com.offblink.mapper;

import com.offblink.entity.Dept;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * 第 5 章：部门侧的关联查询 —— XML 版与注解版
 * 对应老师的 {@code com.chapter05.Chapter05DeptMapper}（chapter05.rar 里的 chapter05Java/）：
 * <pre>
 *   one2manyByXml    → XML（同包同名的 DeptRelationMapper.xml）
 *   findById         → 注解 @Select，被 EmpRelationMapper.one2oneByAnn / many2oneByAnn 的 @One 引用
 *   one2manyByAnn    → 注解 @Results + @Many 嵌套 select
 * </pre>
 * 列名适配：老师用 scott 的 {@code deptno/dname/loc}，本项目是 {@code dept_id/dept_name/loc}。
 */
public interface DeptRelationMapper {

    /**
     * 一对多（XML 方式）：查一个部门，顺带把它下面的所有员工装进 Dept.emps。
     * 一条 LEFT JOIN + &lt;collection ofType="Emp"&gt; 嵌套结果，SQL 只有 1 条。
     */
    Dept one2manyByXml(Integer deptId);

    /**
     * 按部门编号查部门（@One 的靶子）：嵌套 select 的"第二条 SQL"就是它。
     * 单参数 Integer 不必写 @Param，MyBatis 会把唯一的实参直接塞进 {@code #{deptId}}。
     */
    @Select("SELECT dept_id, dept_name, loc FROM ssm_emp.dept WHERE dept_id = #{deptId}")
    Dept findById(Integer deptId);

    /**
     * 一对多（注解方式）：主查询查部门，@Many 指向"按部门编号查员工"的方法。
     * column 传的是部门编号（结果集里的 dept_id），它成为子查询的入参。
     * 查 3 个部门就是 1 + 3 条 SQL —— 一对多的 N+1 比一对一更明显。
     */
    @Select("SELECT dept_id, dept_name, loc FROM ssm_emp.dept WHERE dept_id = #{deptId}")
    @Results({
            @Result(property = "deptId", column = "dept_id"),
            @Result(property = "deptName", column = "dept_name"),
            @Result(property = "loc", column = "loc"),
            @Result(property = "emps", column = "dept_id",
                    many = @Many(select = "com.offblink.mapper.EmpRelationMapper.selectEmpsByDeptId"))
    })
    Dept one2manyByAnn(Integer deptId);
}
