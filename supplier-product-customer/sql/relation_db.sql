-- ============================================================
-- 三实体关系练习：供应商 supplier / 商品 product / 客户 customer
--
-- 关系（本练习的全部模型）：
--   供应商 : 商品 = 1 : n   外键挂商品侧 product.supplier_id
--   商品   : 客户 = 1 : n   外键挂客户侧 customer.product_id
--   客户 : 供应商 = m : n   中间表 after_sale，业务含义"售后"
--
-- 独立库 relation_db：不碰课程库 mybatis_db / ssm_emp，
-- 根项目 47 个用例的计数断言与本库无关。
-- 重跑本脚本 = 丢弃重建 + 重新播种，测试断言永远对同一份数据。
-- 导入注意（本机坑）：mysql 客户端必须带 --default-character-set=utf8mb4，
-- 否则中文种子会报 Cannot convert string ... from utf8mb4 to gbk（ERROR 3854）。
-- ============================================================

DROP DATABASE IF EXISTS relation_db;
CREATE DATABASE relation_db DEFAULT CHARACTER SET utf8mb4;
USE relation_db;

-- 供应商
CREATE TABLE supplier (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(50)  NOT NULL,
    phone   VARCHAR(20),
    address VARCHAR(100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 商品：n 侧挂外键 → 供应商 : 商品 = 1:n
CREATE TABLE product (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    spec        VARCHAR(50),
    supplier_id INT           NOT NULL,
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 客户：n 侧挂外键 → 商品 : 客户 = 1:n
CREATE TABLE customer (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(50) NOT NULL,
    phone      VARCHAR(20),
    product_id INT         NOT NULL,
    CONSTRAINT fk_customer_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 售后：客户 : 供应商 = m:n 的中间表
-- UNIQUE 保证同一对 (客户, 供应商) 只登记一次，多对多集合断言才稳定
CREATE TABLE after_sale (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    supplier_id INT NOT NULL,
    content     VARCHAR(100),
    record_date DATE,
    CONSTRAINT fk_after_sale_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_after_sale_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id),
    UNIQUE KEY uk_after_sale (customer_id, supplier_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ---------------- 种子数据（测试断言的依据，改这里就要同步改断言） ----------------
INSERT INTO supplier (name, phone, address) VALUES
  ('华为技术', '0755-10000', '广东深圳'),
  ('小米科技', '010-10086', '北京海淀'),
  ('戴尔电脑', '0755-20000', '广东厦门');

INSERT INTO product (name, price, spec, supplier_id) VALUES
  ('Mate70 手机',     5999.00, '12+512G', 1),  -- id=1
  ('MateBook 笔记本', 8999.00, '16+1T',   1),  -- id=2
  ('小米手环9',        249.00, '标准版',   2),  -- id=3
  ('AX6000 路由器',   399.00, 'WiFi6',   2),  -- id=4
  ('FreeBuds 耳机',   1299.00, '主动降噪', 1),  -- id=5，故意没有客户（验空关联）
  ('XPS 台式机',      6999.00, 'i7+32G',  3);  -- id=6，供应商 3 没有售后记录

INSERT INTO customer (name, phone, product_id) VALUES
  ('张三', '13800000001', 1),
  ('李四', '13800000002', 1),
  ('王五', '13800000003', 2),
  ('赵六', '13800000004', 3),
  ('孙七', '13800000005', 4);

INSERT INTO after_sale (customer_id, supplier_id, content, record_date) VALUES
  (1, 1, '屏幕保修', '2026-09-01'),      -- 张三 ↔ 华为
  (3, 1, '键盘换新', '2026-09-05'),      -- 王五 ↔ 华为
  (4, 2, '表带更换', '2026-09-10'),      -- 赵六 ↔ 小米
  (5, 2, '固件升级指导', '2026-09-15');  -- 孙七 ↔ 小米（戴尔 3 无售后）
