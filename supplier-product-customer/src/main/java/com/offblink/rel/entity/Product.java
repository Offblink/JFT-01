package com.offblink.rel.entity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品（表 relation_db.product）
 * <p>
 * 两条外键关系都经过它：
 * <ul>
 *   <li>supplier_id —— 供应商 : 商品 = 1 : n，本实体是"多"侧，持有一个供应商对象（功能①）；</li>
 *   <li>客户侧挂在 customer.product_id —— 商品 : 客户 = 1 : n，本实体是"一"侧，持有客户集合（功能①）。</li>
 * </ul>
 * supplier / customers 不是表字段，只承载功能①"通过商品查询客户和供应商"的结果。
 */
public class Product {

    private Integer id;
    private String name;
    private BigDecimal price;
    private String spec;

    /** 外键：本商品属于哪个供应商（供应商 : 商品 = 1:n 的挂点） */
    private Integer supplierId;

    /** 功能①结果：本商品的供应商（n:1 的"一"侧） */
    private Supplier supplier;

    /** 功能①结果：购买过本商品的全部客户（商品 : 客户 = 1:n 的"多"侧） */
    private List<Customer> customers;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + ", spec='" + spec
                + "', supplierId=" + supplierId + ", supplier=" + supplier + ", customers=" + customers + '}';
    }
}
