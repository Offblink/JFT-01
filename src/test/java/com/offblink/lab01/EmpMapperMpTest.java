package com.offblink.lab01;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offblink.entity.Emp;
import com.offblink.lab01.EmpMapper;
import com.offblink.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * EmpMapper MyBatis-Plus 测试（指导书任务 4）
 * BaseMapper 零 SQL CRUD + LambdaQueryWrapper 条件构造 + 分页插件
 */
public class EmpMapperMpTest {

    private EmpMapper mapper;

    @Before
    public void init() {
        // MP 分页工厂：分页插件必须程序化注册（指导书 4.5 的 XML 写法无效，见 MyBatisUtil 注释）
        SqlSession session = MyBatisUtil.getMpFactory().openSession(true);
        mapper = session.getMapper(EmpMapper.class);
    }

    @Test
    public void testBaseMapperCrud() {
        System.out.println("========== MP：BaseMapper 零 SQL CRUD ==========");
        // 增：insert 是 BaseMapper 注入的，不需要 XML
        Emp emp = new Emp();
        emp.setEmpName("MP演练员工");
        emp.setGender("男");
        emp.setDept("研发部");
        emp.setPost("Java工程师");
        emp.setSalary(new BigDecimal("9500"));
        emp.setStatus(1);
        int rows = mapper.insert(emp);
        assertEquals(1, rows);
        assertNotNull(emp.getEmpId());
        System.out.println("插入成功，回填 empId = " + emp.getEmpId());

        // 查 / 改 / 删
        Emp loaded = mapper.selectById(emp.getEmpId());
        assertEquals("MP演练员工", loaded.getEmpName());

        loaded.setSalary(new BigDecimal("12000"));
        assertEquals(1, mapper.updateById(loaded));

        assertEquals(1, mapper.deleteById(emp.getEmpId()));
        assertNull(mapper.selectById(emp.getEmpId()));
        System.out.println("BaseMapper 增改查删全通");
    }

    @Test
    public void testLambdaWrapper() {
        System.out.println("========== MP：LambdaQueryWrapper 条件构造 ==========");
        // Lambda 写法：方法引用防字段名写错；boolean 条件参数控制动态拼接
        LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<Emp>()
                .like("张".isEmpty(), Emp::getEmpName, "张")          // 条件为 false，该条件不拼接
                .eq(Emp::getStatus, 1)                                // 只看在职
                .orderByDesc(Emp::getSalary);
        List<Emp> list = mapper.selectList(wrapper);
        for (Emp e : list) {
            System.out.println(e);
        }
        // 在职 3 人按薪资降序：张伟(12000) > 李娜(10000) > 王强(8000)
        assertEquals(3, list.size());
        assertEquals("张伟", list.get(0).getEmpName());
        assertEquals("王强", list.get(2).getEmpName());
    }

    @Test
    public void testPagination() {
        System.out.println("========== MP：分页插件 selectPage ==========");
        // 第 1 页每页 2 条：4 条种子数据 → total=4, pages=2, 本页 2 条
        Page<Emp> page = mapper.selectPage(new Page<>(1, 2), null);
        List<Emp> records = page.getRecords();
        records.forEach(System.out::println);

        System.out.println("总记录数 = " + page.getTotal() + "，总页数 = " + page.getPages());
        assertEquals(4, page.getTotal());
        assertEquals(2, page.getPages());
        // 分页生效的标志：records 只装本页数据而不是全量（未注册插件时这里会返回 4 条）
        assertEquals(2, records.size());
        assertTrue(page.getTotal() > records.size());
    }
}
