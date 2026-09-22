package com.offblink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 员工实体类（实验一指导书任务 2.2 / 4.2）
 * 对应数据库表：ssm_emp.employer（老师要求的表名，早期版本为 emp）
 * 注意：指导书原稿在 status 上加了 @TableLogic——status 是业务字段（1在职 0离职），
 * 照抄会把离职员工在 MP 查询里过滤"消失"，故不加（正确做法是独立 deleted 字段）
 */
@TableName("ssm_emp.employer")
public class Emp {
    @TableId(value = "emp_id", type = IdType.AUTO)
    private Integer empId;
    @TableField("emp_name")
    private String empName;
    private String gender;
    private String dept;
    private String post;
    private BigDecimal salary;
    private Date hireDate;
    private Integer status;

    /** 所属部门编号（外键 → ssm_emp.dept.dept_id）：第 5 章新加的列，列名 dept_id 靠驼峰自动对上 */
    private Integer deptId;

    /**
     * 关联出来的部门对象（第 5 章 &lt;association&gt; / @One 的落点）
     * 和上面的 String dept 别搞混：dept 是实验一遗留的部门"文本列"（'研发部'），
     * deptInfo 是连表查出来的整个部门对象（编号/名称/地点）
     * <p>
     * @TableField(exist = false)：本类同时是 MyBatis-Plus 实体（EmpMapper extends BaseMapper&lt;Emp&gt;），
     * 必须告诉 MP「这不是表字段」——否则 MP 会把它当成一列拼进 SELECT/INSERT，直接报 Unknown column。
     */
    @TableField(exist = false)
    private Dept deptInfo;

    /** 员工掌握的技能集合（第 5 章多对多 &lt;collection&gt; 的落点），同样不是表字段 */
    @TableField(exist = false)
    private List<Skill> skills;

    public Emp() {
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Dept getDeptInfo() {
        return deptInfo;
    }

    public void setDeptInfo(Dept deptInfo) {
        this.deptInfo = deptInfo;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    /** toString 只打本表字段：关联对象（deptInfo/skills）容易刷屏且可能触发递归打印，由用例按需要自己取 */
    @Override
    public String toString() {
        return "Emp{" +
                "empId=" + empId +
                ", empName='" + empName + '\'' +
                ", gender='" + gender + '\'' +
                ", dept='" + dept + '\'' +
                ", post='" + post + '\'' +
                ", salary=" + salary +
                ", hireDate=" + hireDate +
                ", status=" + status +
                '}';
    }
}
