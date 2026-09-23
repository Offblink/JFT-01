package com.offblink.rel.mapper;

import com.offblink.rel.entity.Customer;
import com.offblink.rel.entity.Product;
import com.offblink.rel.entity.Supplier;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 功能② 的 <b>注解 形式</b>：通过供应商查询商品和客户。
 * <p>
 * 结构与 XML 形式一一对应（主查询 + 两个嵌套子查询），差别只在写法：
 * 这里全在接口上用 {@code @Select} + {@code @Many(select=...)} 表达。
 * 客户子查询必须穿过 after_sale 中间表——m:n 关系的 SQL 形状在两种形式里是同一条。
 */
public interface SupplierMapperAnnotation {

    /**
     * 通过供应商查商品 + 售后客户（功能②，注解 形式）。
     * 两个集合的 column 都传主键 id，各自去查自己的表。
     */
    @Select("SELECT id, name, phone, address FROM supplier WHERE id = #{supplierId}")
    @Results(id = "supplierDetailMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "name", property = "name"),
            @Result(column = "phone", property = "phone"),
            @Result(column = "address", property = "address"),
            @Result(property = "products", column = "id",
                    many = @Many(select = "com.offblink.rel.mapper.SupplierMapperAnnotation.findProductsBySupplierId")),
            @Result(property = "customers", column = "id",
                    many = @Many(select = "com.offblink.rel.mapper.SupplierMapperAnnotation.findCustomersBySupplierId"))
    })
    Supplier findDetailBySupplierId(Integer supplierId);

    /** 嵌套子查询：本供应商名下的商品（1:n，外键在商品侧） */
    @Select("SELECT id, name, price, spec, supplier_id FROM product WHERE supplier_id = #{supplierId} ORDER BY id")
    List<Product> findProductsBySupplierId(Integer supplierId);

    /** 嵌套子查询：穿过 after_sale 中间表，拿与本供应商有售后关系的客户（m:n） */
    @Select("SELECT c.id, c.name, c.phone, c.product_id FROM customer c "
            + "JOIN after_sale a ON a.customer_id = c.id "
            + "WHERE a.supplier_id = #{supplierId} ORDER BY c.id")
    List<Customer> findCustomersBySupplierId(Integer supplierId);
}
