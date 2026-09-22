package com.offblink;

import com.offblink.entity.Dept;
import com.offblink.entity.Emp;
import com.offblink.entity.Skill;
import com.offblink.mapper.DeptRelationMapper;
import com.offblink.mapper.EmpRelationMapper;
import com.offblink.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 第 5 章 关联映射（XML 方式）测试：一对一 / 多对一 / 一对多 / 多对多
 * <p>
 * 数据来自两份建表脚本：{@code sql/ssm_emp.sql}（员工 4 人）+ {@code sql/dept_skill.sql}（部门/技能/中间表）：
 * <pre>
 *   emp_id 1 张伟 研发部(10)   emp_id 2 李娜 研发部(10)
 *   emp_id 3 王强 市场部(20)   emp_id 4 赵敏 人事部(30) —— 故意一项技能都没有
 * </pre>
 * 用例全部只读：关联查询不该顺手改库，所以不做造数/清理，跑多少次结果都一样。
 */
public class EmpRelationMapperTest {

    private SqlSession session;
    private EmpRelationMapper empMapper;
    private DeptRelationMapper deptMapper;

    @Before
    public void init() {
        session = MyBatisUtil.openSession();
        empMapper = session.getMapper(EmpRelationMapper.class);
        deptMapper = session.getMapper(DeptRelationMapper.class);
    }

    @After
    public void close() {
        session.close();
    }

    /**
     * 一对一：查一个员工，带出他的部门（一条 LEFT JOIN + &lt;association&gt; 嵌套结果）
     */
    @Test
    public void testOne2oneByXml() {
        System.out.println("========== 一对一（XML 方式）==========");
        System.out.println("知识点：<association> + javaType + 嵌套结果映射，SQL 只有 1 条");

        Emp emp = empMapper.one2oneByXml(1);
        assertNotNull(emp);
        // 关联对象 deptInfo 是整行部门数据；Emp.dept 那串文字是实验一遗留的文本列，两者都在
        assertNotNull("deptInfo 为空 → 检查外键别名 emp_dept_id 与 d.dept_id 有没有撞车", emp.getDeptInfo());
        assertEquals("研发部", emp.getDeptInfo().getDeptName());
        assertEquals(Integer.valueOf(10), emp.getDeptInfo().getDeptId());

        System.out.println("员工：" + emp.getEmpName() + "（编号 " + emp.getEmpId() + "）");
        System.out.println("部门：" + emp.getDeptInfo().getDeptName() + "（编号 " + emp.getDeptInfo().getDeptId()
                + "，地点 " + emp.getDeptInfo().getLoc() + "）");
        System.out.println("员工表上的外键 deptId = " + emp.getDeptId() + "（与部门对象的 deptId 同值，没被覆盖）");
    }

    /**
     * 多对一：一次查一批员工，每人各带一个部门对象（复用同一份 resultMap，只是没有 WHERE）
     */
    @Test
    public void testMany2oneByXml() {
        System.out.println("========== 多对一（XML 方式）==========");
        System.out.println("知识点：一条 JOIN 查全部 + resultMap 复用 association");

        List<Emp> emps = empMapper.many2oneByXml();
        assertEquals(4, emps.size());
        System.out.println("员工总数：" + emps.size());
        for (Emp e : emps) {
            // "多"的一侧：一批员工各自指向"一"的一侧（同一个部门对象内容会被查成多份，值相同）
            System.out.println("  " + e.getEmpId() + " - " + e.getEmpName() + " -> "
                    + (e.getDeptInfo() != null ? e.getDeptInfo().getDeptName() : "无部门"));
        }
        assertNotNull(emps.get(0).getDeptInfo());
    }

    /**
     * 一对多：查一个部门，带出它下面的所有员工（&lt;collection&gt; + ofType，主键必须 &lt;id&gt;）
     */
    @Test
    public void testOne2manyByXml() {
        System.out.println("========== 一对多（XML 方式）==========");
        System.out.println("知识点：<collection> + ofType 指定元素类型 + 主键归并");

        Dept dept = deptMapper.one2manyByXml(10);
        assertNotNull(dept);
        // JOIN 出 2 行（研发部 2 个员工）被归并成 1 个 Dept；若 dept_id 写成 <result> 这里就会是"2 个部门"
        assertEquals("研发部", dept.getDeptName());
        assertNotNull(dept.getEmps());
        assertEquals(2, dept.getEmps().size());

        System.out.println("部门：" + dept.getDeptName() + "（编号 " + dept.getDeptId() + "，地点 " + dept.getLoc() + "）");
        System.out.println("员工数量：" + dept.getEmps().size());
        for (Emp e : dept.getEmps()) {
            System.out.println("  - " + e.getEmpName() + "（" + e.getPost() + "）");
        }
    }

    /**
     * 多对多：查一个员工 + 他的技能集合（两次 JOIN 穿过中间表）；顺带看不掌握任何技能的人长什么样
     */
    @Test
    public void testMany2manyByXml() {
        System.out.println("========== 多对多（XML 方式）==========");
        System.out.println("知识点：中间表 employer_skill + 两次 LEFT JOIN + <collection> + DISTINCT");

        Emp emp = empMapper.many2manyByXml(1);
        assertNotNull(emp);
        assertNotNull(emp.getSkills());
        assertEquals(2, emp.getSkills().size());

        System.out.println("员工：" + emp.getEmpName() + "（编号 " + emp.getEmpId() + "）");
        System.out.println("技能数量：" + emp.getSkills().size());
        for (Skill s : emp.getSkills()) {
            System.out.println("  - " + s.getName() + "：" + s.getDescription());
        }

        // 赵敏(4) 在中间表里一条关系都没有：LEFT JOIN 仍然返回 1 行（skill 侧整行全 null），
        // 实测（MyBatis 3.5.10）映射层不会造出全 null 的技能对象，Emp.skills 得到空集合 []
        Emp noSkill = empMapper.many2manyByXml(4);
        System.out.println("没技能的人：" + noSkill.getEmpName() + "，skills = " + noSkill.getSkills());
        assertNotNull(noSkill.getSkills());
        // 兜住"全 null 的脏对象"：万一哪天换版本造出来了，这里会红
        for (Skill s : noSkill.getSkills()) {
            assertNotNull("LEFT JOIN 产生了全 null 的技能对象（见 XML 注释的实测记录）", s.getId());
        }
        assertTrue(noSkill.getSkills().isEmpty());
    }
}
