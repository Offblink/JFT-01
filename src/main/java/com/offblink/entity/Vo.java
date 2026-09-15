package com.offblink.entity;

/**
 * 值对象（VO，第 4 节）：专门用来封装「查询结果」，与数据库表结构解耦。
 *
 * <p>属性和表列名毫无关系（P1/P2/P3），故意起这种名字是为了演示 resultMap 的核心能力：
 * 只要写明「谁映射到谁」，结果集就能装进任意模型——实体类、VO、甚至只用到两三个字段的残缺模型。
 * 对比 resultType 的自动映射：它要求列名（或别名）与属性名对得上，对不上就丢字段。</p>
 *
 * <p>注意字段名首字母大写（P1），MyBatis 反射时按 getter 取名并规范化成 p1，
 * 所以 SQL 里的别名/映射写成 p1、p2、p3（小写）即可。</p>
 */
public class Vo { //创建一个VO类 用于封装查询结果

    private Integer P1;
    private String P2;
    private String P3;

    public Vo() {
    }

    // 可这几个起名起得不明所以的属性，怪不得要映射呢😐
    public Vo(Integer p1, String p2, String p3) {
        P1 = p1;
        P2 = p2;
        P3 = p3;
    }

    public Integer getP1() {
        return P1;
    }

    public void setP1(Integer p1) {
        P1 = p1;
    }

    public String getP2() {
        return P2;
    }

    public void setP2(String p2) {
        P2 = p2;
    }

    public String getP3() {
        return P3;
    }

    public void setP3(String p3) {
        P3 = p3;
    }

    @Override
    public String toString() {
        return "Vo{" +
                "P1=" + P1 +
                ", P2='" + P2 + '\'' +
                ", P3='" + P3 + '\'' +
                '}';
    }
}
