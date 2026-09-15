-- 实验一指导书任务 1.1：业务载体库与员工表
-- 修正说明：在指导书原稿基础上补充 IF NOT EXISTS 防重跑报错；其余字段定义保持一致
-- 表名按老师要求为 employer（原稿/早期版本写的是 emp）
CREATE DATABASE IF NOT EXISTS ssm_emp DEFAULT CHARACTER SET utf8mb4;
USE ssm_emp;

CREATE TABLE IF NOT EXISTS employer (
    emp_id    INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工编号',
    emp_name  VARCHAR(50) NOT NULL COMMENT '姓名',
    gender    CHAR(1) DEFAULT '男' COMMENT '性别',
    dept      VARCHAR(50) COMMENT '部门',
    post      VARCHAR(50) COMMENT '岗位',
    salary    DECIMAL(10,2) COMMENT '薪资',
    hire_date DATE COMMENT '入职时间',
    status    TINYINT DEFAULT 1 COMMENT '状态：1在职 0离职'
);

INSERT INTO employer (emp_name, gender, dept, post, salary, hire_date, status) VALUES
('张伟', '男', '研发部', 'Java工程师', 12000.00, '2024-07-01', 1),
('李娜', '女', '研发部', '前端工程师', 10000.00, '2024-08-15', 1),
('王强', '男', '市场部', '市场专员',  8000.00, '2023-03-10', 1),
('赵敏', '女', '人事部', '人事专员',  7500.00, '2022-06-20', 0);
