package com.offblink.rel.mapper;

import com.offblink.rel.entity.Customer;
import com.offblink.rel.entity.Product;
import com.offblink.rel.entity.Supplier;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 功能① 的 <b>注解 形式</b>：通过商品查询它的客户和供应商。
 * <p>
 * 与 XML 形式同一业务、同一套断言，但写法是注解的固定形态：
 * 主查询一条 select，关联对象用 {@code @One(select=...)} / {@code @Many(select=...)} 嵌套子查询——
 * 注解里写 join + 内联多层映射可读性极差，所以注解形式天然长成"嵌套 select"（日志里能看到 N+1 的子查询）。
 * <p>
 * {@code @One/@Many} 的 select 必须写<b>语句全限定 id</b>（命名空间.方法名），短名只在
 * {@code @ResultMap} 引用且同接口时才成立。
 */
public interface ProductMapperAnnotation {

    /**
     * 通过商品查供应商 + 客户（功能①，注解 形式）。
     * supplier_id 列喂给 supplier 的子查询，id 列喂给 customers 的子查询。
     */
    @Select("SELECT id, name, price, spec, supplier_id FROM product WHERE id = #{productId}")
    @Results(id = "productDetailMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "name", property = "name"),
            @Result(column = "price", property = "price"),
            @Result(column = "spec", property = "spec"),
            @Result(column = "supplier_id", property = "supplierId"),
            @Result(property = "supplier", column = "supplier_id",
                    one = @One(select = "com.offblink.rel.mapper.ProductMapperAnnotation.findSupplierById")),
            @Result(property = "customers", column = "id",
                    many = @Many(select = "com.offblink.rel.mapper.ProductMapperAnnotation.findCustomersByProductId"))
    })
    Product findDetailByProductId(Integer productId);

    /** 嵌套子查询：按 supplier_id 查回唯一的供应商 */
    @Select("SELECT id, name, phone, address FROM supplier WHERE id = #{supplierId}")
    Supplier findSupplierById(Integer supplierId);

    /** 嵌套子查询：按商品 id 查客户（商品 : 客户 = 1:n，外键在客户侧） */
    @Select("SELECT id, name, phone, product_id FROM customer WHERE product_id = #{productId} ORDER BY id")
    List<Customer> findCustomersByProductId(Integer productId);
}
