package com.offblink.chapter05;

import com.offblink.entity.Emp;
import com.offblink.entity.Skill;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 第 5 章：员工侧的关联查询 —— XML 版与注解版都收在这一个接口里
 * <p>
 * 与老师 {@code com.chapter05.Chapter05EmpMapper}（chapter05.rar 里的 chapter05Java/）一一对应：
 * <pre>
 *   one2oneByXml / many2oneByXml / many2manyByXml     → XML（同包同名的 EmpRelationMapper.xml）
 *   one2oneByAnn / many2oneByAnn / many2manyByAnn     → 注解（@Results + @One/@Many 嵌套 select）
 *   selectSkillsByEmpId                               → 多对多的子查询（被 many2manyByAnn 的 @Many 引用）
 *   selectEmpsByDeptId                                → 一对多的子查询（被 DeptRelationMapper.one2manyByAnn 引用）
 * </p>
 * 适配点（老师的包名/列名 → 本项目）：{@code com.chapter05} → {@code com.offblink.chapter05}、
 * {@code com.entity} → {@code com.offblink.entity}、{@code emp/empno} → {@code ssm_emp.employer/emp_id}、
 * {@code deptno} → {@code dept_id}、关联对象字段 {@code dept} → {@code deptInfo}（本项目的 {@code dept}
 * 已被实验一的部门文本列占用）。表名一律 schema 限定。
 * <p>
 * 为什么另起接口而不是塞进 {@link EmpMapper}：那个继承 BaseMapper&lt;Emp&gt;，是实验一（CRUD/动态 SQL）的载体，
 * 本章只做连表查询，混在一起以后找"关联映射"就得翻全文件。
 */
public interface EmpRelationMapper {

    /* ==================== 一对一 / 多对一 ==================== */

    /**
     * 一对一（XML 方式）：查一个员工，顺带把他的部门装进 Emp.deptInfo。
     * 一条 LEFT JOIN + &lt;association&gt; 嵌套结果，SQL 只有 1 条。
     */
    Emp one2oneByXml(Integer empId);

    /**
     * 一对一（注解方式）：主查询只查员工，{@code @One} 指向"按编号查部门"的方法 —— 这就是嵌套 select，
     * 访问 getDeptInfo() 时才发第二条 SQL（靠 mybatis-config.xml 的 lazyLoadingEnabled）。
     * <p>
     * 注意 select 必须写接口**全限定名**，写短名会报 Invalid bound statement。
     * 老师原版这条 SQL 还写了 {@code LEFT JOIN dept} 并把 {@code d.deptno AS d_deptno} 查出来，
     * 但映射走的是 @One 嵌套 select，那些 JOIN 列一个都没用上（白查一次 JOIN）—— 这里去掉，只查员工表。
     */
    @Select("SELECT emp_id, emp_name, gender, dept, post, salary, hire_date, status, dept_id "
            + "FROM ssm_emp.employer WHERE emp_id = #{empId}")
    @Results({
            @Result(property = "empId", column = "emp_id"),
            @Result(property = "empName", column = "emp_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dept", column = "dept"),
            @Result(property = "post", column = "post"),
            @Result(property = "salary", column = "salary"),
            @Result(property = "hireDate", column = "hire_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "deptId", column = "dept_id"),
            /* column 写的是"结果集的列名"，它是子查询的入参；property 才是 Emp 里的字段 */
            @Result(property = "deptInfo", column = "dept_id",
                    one = @One(select = "com.offblink.chapter05.DeptRelationMapper.findById"))
    })
    Emp one2oneByAnn(Integer empId);

    /**
     * 多对一（XML 方式）：一次查一批员工，每人带一个部门对象。
     * 与 {@link #one2oneByXml} 共用同一份 resultMap，只差 WHERE 与返回类型 ——
     * 这就是"一对一与多对一在映射层写法相同、只是发起查询的一侧不同"的实证。
     */
    List<Emp> many2oneByXml();

    /**
     * 多对一（注解方式）：没有 WHERE 的版本。发 1 条主查询 + N 条部门子查询（4 个员工 = 1+4 条），
     * 这正是讲义里 N+1 的现场 —— 注解版没法像 XML 那样复用一份 resultMap，
     * 要么把 @Results 重贴一遍（老师与本接口的做法），要么用 @ResultMap 引用另一个注解方法的映射。
     */
    @Select("SELECT emp_id, emp_name, gender, dept, post, salary, hire_date, status, dept_id "
            + "FROM ssm_emp.employer ORDER BY emp_id")
    @Results({
            @Result(property = "empId", column = "emp_id"),
            @Result(property = "empName", column = "emp_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dept", column = "dept"),
            @Result(property = "post", column = "post"),
            @Result(property = "salary", column = "salary"),
            @Result(property = "hireDate", column = "hire_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "deptId", column = "dept_id"),
            @Result(property = "deptInfo", column = "dept_id",
                    one = @One(select = "com.offblink.chapter05.DeptRelationMapper.findById"))
    })
    List<Emp> many2oneByAnn();

    /* ==================== 多对多 ==================== */

    /**
     * 多对多（XML 方式）：查一个员工 + 他掌握的技能集合。
     * 关系藏在中间表 ssm_emp.employer_skill 里，所以要两次 LEFT JOIN
     * （employer → employer_skill → skill）+ &lt;collection&gt;。
     */
    Emp many2manyByXml(Integer empId);

    /**
     * 多对多（注解方式）：主查询查员工，{@code @Many} 指向"按员工编号查技能"的方法，
     * 该方法内部自己做两次 JOIN —— 注解版把中间表查询抽成独立方法，职责更清楚。
     */
    @Select("SELECT emp_id, emp_name, gender, dept, post, salary, hire_date, status, dept_id "
            + "FROM ssm_emp.employer WHERE emp_id = #{empId}")
    @Results({
            @Result(property = "empId", column = "emp_id"),
            @Result(property = "empName", column = "emp_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "dept", column = "dept"),
            @Result(property = "post", column = "post"),
            @Result(property = "salary", column = "salary"),
            @Result(property = "hireDate", column = "hire_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "deptId", column = "dept_id"),
            @Result(property = "skills", column = "emp_id",
                    many = @Many(select = "com.offblink.chapter05.EmpRelationMapper.selectSkillsByEmpId"))
    })
    Emp many2manyByAnn(Integer empId);

    /**
     * 多对多的子查询（@Many 的靶子）：从技能表穿过中间表找"这个员工会什么"。
     * 单参数方法不必加 @Param，但加了更明确（也多一层"@Param 是给 collection/占位符用的名字"的记忆点）。
     */
    @Select("SELECT s.id, s.name, s.description "
            + "FROM ssm_emp.skill s "
            + "INNER JOIN ssm_emp.employer_skill es ON s.id = es.skill_id "
            + "WHERE es.emp_id = #{empId}")
    List<Skill> selectSkillsByEmpId(@Param("empId") Integer empId);

    /**
     * 一对多的子查询（被 {@link DeptRelationMapper#one2manyByAnn} 的 @Many 引用）：
     * 按部门编号查员工列表，等于 XML 版 {@code deptWithEmpsMap} 那条嵌套结果的分工。
     */
    @Select("SELECT emp_id, emp_name, gender, dept, post, salary, hire_date, status, dept_id "
            + "FROM ssm_emp.employer WHERE dept_id = #{deptId} ORDER BY emp_id")
    List<Emp> selectEmpsByDeptId(@Param("deptId") Integer deptId);
}
