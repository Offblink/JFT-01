package com.offblink.rel;

import com.offblink.rel.entity.Customer;
import com.offblink.rel.entity.Product;
import com.offblink.rel.mapper.ProductMapper;
import com.offblink.rel.mapper.ProductMapperAnnotation;
import com.offblink.rel.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 功能①：通过商品查询客户和供应商 —— XML 形式 vs 注解 形式。
 * <p>
 * 种子依据 {@code sql/relation_db.sql}：
 * 商品 1 Mate70（供应商华为技术，客户 张三/李四）、商品 5 FreeBuds（供应商华为技术，<b>没有客户</b>）。
 * 用例全部只读，跑多少次结果都一样；客户集合不依赖行序，按成员断言。
 */
public class ProductQueryTest {

    private SqlSession session;
    private ProductMapper xmlMapper;
    private ProductMapperAnnotation annMapper;

    @Before
    public void init() {
        session = MyBatisUtil.openSession();
        xmlMapper = session.getMapper(ProductMapper.class);
        annMapper = session.getMapper(ProductMapperAnnotation.class);
    }

    @After
    public void close() {
        session.close();
    }

    /** XML 形式：一条 LEFT JOIN + 嵌套 resultMap，商品 1 应带出供应商与 2 个客户 */
    @Test
    public void testQueryByProductByXml() {
        Product p = xmlMapper.findDetailByProductId(1);

        assertNotNull("商品 1 应查得到", p);
        assertEquals("Mate70 手机", p.getName());
        assertNotNull("应带出供应商", p.getSupplier());
        assertEquals("华为技术", p.getSupplier().getName());
        assertEquals("供应商外键应归位", Integer.valueOf(1), p.getSupplierId());

        assertNotNull(p.getCustomers());
        assertEquals(2, p.getCustomers().size());
        List<String> names = customerNames(p.getCustomers());
        assertTrue("客户应含 张三，实际：" + names, names.contains("张三"));
        assertTrue("客户应含 李四，实际：" + names, names.contains("李四"));
        System.out.println("[function1/xml] " + p.getName() + " -> supplier=" + p.getSupplier().getName()
                + ", customers=" + names);
    }

    /** 注解 形式：主查询 + @One/@Many 两条嵌套子查询，结果必须与 XML 版同一套数据 */
    @Test
    public void testQueryByProductByAnnotation() {
        Product p = annMapper.findDetailByProductId(1);

        assertNotNull("商品 1 应查得到", p);
        assertEquals("Mate70 手机", p.getName());
        assertNotNull("应带出供应商", p.getSupplier());
        assertEquals("华为技术", p.getSupplier().getName());

        assertEquals(2, p.getCustomers().size());
        List<String> names = customerNames(p.getCustomers());
        assertTrue("客户应含 张三，实际：" + names, names.contains("张三"));
        assertTrue("客户应含 李四，实际：" + names, names.contains("李四"));
        System.out.println("[function1/annotation] " + p.getName() + " -> supplier=" + p.getSupplier().getName()
                + ", customers=" + names);
    }

    /** 空关联边界：商品 5 没有任何客户，供应商照常带出、客户集合必须是空集合而不是 null */
    @Test
    public void testProductWithoutCustomerByXml() {
        Product p = xmlMapper.findDetailByProductId(5);

        assertNotNull(p);
        assertEquals("FreeBuds 耳机", p.getName());
        assertNotNull("无客户也要带出供应商", p.getSupplier());
        assertEquals("华为技术", p.getSupplier().getName());
        assertNotNull("集合应被初始化为 []，而不是留 null", p.getCustomers());
        assertTrue("应为空集合，实际：" + p.getCustomers(), p.getCustomers().isEmpty());
        System.out.println("[function1/no-customer] " + p.getName() + " -> customers=[] (LEFT JOIN 空关联)");
    }

    /** 交付自查：同一条业务查询，XML 与注解两套实现的结果必须完全一致（不一致即有一边列名/select 路径写错） */
    @Test
    public void testXmlVsAnnotationConsistency() {
        Product byXml = xmlMapper.findDetailByProductId(1);
        Product byAnn = annMapper.findDetailByProductId(1);

        assertEquals(byXml.getId(), byAnn.getId());
        assertEquals(byXml.getSupplier().getId(), byAnn.getSupplier().getId());
        assertEquals("两种形式的客户 id 集合必须一致", customerIds(byXml), customerIds(byAnn));
        System.out.println("[function1/consistency] xml == annotation, customers=" + customerIds(byXml));
    }

    private static List<String> customerNames(List<Customer> customers) {
        List<String> names = new ArrayList<>();
        for (Customer c : customers) {
            names.add(c.getName());
        }
        return names;
    }

    /** 客户集合顺序在功能① 的 join 里没有 ORDER BY 保证，比对前排序 */
    private static List<Integer> customerIds(Product product) {
        List<Integer> ids = new ArrayList<>();
        for (Customer c : product.getCustomers()) {
            ids.add(c.getId());
        }
        java.util.Collections.sort(ids);
        return ids;
    }
}
