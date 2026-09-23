package com.offblink.rel.entity;

import java.util.List;

/**
 * 供应商（表 relation_db.supplier）
 * <p>
 * 本练习的三实体关系：
 * <ul>
 *   <li>供应商 : 商品 = 1 : n —— 外键在 product.supplier_id；</li>
 *   <li>客户 : 供应商 = m : n —— 中间表 after_sale，业务含义"售后"。</li>
 * </ul>
 * products / customers 不是表字段，只承载功能②"通过供应商查询商品和客户"的结果。
 */
public class Supplier {

    private Integer id;
    private String name;
    private String phone;
    private String address;

    /** 功能②结果：该供应商名下的全部商品（1:n 的"多"侧，查 product 表） */
    private List<Product> products;

    /** 功能②结果：与该供应商有售后关系的全部客户（m:n 的另一侧，穿过 after_sale 中间表） */
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    @Override
    public String toString() {
        return "Supplier{id=" + id + ", name='" + name + "', phone='" + phone + "', address='" + address
                + "', products=" + products + ", customers=" + customers + '}';
    }
}
