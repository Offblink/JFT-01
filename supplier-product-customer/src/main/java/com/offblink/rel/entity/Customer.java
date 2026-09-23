package com.offblink.rel.entity;

/**
 * 客户（表 relation_db.customer）
 * <p>
 * product_id 是"商品 : 客户 = 1:n"的挂点（一个客户只归属一个商品，多侧外键）；
 * 与供应商的 m:n"售后"关系存在 after_sale 中间表里，本实体不冗余存供应商：
 * 从供应商查客户（功能②）要穿过 after_sale。
 */
public class Customer {

    private Integer id;
    private String name;
    private String phone;

    /** 外键：本客户归属哪个商品（商品 : 客户 = 1:n 的挂点） */
    private Integer productId;

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

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', phone='" + phone + "', productId=" + productId + '}';
    }
}
