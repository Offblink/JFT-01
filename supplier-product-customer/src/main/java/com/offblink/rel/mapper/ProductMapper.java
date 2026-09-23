package com.offblink.rel.mapper;

import com.offblink.rel.entity.Product;

/**
 * 功能① 的 <b>XML 形式</b>：通过商品查询它的客户和供应商。
 * <p>
 * 同包同名 XML：{@code resources/com/offblink/rel/mapper/ProductMapper.xml}。
 * 实现是一条 {@code LEFT JOIN} 把三张表的列一次查回来，靠嵌套 resultMap 归位——
 * 这是 XML 形式的强项（一条 SQL、无 N+1）。
 */
public interface ProductMapper {

    /**
     * 通过商品查供应商 + 客户。
     *
     * @param productId 商品 id
     * @return 商品本体，携带 supplier（一）与 customers（多）；商品不存在返回 null
     */
    Product findDetailByProductId(Integer productId);
}
