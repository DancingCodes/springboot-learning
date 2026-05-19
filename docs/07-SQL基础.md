# SQL 基础

## 什么是 SQL

SQL 是跟数据库对话的语言。你告诉数据库"要干什么"，它返回结果。

## 增删改查（CRUD）

数据库操作只有四种：

| 操作 | 关键字 | 作用 |
|------|--------|------|
| 增 | `INSERT` | 新增一行数据 |
| 删 | `DELETE` | 删除符合条件的数据 |
| 改 | `UPDATE` | 修改符合条件的数据 |
| 查 | `SELECT` | 查询符合条件的数据 |

## 基本语法

### SELECT（查）

```sql
-- 查 account 表里所有行的所有列
SELECT * FROM account;

-- 只查 id 和 balance 两列
SELECT id, balance FROM account;

-- 查 id=1 的那一行
SELECT * FROM account WHERE id = 1;
```

### INSERT（增）

```sql
-- 插入一行，balance 初始为 1000.00
INSERT INTO account (id, balance) VALUES (1, 1000.00);
```

> MyBatis-Plus 的 `insert(entity)` 帮你拼好了这条 SQL，不需要手写。

### UPDATE（改）

```sql
-- 把 id=1 的 balance 改为 999.00
UPDATE account SET balance = 999.00 WHERE id = 1;
```

**`WHERE` 很重要！** 不加 WHERE 会把整张表的所有行都改了：

```sql
UPDATE account SET balance = 999.00;  -- 所有行都变成 999，灾难！
```

### DELETE（删）

```sql
-- 删除 id=1 的那行
DELETE FROM account WHERE id = 1;
```

同样，不加 `WHERE` 会清空整张表。

## 怎么理解 UPDATE 语句

把一条 SQL 当成一句话来读：

```sql
UPDATE  account  SET  balance = balance - 100  WHERE  id = 1  AND  balance >= 100;
  |       |       |          |                   |      |     |         |
修改    这张表    把...      当前余额减100        只改   id列  匹配1   而且  余额要够100
```

逐段翻译：

| 部分 | 作用 | 翻译 |
|------|------|------|
| `UPDATE account` | 指定要改哪张表 | "我要改 account 表" |
| `SET balance = balance - 100` | 指定怎么改 | "把 balance 的值改成当前值减 100" |
| `WHERE id = 1` | 指定改哪行 | "只改 id 等于 1 的那一行" |
| `AND balance >= 100` | 附加条件 | "而且余额必须大于等于 100" |

### 为什么用 `balance = balance - 100` 而不是直接写死

```sql
-- ✅ 在现有余额基础上减
balance = balance - 100     -- 余额 500 → 400

-- ❌ 直接覆盖成一个固定值，丢失了原来的数
balance = 400               -- 前提是你得知道原来是 500，还要自己算减法
```

`balance = balance + 数值` 是"在当前值上加减"，`balance = 具体数值` 是"不管原来多少，直接覆盖"。转账需要在当前值上加减，所以用前一种写法。

## WHERE 条件

### 比较运算符

| 写法 | 含义 |
|------|------|
| `=` | 等于 |
| `>` / `<` | 大于 / 小于 |
| `>=` / `<=` | 大于等于 / 小于等于 |
| `<>` 或 `!=` | 不等于 |

### `AND` vs `OR`

```sql
-- AND：两个条件都要满足
WHERE balance >= 100 AND id = 1

-- OR：满足任意一个即可
WHERE id = 1 OR id = 2
```

## MyBatis 里的 `#{}` 占位符

```java
@Update("UPDATE account SET balance = balance - #{amount} WHERE id = #{id}")
int deduct(@Param("id") Long id, @Param("amount") BigDecimal amount);
```

`#{amount}` 不是 SQL 语法，是 MyBatis 的**占位符**。执行时 MyBatis 会把 `#{amount}` 替换成你传的参数值：

```java
// 调用 deduct(1L, new BigDecimal("100"));
// MyBatis 替换后实际执行的 SQL：
// UPDATE account SET balance = balance - ? WHERE id = ?
// 参数填入：? → 100, ? → 1
```

用的是 `?` 参数化查询，不是字符串拼接，所以**不会被 SQL 注入攻击**。

## 实战：项目里的两条 SQL

```sql
-- 扣钱：余额够才让扣
UPDATE account SET balance = balance - #{amount}
WHERE id = #{id} AND balance >= #{amount}

-- 加钱：没有余额限制
UPDATE account SET balance = balance + #{amount}
WHERE id = #{id}
```

扣钱多了一个 `AND balance >= #{amount}`，超扣时 WHERE 条件不满足 → 影响 0 行 → 代码里 `if (rows == 0)` 抛异常 → 事务回滚。
