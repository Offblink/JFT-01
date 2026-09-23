package com.offblink.rel.mapper;

import com.offblink.rel.entity.Customer;
import com.offblink.rel.entity.Product;
import com.offblink.rel.entity.Supplier;

import java.util.List;

/**
 * 功能② 的 <b>XML 形式</b>：通过供应商查询商品和客户。
 * <p>
 * 同包同名 XML：{@code resources/com/offblink/rel/mapper/SupplierMapper.xml}。
 * 主查询带两个集合，且两个集合来自两张不同的表（product 直连、customer 要穿 after_sale 中间表）——
 * 一条 join 会把两组子行做成笛卡尔积，所以这里用嵌套 select（{@code <collection select=...>}），
 * 主查询 1 条 + 每个集合各 1 条子查询。
 */
public interface SupplierMapper {

    /**
     * 通过供应商查商品 + 售后客户。
     *
     * @param supplierId 供应商 id
     * @return 供应商本体，携带 products（1:n 的多侧）与 customers（m:n 的售后侧）；不存在返回 null
     */
    Supplier findDetailBySupplierId(Integer supplierId);

    /** 功能② 的商品子查询：本供应商名下的全部商品（也是嵌套 select 的被引语句） */
    List<Product> findProductsBySupplierId(Integer supplierId);

    /** 功能② 的客户子查询：穿过 after_sale 中间表拿与本供应商有售后关系的客户（也是嵌套 select 的被引语句） */
    List<Customer> findCustomersBySupplierId(Integer supplierId);
}
