# JFT-01 · mybatis01

《JAVA框架技术（一）》（AI 赋能版）实验一：**MyBatis 环境搭建与基础 CRUD 实操**。
以 AI 辅助、人脑主导的方式完成，全程保留开发轨迹（Git 提交历史即过程记录）。

## 技术栈

| 类别 | 选型 |
|---|---|
| 语言 / JDK | Java 8 语法目标（老师推荐 JDK 21 运行） |
| 构建 | Maven 3.9.x（阿里云镜像），war 打包 |
| 持久层 | MyBatis 3.5.13 |
| 数据库 | MySQL 8/9（`mysql-connector-j` 8.0.33） |
| 测试 | JUnit 4.13.2 |
| 日志 | SLF4J 门面 + Logback 1.2.13（控制台 + 滚动文件双输出） |
| Web | JavaEE（javax.servlet-api 4.0.1，web.xml） |

## 功能

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

## 快速开始

```bash
# 1. 建库建表（MySQL 8+）
mysql -u root -p < sql/user_db.sql

# 2. 配置数据库连接（占位符改为你自己的本地配置）
#    src/main/resources/db.properties

# 3. 跑测试
mvn test -Dtest=UserMapperTest
```

> `db.properties` 仓库内为占位配置，本地实际配置不提交（skip-worktree），请勿将真实口令提交到仓库。
>
> 其中 JDBC URL 需要 `allowMultiQueries=true`（批量更新 `updateBatch` 用 `<foreach>` 分号拼多条 UPDATE，
> 不开这个开关驱动会直接报语法错误）；`allowPublicKeyRetrieval=true` 是 MySQL 8/9 在 `useSSL=false` 下必须的。

## 项目结构

```
├── sql/user_db.sql                     # 建库建表 + 测试数据
├── src/main/java/com/offblink/
│   ├── entity/User.java                # 实体类（驼峰映射 created_at → createdAt）
│   ├── entity/Vo.java                  # 查询结果 VO（第 4 节：resultMap 对接「任意模型」的演示）
│   └── mapper/UserMapper.java          # Mapper 接口（动态代理）
├── src/main/resources/
│   ├── db.properties                   # 连接配置（占位）
│   ├── mybatis-config.xml              # 全局配置（logImpl=SLF4J、驼峰、typeAliases）
│   ├── logback.xml                     # 日志：控制台 + logs/mybatis.log 滚动文件
│   └── mapper/UserMapper.xml           # SQL 映射
├── src/main/webapp/                    # JavaEE web 骨架
├── src/test/java/com/offblink/UserMapperTest.java
└── docs/                               # 实验报告草稿、会话回顾（AI 协作过程记录）
```

## 文档

- [实验报告草稿](docs/实验报告-实验一-草稿.md)
- [会话回顾](docs/会话回顾-实验一-20260901.md) —— AI 辅助开发全程记录与人工校验过程

## License

[MIT](LICENSE)
