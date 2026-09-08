package com.offblink.util;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import com.offblink.mapper.EmpMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * MyBatis 会话工厂工具类（实验一指导书步骤 2.4 要求封装，避免每个测试重复建工厂）
 * <p>
 * 双工厂设计：
 * <ul>
 *   <li>{@link #getFactory()}：由 mybatis-config.xml 驱动，服务全部 XML/注解 Mapper
 *       （用 MP 的 MybatisSqlSessionFactoryBuilder 构建——它内部用 MybatisConfiguration，
 *       对普通 Mapper 完全兼容，同时让 EmpMapper 继承 BaseMapper 获得注入的通用方法）；</li>
 *   <li>{@link #getMpFactory()}：程序化配置，专供分页测试。指导书 4.5 的 XML
 *       &lt;property name="@..."&gt; 写法无法构造 InnerInterceptor（分页静默不生效，返回全量），
 *       插件必须代码注册。</li>
 * </ul>
 */
public final class MyBatisUtil {

    private static volatile SqlSessionFactory factory;
    private static volatile SqlSessionFactory mpFactory;

    private MyBatisUtil() {
    }

    /** 通用工厂：读 mybatis-config.xml（MP 构建器，兼容 BaseMapper 注入） */
    public static SqlSessionFactory getFactory() {
        if (factory == null) {
            synchronized (MyBatisUtil.class) {
                if (factory == null) {
                    try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
                        factory = new MybatisSqlSessionFactoryBuilder().build(in);
                    } catch (Exception e) {
                        throw new IllegalStateException("构建 SqlSessionFactory 失败", e);
                    }
                }
            }
        }
        return factory;
    }

    public static SqlSession openSession() {
        return getFactory().openSession();
    }

    public static SqlSession openSession(boolean autoCommit) {
        return getFactory().openSession(autoCommit);
    }

    /** MP 分页专用工厂：MybatisConfiguration 程序化构建 + 分页插件 */
    public static SqlSessionFactory getMpFactory() {
        if (mpFactory == null) {
            synchronized (MyBatisUtil.class) {
                if (mpFactory == null) {
                    MybatisConfiguration cfg = new MybatisConfiguration();
                    cfg.setLogImpl(Slf4jImpl.class);
                    cfg.setMapUnderscoreToCamelCase(true);
                    cfg.setEnvironment(new Environment("mp", new JdbcTransactionFactory(), pooledDataSource()));
                    // 程序化配置不读 mybatis-config.xml，实体别名需单独注册（XML 里 resultType="Emp" 依赖它）
                    cfg.getTypeAliasRegistry().registerAliases("com.offblink.entity");
                    // 接口与 XML 同包同名（resources/com/offblink/mapper/EmpMapper.xml），addMapper 自动加载 XML
                    cfg.addMapper(EmpMapper.class);

                    // 正确的分页插件注册：MybatisPlusInterceptor + PaginationInnerInterceptor
                    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
                    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
                    cfg.addInterceptor(interceptor);

                    mpFactory = new MybatisSqlSessionFactoryBuilder().build(cfg);
                }
            }
        }
        return mpFactory;
    }

    /** 连接四要素外置在 db.properties（与 mybatis-config.xml 同源），不硬编码 */
    private static PooledDataSource pooledDataSource() {
        try (InputStream in = Resources.getResourceAsStream("db.properties")) {
            Properties p = new Properties();
            p.load(in);
            return new PooledDataSource(
                    p.getProperty("jdbc.driver"),
                    p.getProperty("jdbc.url"),
                    p.getProperty("jdbc.username"),
                    p.getProperty("jdbc.password"));
        } catch (Exception e) {
            throw new IllegalStateException("读取 db.properties 失败", e);
        }
    }
}
