package com.offblink;

import com.offblink.entity.Emp;
import com.offblink.mapper.EmpMapper;
import com.offblink.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * EmpMapper XML 版测试（指导书任务 2 + 任务 3 动态 SQL）
 * 每个用例自包含：自造数据自清理，不依赖种子数据的 id，也允许重复运行
 */
public class EmpMapperTest {

    private EmpMapper mapper;
    private SqlSession session;

    @Before
    public void init() {
        session = MyBatisUtil.openSession();
        mapper = session.getMapper(EmpMapper.class);
    }

    private Emp newEmp(String name, String gender, String dept, String post, String salary) {
        Emp e = new Emp();
        e.setEmpName(name);
        e.setGender(gender);
        e.setDept(dept);
        e.setPost(post);
        e.setSalary(new BigDecimal(salary));
        e.setHireDate(new Date());
        e.setStatus(1);
        return e;
    }

    @Test
    public void testSelectAll() {
        System.out.println("========== Emp：查询所有（resultMap 映射验证） ==========");

        // 这里，调用了selectAll，为啥得到的是emp列表呢？
        // 原因很简单，因为我们的type是emp
        List<Emp> list = mapper.selectAll();
        for (Emp e : list) {
            System.out.println(e);
        }
        assertEquals(4, list.size());
        // resultMap 生效：emp_id/emp_name/hire_date 正确映射进属性
        assertNotNull(list.get(0).getEmpName());
        assertNotNull(list.get(0).getHireDate());
    }

    @Test
    public void testCrudLifecycle() {
        System.out.println("========== Emp：增改查删生命周期（XML 版） ==========");
        Emp emp = newEmp("演练员工", "男", "研发部", "测试工程师", "9000");

        // 增：XML 版 useGeneratedKeys 回填 empId
        int rows = mapper.insertEmp(emp);
        System.out.println("新增影响行数：" + rows + "，回填主键 empId = " + emp.getEmpId());
        assertEquals(1, rows);
        assertNotNull(emp.getEmpId());

        // 改
        emp.setPost("高级测试工程师");
        emp.setSalary(new BigDecimal("11000"));
        assertEquals(1, mapper.updateEmp(emp));

        // 查：确认修改生效
        Emp updated = mapper.selectEmpById(emp.getEmpId());
        System.out.println("更新后查询：" + updated);
        assertEquals("高级测试工程师", updated.getPost());

        // 删：清场
        assertEquals(1, mapper.deleteEmpById(emp.getEmpId()));
        assertNull(mapper.selectEmpById(emp.getEmpId()));

        session.commit();
    }

    @Test
    public void testSelectByCondition() {
        System.out.println("========== Emp：if/where 多条件动态查询 ==========");
        // 组合条件：研发部 + 女 → 只有李娜
        Emp cond = new Emp();
        cond.setGender("女");
        cond.setDept("研发部");
        List<Emp> hit = mapper.selectByCondition(cond);
        System.out.println("研发部+女：" + hit);
        assertEquals(1, hit.size());
        assertEquals("李娜", hit.get(0).getEmpName());

        // 全空条件：where 标签不产生 WHERE 子句 → 全表
        List<Emp> all = mapper.selectByCondition(new Emp());
        System.out.println("空条件全表：" + all.size() + " 条");
        assertEquals(4, all.size());
    }

    @Test
    public void testUpdateDynamic() {
        System.out.println("========== Emp：set 动态更新（只改非空字段） ==========");
        Emp emp = newEmp("动态更新演练", "女", "人事部", "专员", "7000");
        mapper.insertEmp(emp);

        // 只带 salary 和 post：其它字段不受影响（set 标签跳过 null）
        Emp patch = new Emp();
        patch.setEmpId(emp.getEmpId());
        patch.setSalary(new BigDecimal("8800"));
        patch.setPost("主管");
        assertEquals(1, mapper.updateDynamic(patch));

        Emp after = mapper.selectEmpById(emp.getEmpId());
        System.out.println("动态更新后：" + after);
        // BigDecimal 数值相等但标度不同（8800 vs 8800.00），用 compareTo 比较
        assertEquals(0, after.getSalary().compareTo(new BigDecimal("8800")));
        assertEquals("主管", after.getPost());
        assertEquals("动态更新演练", after.getEmpName()); // 未被清空

        mapper.deleteEmpById(emp.getEmpId());
        session.commit();
    }

    @Test
    public void testBatchInsertAndDelete() {
        System.out.println("========== Emp：foreach 批量插入/删除 ==========");
        List<Emp> batch = Arrays.asList(
                newEmp("批量甲", "男", "市场部", "专员", "6000"),
                newEmp("批量乙", "女", "市场部", "专员", "6100"));
        int rows = mapper.insertBatch(batch);
        System.out.println("批量插入影响行数：" + rows + "，甲回填 id=" + batch.get(0).getEmpId()
                + "，乙回填 id=" + batch.get(1).getEmpId());
        assertEquals(2, rows);

        assertEquals(6, mapper.selectAll().size());

        List<Integer> ids = Arrays.asList(batch.get(0).getEmpId(), batch.get(1).getEmpId());
        assertEquals(2, mapper.deleteBatch(ids));
        assertEquals(4, mapper.selectAll().size());

        session.commit();
    }

    @Test
    public void testSelectByChoice() {
        System.out.println("========== Emp：choose/when/otherwise 分支查询 ==========");
        // 有姓名 → 按姓名精确匹配（分支一）
        List<Emp> byName = mapper.selectByChoice("张伟", "人事部");
        assertEquals(1, byName.size());
        assertEquals("张伟", byName.get(0).getEmpName());

        // 无姓名有部门 → 按部门（分支二）
        List<Emp> byDept = mapper.selectByChoice(null, "研发部");
        assertEquals(2, byDept.size());

        // 全空 → otherwise 全表
        List<Emp> all = mapper.selectByChoice(null, null);
        assertEquals(4, all.size());

        session.commit();
    }

    @Test
    public void testSelectByPosts() {
        System.out.println("========== Emp：foreach IN 按岗位批量查询（经理 / 销售员） ==========");
        // 种子数据只有 Java工程师/前端工程师/市场专员/人事专员这四个岗位，
        // 本用例自造「经理」「销售员」各一条，外加一条 Java工程师 当对照，跑完全删（不污染种子表）
        Emp manager = newEmp("IN演练经理", "男", "销售部", "经理", "15000");
        Emp salesman = newEmp("IN演练销售", "女", "销售部", "销售员", "6500");
        Emp other = newEmp("IN演练工程师", "男", "研发部", "Java工程师", "12000");
        assertEquals(3, mapper.insertBatch(Arrays.asList(manager, salesman, other)));

        // 集合里有几个岗位就摊出几个占位符：WHERE post IN (?, ?)
        List<Emp> hits = mapper.selectByPosts(Arrays.asList("经理", "销售员"));
        System.out.println("IN (经理, 销售员) 命中 " + hits.size() + " 条：");
        for (Emp e : hits) {
            System.out.println(e);
        }
        assertEquals(2, hits.size());
        assertEquals("经理", hits.get(0).getPost());
        assertEquals("销售员", hits.get(1).getPost());

        // 集合只放一个岗位同样成立（占位符变成一个）
        List<Emp> onlySalesman = mapper.selectByPosts(Arrays.asList("销售员"));
        System.out.println("IN (销售员) 命中 " + onlySalesman.size() + " 条");
        assertEquals(1, onlySalesman.size());

        // 清场
        List<Integer> ids = Arrays.asList(manager.getEmpId(), salesman.getEmpId(), other.getEmpId());
        assertEquals(3, mapper.deleteBatch(ids));
        assertEquals(4, mapper.selectAll().size());

        session.commit();
    }
}
