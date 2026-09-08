package com.offblink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offblink.entity.Emp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工 Mapper（实验一指导书任务 2.3 / 3 / 4.3）
 * 继承 BaseMapper 获得零 SQL 通用 CRUD；自定义方法走 XML（resources/com/offblink/mapper/EmpMapper.xml）。
 * 自定义方法名刻意避开 BaseMapper 同名方法（insert/selectById/deleteById/updateById），避免注入冲突。
 */
public interface EmpMapper extends BaseMapper<Emp> {

    /* ==================== XML 版基础 CRUD（resultMap 映射） ==================== */

    /** 查询所有员工 */
    List<Emp> selectAll();

    /** 根据员工编号查询 */
    Emp selectEmpById(Integer empId);

    /** 新增员工（XML 版自增主键回填 empId） */
    int insertEmp(Emp emp);

    /** 全字段更新 */
    int updateEmp(Emp emp);

    /** 删除员工 */
    int deleteEmpById(Integer empId);

    /* ==================== 动态 SQL（指导书任务 3） ==================== */

    /** 多条件动态查询（if / where） */
    List<Emp> selectByCondition(Emp cond);

    /** 动态更新（set：只更新非空字段） */
    int updateDynamic(Emp emp);

    /** 批量删除（foreach） */
    int deleteBatch(@Param("ids") List<Integer> ids);

    /** 批量插入（foreach） */
    int insertBatch(@Param("list") List<Emp> emps);

    /** 分支选择（choose / when / otherwise）：有姓名按姓名，否则有部门按部门，否则查全部 */
    List<Emp> selectByChoice(@Param("empName") String empName, @Param("dept") String dept);
}
