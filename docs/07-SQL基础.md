# SQL 基础

## 增删改查（CRUD）

| 操作 | 关键字 | 作用 |
|------|--------|------|
| 增 | `INSERT` | 新增一行 |
| 删 | `DELETE` | 删除符合条件的数据 |
| 改 | `UPDATE` | 修改符合条件的数据 |
| 查 | `SELECT` | 查询符合条件的数据 |

## 基本语法

### SELECT（查）

```sql
-- 查所有行所有列
SELECT * FROM account;

-- 只查 id 和 balance 两列
SELECT id, balance FROM account;

-- 查 id=1 的那一行
SELECT * FROM account WHERE id = 1;
```

### INSERT（增）

```sql
INSERT INTO account (id, balance) VALUES (1, 1000.00);
```

> MyBatis-Plus 的 `insert(entity)` 帮你拼好了这条 SQL，不需要手写。

### UPDATE（改）

```sql
-- 把 id=1 的 balance 改为 999
UPDATE account SET balance = 999 WHERE id = 1;
```

**`WHERE` 很重要！** 不加会把整张表都改了：

```sql
UPDATE account SET balance = 999;  -- 所有行都变成 999，灾难！
```

### DELETE（删）

```sql
DELETE FROM account WHERE id = 1;
```

同样，不加 `WHERE` 会清空整张表。

## UPDATE 逐段理解

```sql
UPDATE account SET balance = balance - 100 WHERE id = 1 AND balance >= 100;
```

| 部分 | 作用 |
|------|------|
| `UPDATE account` | 改 account 表 |
| `SET balance = balance - 100` | 在当前值上减 100 |
| `WHERE id = 1` | 只改 id=1 的行 |
| `AND balance >= 100` | 余额必须 >=100 |

`balance = balance - 100` 是在当前值上加减，`balance = 400` 是直接覆盖。转账需要加减，不能用固定值。

## WHERE 条件

| 写法 | 含义 |
|------|------|
| `=` | 等于 |
| `>` / `<` | 大于 / 小于 |
| `>=` / `<=` | 大于等于 / 小于等于 |
| `<>` 或 `!=` | 不等于 |

```sql
-- AND：两个条件都要满足
WHERE balance >= 100 AND id = 1

-- OR：满足任意一个
WHERE id = 1 OR id = 2
```

## MyBatis 的 `#{}` 占位符

```java
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;

@Update("UPDATE account SET balance = balance - #{amount} WHERE id = #{id}")
int deduct(@Param("id") Long id, @Param("amount") BigDecimal amount);
```

`#{amount}` 是 MyBatis 占位符，执行时替换为 `?` 参数化查询，**不是字符串拼接**，不会被 SQL 注入。

```java
// 调用 deduct(1L, new BigDecimal("100"));
// 实际执行：UPDATE account SET balance = balance - ? WHERE id = ?
// ? → 100, ? → 1
```

## 实战：扣钱 + 加钱

```sql
-- 扣钱：余额够才让扣
UPDATE account SET balance = balance - #{amount}
WHERE id = #{id} AND balance >= #{amount}

-- 加钱：没有余额限制
UPDATE account SET balance = balance + #{amount}
WHERE id = #{id}
```

扣钱多了 `AND balance >= #{amount}`，超扣时 WHERE 不满足 → 影响 0 行 → 代码里判 `rows == 0` 抛异常 → 事务回滚。
