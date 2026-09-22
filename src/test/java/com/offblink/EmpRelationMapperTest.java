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
 * 第 5 章 关联映射测试：一对一 / 多对一 / 一对多 / 多对多 × XML / 注解
 * <p>
 * 由老师 {@code chapter05.rar → chapter05test/Chapter05Test.java} 的 9 个用例适配而来
 * （方法名一一对应），改动三处：
 * <ol>
 *   <li>数据换成本项目种子：员工 1 张伟 / 2 李娜 / 3 王强 / 4 赵敏；部门 10 研发部 / 20 市场部 / 30 人事部；</li>
 *   <li>老师用自建 {@code Resources.getResourceAsStream("chapter05/mybatis-config.xml")}，
 *       本项目统一走 {@link MyBatisUtil}（全项目共用一个工厂）；</li>
 *   <li>老师只打印不断言，这里补上断言 —— 打印给人看，断言给回归看。</li>
 * </ol>
 * 数据来自 {@code sql/ssm_emp.sql} + {@code sql/dept_skill.sql}：张伟(1) 会 Java+MySQL，李娜(2) 会 Vue，
 * 王强(3) 会 Axure，赵敏(4) 一项技能都没有。用例全部只读，跑多少次结果都一样。
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

    /* ==================== 一对一 ==================== */

    @Test
    public void testOne2oneByXml() {
        System.out.println("========== 一对一（XML方式）==========");
        System.out.println("知识点：<association> + javaType + 嵌套结果映射，SQL 只有 1 条");

        Emp emp = empMapper.one2oneByXml(1);
        assertNotNull(emp);
        assertNotNull("deptInfo 为空 → 检查外键别名 emp_dept_id 与 d.dept_id 有没有撞车", emp.getDeptInfo());
        assertEquals("研发部", emp.getDeptInfo().getDeptName());
        assertEquals(Integer.valueOf(10), emp.getDeptInfo().getDeptId());

        System.out.println("员工：" + emp.getEmpName() + "（编号 " + emp.getEmpId() + "）");
        System.out.println("部门：" + emp.getDeptInfo().getDeptName() + "（编号 " + emp.getDeptInfo().getDeptId()
                + "，地点 " + emp.getDeptInfo().getLoc() + "）");
        System.out.println("员工表上的外键 deptId = " + emp.getDeptId() + "（与部门对象的 deptId 同值，没被覆盖）");
    }

    @Test
    public void testOne2oneByAnn() {
        System.out.println("========== 一对一（注解方式）==========");
        System.out.println("知识点：@Results + @One(select = ...) 嵌套 select，访问 getDeptInfo() 才发第二条 SQL");

        Emp emp = empMapper.one2oneByAnn(2);
        assertNotNull(emp);
        assertEquals("李娜", emp.getEmpName());
        assertNotNull("deptInfo 为空 → 检查 @One 的 select 是不是接口全限定名", emp.getDeptInfo());
        assertEquals("研发部", emp.getDeptInfo().getDeptName());

        System.out.println("员工：" + emp.getEmpName() + "（编号 " + emp.getEmpId() + "）");
        System.out.println("部门：" + emp.getDeptInfo().getDeptName() + "（编号 " + emp.getDeptInfo().getDeptId()
                + "，地点 " + emp.getDeptInfo().getLoc() + "）");
    }

    /* ==================== 多对一 ==================== */

    @Test
    public void testMany2oneByXml() {
        System.out.println("========== 多对一（XML方式）==========");
        System.out.println("知识点：一条 JOIN 查全部 + resultMap 复用 association");

        List<Emp> emps = empMapper.many2oneByXml();
        assertEquals(4, emps.size());
        System.out.println("员工总数：" + emps.size());
        for (Emp e : emps) {
            // "多"的一侧：一批员工各自指向"一"的一侧
            System.out.println("  " + e.getEmpId() + " - " + e.getEmpName() + " -> "
                    + (e.getDeptInfo() != null ? e.getDeptInfo().getDeptName() : "无部门"));
        }
        assertNotNull(emps.get(0).getDeptInfo());
    }

    @Test
    public void testMany2oneByAnn() {
        System.out.println("========== 多对一（注解方式）==========");
        System.out.println("知识点：一条主查询 + @One 逐条发子查询 → 4 个员工就是 1+4 条 SQL（N+1 现场）");

        List<Emp> emps = empMapper.many2oneByAnn();
        assertEquals(4, emps.size());
        System.out.println("员工总数：" + emps.size());
        for (Emp e : emps) {
            System.out.println("  " + e.getEmpId() + " - " + e.getEmpName() + " -> "
                    + (e.getDeptInfo() != null ? e.getDeptInfo().getDeptName() : "无部门"));
        }
        assertNotNull(emps.get(1).getDeptInfo());
    }

    /* ==================== 一对多 ==================== */

    @Test
    public void testOne2manyByXml() {
        System.out.println("========== 一对多（XML方式）==========");
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

    @Test
    public void testOne2manyByAnn() {
        System.out.println("========== 一对多（注解方式）==========");
        System.out.println("知识点：@Results + @Many(select = ...) 嵌套 select");

        Dept dept = deptMapper.one2manyByAnn(20);
        assertNotNull(dept);
        assertEquals("市场部", dept.getDeptName());
        assertNotNull(dept.getEmps());
        assertEquals(1, dept.getEmps().size());

        System.out.println("部门：" + dept.getDeptName() + "（编号 " + dept.getDeptId() + "，地点 " + dept.getLoc() + "）");
        System.out.println("员工数量：" + dept.getEmps().size());
        for (Emp e : dept.getEmps()) {
            System.out.println("  - " + e.getEmpName() + "（" + e.getPost() + "）");
        }
    }

    /* ==================== 多对多 ==================== */

    @Test
    public void testMany2manyByXml() {
        System.out.println("========== 多对多（XML方式）==========");
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

    @Test
    public void testMany2manyByAnn() {
        System.out.println("========== 多对多（注解方式）==========");
        System.out.println("知识点：@Many(select = ...) 调用另一个查询方法（中间表查询被抽成独立方法）");

        Emp emp = empMapper.many2manyByAnn(2);
        assertNotNull(emp);
        assertEquals("李娜", emp.getEmpName());
        assertNotNull(emp.getSkills());
        assertEquals(1, emp.getSkills().size());
        assertEquals("Vue", emp.getSkills().get(0).getName());

        System.out.println("员工：" + emp.getEmpName() + "（编号 " + emp.getEmpId() + "）");
        System.out.println("技能数量：" + emp.getSkills().size());
        for (Skill s : emp.getSkills()) {
            System.out.println("  - " + s.getName() + "：" + s.getDescription());
        }
    }

    /* ==================== XML vs 注解 对照（专项实验的自查项） ==================== */

    /**
     * 专项实验的交付自查：同一个业务查询，XML 与注解两套实现的结果必须完全一致。
     * 不一致就说明某一边的主键标签、列名别名或 select 路径写错了。
     * 另外：注解版是嵌套 select，日志里能看到多出来的那条子查询（N+1），XML 版只有 1 条。
     */
    @Test
    public void testComparison() {
        System.out.println("========== XML vs 注解 方式对比 ==========");

        System.out.println("\n--- 一对一 / 多对一 ---");
        Emp xml = empMapper.one2oneByXml(1);
        Emp ann = empMapper.one2oneByAnn(1);
        System.out.println("XML: 员工=" + xml.getEmpName() + ", 部门=" + xml.getDeptInfo().getDeptName());
        System.out.println("Ann: 员工=" + ann.getEmpName() + ", 部门=" + ann.getDeptInfo().getDeptName());
        assertEquals(xml.getEmpName(), ann.getEmpName());
        assertEquals(xml.getDeptInfo().getDeptName(), ann.getDeptInfo().getDeptName());

        System.out.println("\n--- 一对多 ---");
        Dept deptXml = deptMapper.one2manyByXml(10);
        Dept deptAnn = deptMapper.one2manyByAnn(10);
        System.out.println("XML: " + deptXml.getDeptName() + " 员工数=" + deptXml.getEmps().size());
        System.out.println("Ann: " + deptAnn.getDeptName() + " 员工数=" + deptAnn.getEmps().size());
        assertEquals(deptXml.getDeptName(), deptAnn.getDeptName());
        assertEquals(deptXml.getEmps().size(), deptAnn.getEmps().size());

        System.out.println("\n--- 多对多 ---");
        Emp m2mXml = empMapper.many2manyByXml(1);
        Emp m2mAnn = empMapper.many2manyByAnn(1);
        System.out.println("XML: " + m2mXml.getEmpName() + " 技能数=" + m2mXml.getSkills().size());
        System.out.println("Ann: " + m2mAnn.getEmpName() + " 技能数=" + m2mAnn.getSkills().size());
        assertEquals(m2mXml.getSkills().size(), m2mAnn.getSkills().size());
    }
}
