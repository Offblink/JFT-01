# JFT-01 · mybatis01

《JAVA框架技术（一）》（AI 赋能版）课程项目。以 AI 辅助、人脑主导的方式完成，全程保留开发轨迹
（Git 提交历史即过程记录）。当前进度：**实验一（MyBatis + MyBatis-Plus）** + **第 5 章 关联映射与多表查询**。

## 技术栈

| 类别 | 选型 |
|---|---|
| 语言 / JDK | Java 8 语法目标（IDEA 运行 JDK 见下） |
| 构建 | Maven 3.9.x（阿里云镜像），war 打包 |
| 持久层 | MyBatis-Plus 3.5.3.1（内置 MyBatis 3.5.10，`EmpMapper extends BaseMapper`） |
| 数据库 | MySQL 8/9（`mysql-connector-j` 8.0.33） |
| 测试 | JUnit 4.13.2 |
| 日志 | SLF4J 门面 + Logback 1.2.13（控制台 + 滚动文件双输出） |
| Web | JavaEE（javax.servlet-api 4.0.1，web.xml） |

## 功能

**实验一：MyBatis 环境搭建 + CRUD + 动态 SQL + MyBatis-Plus**

- 用户表完整 CRUD：`findAll` / `findById` / `addUser`（自增主键回填）/ `updateUser` / `deleteUser`
- 用户名模糊查询 `findByUsernameLike`（`LIKE CONCAT('%', #{keyword}, '%')` 预编译防注入）
- 生命周期测试：以回填的主键 id 贯穿增→改→查→删，无顺序依赖，跑完自清理
- 结果映射（第 4 节）：
  - XML 侧三连对照——① `findAllWithColumnAlias`（别名 `uid` 对不上属性名 `id`，resultType 丢字段）、
    ② `findAllByVoWithResultType`（别名即契约，靠 `p1/p2/p3` 对上 VO）、③ `findAllByVoMap`（`resultMap` 集中声明映射规则）
  - 注解侧两式——`@Results` 匿名映射成 VO；`@Results(id="annoUserMap")` 命名后由 `@ResultMap("annoUserMap")` 复用
- 动态 SQL（第 4-2 节）：
  - XML 六种标签：`<if>`+`<where>` 条件组合、`<set>` 选择性更新、`<choose>` 互斥分支、
    `<foreach>` IN 查询与一条 INSERT 批量插入、`<trim>` 自定义裁剪、`<sql>`+`<include>` 片段复用
  - 注解线：`<script>` 包住动态标签的写法，以及注解 SQL 用全限定名 `@ResultMap` 跨方式引用 XML 的 `userResultMap`
- MyBatis-Plus：`BaseMapper` 零 SQL CRUD、`LambdaQueryWrapper`、分页插件（`MybatisPlusInterceptor` 代码注册）
- emp 载体（指导书任务 2/3 要求）：`ssm_emp.employer` 表 + `EmpMapper`（resultMap / sql 片段 / 批量插入 /
  `<foreach>` IN 按岗位查询 / 分号拼多条 UPDATE 的一次性批量更新）

**第 5 章：关联映射与多表查询（XML 版 + 注解版各一套）**

| 关系 | XML 方式（嵌套结果，1 条 SQL） | 注解方式（嵌套 select，1+N 条） |
|---|---|---|
| 一对一 / 多对一 | `EmpRelationMapper.one2oneByXml` / `many2oneByXml`（共用 `empWithDeptMap`） | `one2oneByAnn` / `many2oneByAnn`（`@One`） |
| 一对多 | `DeptRelationMapper.one2manyByXml`（`<collection ofType="Emp">`） | `one2manyByAnn`（`@Many`） |
| 多对多 | `EmpRelationMapper.many2manyByXml`（两次 JOIN 穿中间表） | `many2manyByAnn`（`@Many` 调 `selectSkillsByEmpId`） |

## 快速开始

```bash
# 1. 建库建表（MySQL 8+）——实验一建库建表在前，第 5 章的关联三表在其后执行
mysql -u root -p < sql/实验一/user_db.sql
mysql -u root -p < sql/实验一/ssm_emp.sql
mysql -u root -p < sql/第5章/dept_skill.sql     # 可重复执行（会补 dept/skill/employer_skill + employer.dept_id）

# 2. 配置数据库连接（占位符改为你自己的本地配置）
#    src/main/resources/db.properties

# 3. 跑全量测试（当前 47 个用例）
mvn test
```

> `db.properties` 仓库内为占位配置（`jdbc.password=CHANGE_ME`），本地实际配置不提交（skip-worktree），
> 请勿将真实口令提交到仓库。
>
> 其中 JDBC URL 需要 `allowMultiQueries=true`（批量更新 `updateBatch` 用 `<foreach>` 分号拼多条 UPDATE，
> 不开这个开关驱动会直接报语法错误）；`allowPublicKeyRetrieval=true` 是 MySQL 8/9 在 `useSSL=false` 下必须的。

## 项目结构

```
├── sql/
│   ├── 实验一/{user_db.sql, ssm_emp.sql}          # mybatis_db / ssm_emp 建库建表 + 种子数据
│   └── 第5章/dept_skill.sql                       # dept / skill / employer_skill 中间表 + employer.dept_id
├── src/main/java/com/offblink/
│   ├── entity/{Emp, User, Vo}.java                # 实验一实体
│   ├── entity/{Dept, Skill}.java                  # 第 5 章：关联映射的"一方"与多对多另一侧
│   ├── mapper/{EmpMapper, UserMapper, UserMapperAnnotation}.java
│   ├── mapper/{EmpRelationMapper, DeptRelationMapper}.java       # 第 5 章：XML + 注解两套关联查询
│   └── util/MyBatisUtil.java                      # 会话工厂（通用工厂 + MP 分页专用工厂）
├── src/main/resources/
│   ├── db.properties                              # 连接配置（占位）
│   ├── mybatis-config.xml                         # logImpl=SLF4J、驼峰、typeAliases、延迟加载、mapper 注册
│   ├── logback.xml                                # 日志：控制台 + logs/mybatis.log 滚动文件
│   ├── mapper/UserMapper.xml                      # 扁平目录 → <mapper resource> 注册
│   └── com/offblink/mapper/*.xml                  # 与接口同包同名 → <mapper class> 注册
├── src/main/webapp/                               # JavaEE web 骨架
├── src/test/java/com/offblink/                    # UserMapperTest / UserMapperAnnotationTest
│   │                                              # EmpMapperTest / EmpMapperMpTest / EmpRelationMapperTest
└── docs/                                          # 过程材料（报告草稿、会话回顾、排查演练、章节实测记录）
```

## 文档

- 实验一：[实验报告草稿](docs/实验一/实验报告-实验一-草稿.md) ｜
  [会话回顾](docs/实验一/会话回顾-实验一-20260901.md)（AI 辅助开发全程记录与人工校验过程）｜
  [日志排查演练](docs/实验一/日志排查演练-实验一.md)
- 第 5 章：[关联映射实测记录](docs/第5章/关联映射实测记录.md)（两套实现的 SQL 条数、三处与讲义不一致的实测结论）

## License

[MIT](LICENSE)
