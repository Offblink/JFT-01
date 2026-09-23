package com.offblink.rel;

import com.offblink.rel.entity.Customer;
import com.offblink.rel.entity.Product;
import com.offblink.rel.entity.Supplier;
import com.offblink.rel.mapper.SupplierMapper;
import com.offblink.rel.mapper.SupplierMapperAnnotation;
import com.offblink.rel.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 功能②：通过供应商查询商品和客户 —— XML 形式 vs 注解 形式。
 * <p>
 * 种子依据 {@code sql/relation_db.sql}：
 * 供应商 1 华为技术（3 商品、售后客户 张三/王五）、供应商 2 小米科技（2 商品、售后客户 赵六/孙七）、
 * 供应商 3 戴尔电脑（1 商品、<b>没有任何售后</b>，验 m:n 空关联）。
 * 客户是穿过 after_sale 中间表（关系"售后"）查回来的——两个功能里唯一的 m:n 路径。
 * 用例全部只读，商品集合带 ORDER BY id、客户集合带 ORDER BY c.id，断言可直接比列表。
 */
public class SupplierQueryTest {

    private SqlSession session;
    private SupplierMapper xmlMapper;
    private SupplierMapperAnnotation annMapper;

    @Before
    public void init() {
        session = MyBatisUtil.openSession();
        xmlMapper = session.getMapper(SupplierMapper.class);
        annMapper = session.getMapper(SupplierMapperAnnotation.class);
    }

    @After
    public void close() {
        session.close();
    }

    /** XML 形式：主查询 + 两条嵌套子查询（商品直查、客户穿中间表） */
    @Test
    public void testQueryBySupplierByXml() {
        Supplier s = xmlMapper.findDetailBySupplierId(1);

        assertNotNull("供应商 1 应查得到", s);
        assertEquals("华为技术", s.getName());

        assertNotNull(s.getProducts());
        assertEquals("华为名下 3 个商品", Arrays.asList("Mate70 手机", "MateBook 笔记本", "FreeBuds 耳机"),
                productNames(s.getProducts()));

        assertNotNull(s.getCustomers());
        assertEquals("售后客户 2 个（张三、王五）", Arrays.asList("张三", "王五"), customerNames(s.getCustomers()));
        System.out.println("[function2/xml] " + s.getName() + " -> products=" + productNames(s.getProducts())
                + ", customers=" + customerNames(s.getCustomers()));
    }

    /** 注解 形式：@Many 嵌套子查询，拿供应商 2 验同一套断言形状 */
    @Test
    public void testQueryBySupplierByAnnotation() {
        Supplier s = annMapper.findDetailBySupplierId(2);

        assertNotNull("供应商 2 应查得到", s);
        assertEquals("小米科技", s.getName());

        assertEquals("小米名下 2 个商品", Arrays.asList("小米手环9", "AX6000 路由器"), productNames(s.getProducts()));
        assertEquals("售后客户 2 个（赵六、孙七）", Arrays.asList("赵六", "孙七"), customerNames(s.getCustomers()));
        System.out.println("[function2/annotation] " + s.getName() + " -> products=" + productNames(s.getProducts())
                + ", customers=" + customerNames(s.getCustomers()));
    }

    /** m:n 空关联边界：戴尔没有登记任何售后，客户集合应为空集合；商品照常带出 */
    @Test
    public void testSupplierWithoutAfterSale() {
        Supplier s = xmlMapper.findDetailBySupplierId(3);

        assertNotNull(s);
        assertEquals("戴尔电脑", s.getName());
        assertEquals(Arrays.asList("XPS 台式机"), productNames(s.getProducts()));
        assertNotNull("m:n 子查询返回空时集合应是 []，而不是 null", s.getCustomers());
        assertTrue("无售后记录应为空集合，实际：" + customerNames(s.getCustomers()), s.getCustomers().isEmpty());
        System.out.println("[function2/no-after-sale] " + s.getName() + " -> customers=[]");
    }

    /** 交付自查：同一条业务查询，XML 与注解两套实现的结果必须完全一致 */
    @Test
    public void testXmlVsAnnotationConsistency() {
        Supplier byXml = xmlMapper.findDetailBySupplierId(1);
        Supplier byAnn = annMapper.findDetailBySupplierId(1);

        assertEquals(byXml.getId(), byAnn.getId());
        assertEquals("商品列表必须一致", productNames(byXml.getProducts()), productNames(byAnn.getProducts()));
        assertEquals("售后客户列表必须一致", customerNames(byXml.getCustomers()), customerNames(byAnn.getCustomers()));
        System.out.println("[function2/consistency] xml == annotation, products="
                + productNames(byXml.getProducts()) + ", customers=" + customerNames(byXml.getCustomers()));
    }

    private static List<String> productNames(List<Product> products) {
        List<String> names = new ArrayList<>();
        for (Product p : products) {
            names.add(p.getName());
        }
        return names;
    }

    private static List<String> customerNames(List<Customer> customers) {
        List<String> names = new ArrayList<>();
        for (Customer c : customers) {
            names.add(c.getName());
        }
        return names;
    }
}
