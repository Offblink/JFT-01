package com.offblink.rel.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * 会话工厂工具类（纯 MyBatis 版，不涉 MyBatis-Plus，所以比根项目的双工厂简单）。
 * <p>
 * 工厂重量级、进程一个即可：懒加载 + 双检锁构建，读 classpath 根下的 mybatis-config.xml。
 * 测试侧 {@code @Before openSession()} / {@code @After close()}，一用例一会话。
 */
public final class MyBatisUtil {

    private static volatile SqlSessionFactory factory;

    private MyBatisUtil() {
    }

    /** 通用工厂：读 mybatis-config.xml，含 XML 形式与注解 形式的全部 Mapper 注册 */
    public static SqlSessionFactory getFactory() {
        if (factory == null) {
            synchronized (MyBatisUtil.class) {
                if (factory == null) {
                    try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
                        factory = new SqlSessionFactoryBuilder().build(in);
                    } catch (IOException e) {
                        throw new IllegalStateException("mybatis-config.xml 加载失败", e);
                    }
                }
            }
        }
        return factory;
    }

    /** 开一个会话（手动提交：本练习用例全只读，close 即回滚，无副作用） */
    public static SqlSession openSession() {
        return getFactory().openSession();
    }
}
