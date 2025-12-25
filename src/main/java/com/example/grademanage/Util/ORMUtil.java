package com.example.grademanage.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * ORM工具类（仅注解标记主键/自增，其他字段从orm.json读取）
 * 修正：1. 修复fieldtype拼写错误 2. 规范缓存键值 3. 增强异常处理 4. 兼容独立TableId注解 5. 提升代码健壮性
 */
public class ORMUtil {
    // 数据库连接配置
    private static final Map<String, String> dbConfig = new HashMap<>();
    // 表元数据缓存（key=实体类全限定名，避免简单类名冲突）
    public static Map<String, Map<String, Object>> tableMetaCache = new HashMap<>();
    // 主键缓存（key=实体类全限定名，value=主键信息：fieldName/isAutoIncrement）
    private static final Map<String, Map<String, Object>> primaryKeyCache = new HashMap<>();

    // 静态初始化：解析orm.json + 预加载表元数据
    static {
        try {
            // 1. 读取orm.json（兼容项目根目录和resources目录）
            String projectRoot = new File("").getAbsolutePath();
            File configFile = new File(projectRoot, "orm.json");

            // 兼容resources目录查找
            if (!configFile.exists()) {
                configFile = new File(projectRoot + "/src/main/resources", "orm.json");
                if (!configFile.exists()) {
                    throw new RuntimeException("未找到orm.json配置文件，已尝试路径：\n1. "
                            + new File(projectRoot, "orm.json").getAbsolutePath() + "\n2. "
                            + configFile.getAbsolutePath());
                }
            }

            // 2. 解析数据库连接配置
            JsonObject rootJson = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();
            // 校验核心配置项
            if (!rootJson.has("url") || !rootJson.has("username") || !rootJson.has("password")) {
                throw new RuntimeException("orm.json缺少必要的数据库配置：url/username/password");
            }
            dbConfig.put("url", rootJson.get("url").getAsString());
            dbConfig.put("username", rootJson.get("username").getAsString());
            dbConfig.put("password", rootJson.get("password").getAsString());

            // 3. 解析表元数据（从orm.json读取）
            if (!rootJson.has("tables")) {
                throw new RuntimeException("orm.json缺少tables配置节点");
            }
            JsonArray tablesJson = rootJson.getAsJsonArray("tables");
            for (int i = 0; i < tablesJson.size(); i++) {
                JsonObject tableJson = tablesJson.get(i).getAsJsonObject();
                // 校验表配置必要字段
                if (!tableJson.has("classname") || !tableJson.has("tablename")) {
                    throw new RuntimeException("第" + (i+1) + "个表配置缺少classname/tablename");
                }
                String className = tableJson.get("classname").getAsString(); // 全限定类名
                String tableName = tableJson.get("tablename").getAsString();

                // 存储表基本信息
                Map<String, Object> tableMeta = new HashMap<>();
                tableMeta.put("tableName", tableName);

                // 解析列配置（fieldname -> colname/coltype/fieldtype）
                Map<String, Map<String, String>> columnMeta = new HashMap<>();
                if (tableJson.has("columns")) {
                    JsonArray columnsJson = tableJson.getAsJsonArray("columns");
                    for (int j = 0; j < columnsJson.size(); j++) {
                        JsonObject colJson = columnsJson.get(j).getAsJsonObject();
                        // 校验列配置必要字段
                        if (!colJson.has("fieldname") || !colJson.has("colname") || !colJson.has("coltype")) {
                            throw new RuntimeException("第" + (i+1) + "个表的第" + (j+1) + "列配置缺少fieldname/colname/coltype");
                        }
                        String fieldName = colJson.get("fieldname").getAsString();

                        Map<String, String> colProps = new HashMap<>();
                        colProps.put("colName", colJson.get("colname").getAsString());
                        colProps.put("colType", colJson.get("coltype").getAsString());
                        // 修复：fieldtype拼写错误（原代码误写为filetype）
                        colProps.put("fieldType", colJson.has("fieldtype")
                                ? colJson.get("fieldtype").getAsString()
                                : "String"); // 默认值避免空指针

                        columnMeta.put(fieldName, colProps);
                    }
                }
                tableMeta.put("columns", columnMeta);
                // 使用全限定类名作为缓存key，避免不同包下同名类冲突
                tableMetaCache.put(className, tableMeta);
            }

            // 4. 加载MySQL驱动（兼容新旧版本）
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Class.forName("com.mysql.jdbc.Driver");
            }
            System.out.println("ORMUtil初始化成功，已加载表元数据：" + tableMetaCache.keySet());
        } catch (Exception e) {
            throw new RuntimeException("ORM初始化失败", e);
        }
    }

    // -------------------------- 核心工具方法 --------------------------
    /**
     * 格式化SQL值（处理字符串转义、多类型适配）
     */
    private static String formatSqlValue(Object value) {
        if (value == null) return "NULL";
        if (value instanceof String) return "'" + ((String) value).replace("'", "''") + "'";
        if (value instanceof Date) return "'" + new java.sql.Timestamp(((Date) value).getTime()) + "'";
        if (value instanceof BigDecimal || value instanceof Double || value instanceof Float) return value.toString();
        if (value instanceof Boolean) return ((Boolean) value) ? "1" : "0";
        if (value instanceof Number) return value.toString();
        // 兜底处理：转义单引号，避免SQL注入
        return "'" + value.toString().replace("'", "''") + "'";
    }

    /**
     * 解析实体类的主键/自增注解（仅解析一次，缓存结果）
     * 适配独立的TableId注解
     */
    private static Map<String, Object> parsePrimaryKeyAnnotation(Class<?> clazz) {
        // 使用全限定类名作为缓存key，避免冲突
        String className = clazz.getName();
        if (primaryKeyCache.containsKey(className)) {
            return primaryKeyCache.get(className);
        }

        Map<String, Object> pkInfo = new HashMap<>();
        String primaryKeyField = null;
        boolean isAutoIncrement = false;

        // 遍历字段，查找独立的@TableId注解
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null) {
                if (primaryKeyField != null) {
                    throw new RuntimeException("实体类[" + className + "]配置多个@TableId注解（仅支持单主键）");
                }
                primaryKeyField = field.getName();
                isAutoIncrement = tableId.isAutoIncrement();
            }
        }

        // 校验主键配置
        if (primaryKeyField == null) {
            throw new RuntimeException("实体类[" + className + "]未配置@TableId注解（主键字段必须标记）");
        }

        pkInfo.put("primaryKeyField", primaryKeyField);
        pkInfo.put("isAutoIncrement", isAutoIncrement);
        primaryKeyCache.put(className, pkInfo);
        return pkInfo;
    }

    // -------------------------- 核心API --------------------------
    /**
     * 获取数据库连接（增强稳定性）
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                dbConfig.get("url"),
                dbConfig.get("username"),
                dbConfig.get("password")
        );
        // 设置连接属性，提升稳定性
        conn.setAutoCommit(true);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return conn;
    }

    /**
     * 生成新增SQL（自动忽略自增主键）
     */
    @SuppressWarnings("unchecked")
    public static String createSaveSQL(Object entity) throws Exception {
        if (entity == null) throw new IllegalArgumentException("新增实体不能为null");

        Class<?> clazz = entity.getClass();
        String className = clazz.getName(); // 全限定类名
        // 优先使用全限定类名查找，降级到简单类名
        Map<String, Object> tableMeta = tableMetaCache.get(className);
        if (tableMeta == null) {
            tableMeta = tableMetaCache.get(clazz.getSimpleName());
            if (tableMeta == null) {
                throw new RuntimeException("未找到[" + className + "]的orm配置，请检查classname配置");
            }
        }

        Map<String, Object> pkInfo = parsePrimaryKeyAnnotation(clazz);
        String tableName = (String) tableMeta.get("tableName");
        Map<String, Map<String, String>> columns = (Map<String, Map<String, String>>) tableMeta.get("columns");
        String primaryKeyField = (String) pkInfo.get("primaryKeyField");
        boolean isAutoIncrement = (boolean) pkInfo.get("isAutoIncrement");

        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();

        for (Map.Entry<String, Map<String, String>> entry : columns.entrySet()) {
            String fieldName = entry.getKey();
            Map<String, String> colProps = entry.getValue();

            // 1. 忽略自增主键字段
            if (isAutoIncrement && fieldName.equals(primaryKeyField)) {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object fieldValue = field.get(entity);
                if (fieldValue != null) {
                    System.err.println("警告：[" + className + "]的自增主键[" + fieldName + "]无需设置值，已忽略");
                }
                continue;
            }

            // 2. 反射获取字段值
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            if (fieldValue == null) continue;

            // 3. 拼接列名和值
            if (!cols.isEmpty()) cols.append(",");
            cols.append(colProps.get("colName"));

            if (!vals.isEmpty()) vals.append(",");
            vals.append(formatSqlValue(fieldValue));
        }

        if (cols.isEmpty() || vals.isEmpty()) {
            throw new RuntimeException("新增操作必须设置至少一个非自增字段值");
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, cols, vals);
    }

    /**
     * 生成更新SQL（支持多字段修改，基于主键）
     */
    @SuppressWarnings("unchecked")
    public static String createUpdateSQL(Object entity) throws Exception {
        if (entity == null) throw new IllegalArgumentException("更新实体不能为null");

        Class<?> clazz = entity.getClass();
        String className = clazz.getName();
        Map<String, Object> tableMeta = tableMetaCache.get(className);
        if (tableMeta == null) {
            tableMeta = tableMetaCache.get(clazz.getSimpleName());
            if (tableMeta == null) {
                throw new RuntimeException("未找到[" + className + "]的orm配置，请检查classname配置");
            }
        }

        Map<String, Object> pkInfo = parsePrimaryKeyAnnotation(clazz);
        String tableName = (String) tableMeta.get("tableName");
        Map<String, Map<String, String>> columns = (Map<String, Map<String, String>>) tableMeta.get("columns");
        String primaryKeyField = (String) pkInfo.get("primaryKeyField");

        // 校验主键列是否在orm.json中配置
        if (!columns.containsKey(primaryKeyField)) {
            throw new RuntimeException("实体类[" + className + "]的主键字段[" + primaryKeyField + "]未在orm.json中配置");
        }
        String primaryKeyCol = columns.get(primaryKeyField).get("colName");

        // 提前获取并校验主键值（减少无效循环）
        Field pkField = clazz.getDeclaredField(primaryKeyField);
        pkField.setAccessible(true);
        Object primaryKeyValue = pkField.get(entity);
        if (primaryKeyValue == null) {
            throw new RuntimeException("更新操作必须设置主键[" + primaryKeyField + "]");
        }

        StringBuilder setClause = new StringBuilder();

        for (Map.Entry<String, Map<String, String>> entry : columns.entrySet()) {
            String fieldName = entry.getKey();
            Map<String, String> colProps = entry.getValue();

            // 跳过主键字段
            if (fieldName.equals(primaryKeyField)) continue;

            // 反射获取字段值
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldValue = field.get(entity);

            // 跳过空值
            if (fieldValue == null) continue;

            // 拼接SET子句
            if (!setClause.isEmpty()) setClause.append(",");
            setClause.append(colProps.get("colName")).append("=").append(formatSqlValue(fieldValue));
        }

        if (setClause.isEmpty()) {
            throw new RuntimeException("无可用更新字段，请设置非主键字段值");
        }

        return String.format("UPDATE %s SET %s WHERE %s=%s",
                tableName, setClause, primaryKeyCol, formatSqlValue(primaryKeyValue));
    }

    /**
     * 生成单条查询SQL（根据主键）
     */
    @SuppressWarnings("unchecked")
    public static String createSelectSQL(Class<?> clazz, Object id) {
        if (clazz == null || id == null) {
            throw new IllegalArgumentException("实体类/主键不能为null");
        }

        String className = clazz.getName();
        Map<String, Object> tableMeta = tableMetaCache.get(className);
        if (tableMeta == null) {
            tableMeta = tableMetaCache.get(clazz.getSimpleName());
            if (tableMeta == null) {
                throw new RuntimeException("未找到[" + className + "]的orm配置，请检查classname配置");
            }
        }

        Map<String, Object> pkInfo = parsePrimaryKeyAnnotation(clazz);
        String tableName = (String) tableMeta.get("tableName");
        Map<String, Map<String, String>> columns = (Map<String, Map<String, String>>) tableMeta.get("columns");
        String primaryKeyField = (String) pkInfo.get("primaryKeyField");

        if (!columns.containsKey(primaryKeyField)) {
            throw new RuntimeException("实体类[" + className + "]的主键字段[" + primaryKeyField + "]未在orm.json中配置");
        }
        String primaryKeyCol = columns.get(primaryKeyField).get("colName");

        return String.format("SELECT * FROM %s WHERE %s=%s", tableName, primaryKeyCol, formatSqlValue(id));
    }

    /**
     * 生成批量查询SQL（多个主键）
     */
    @SuppressWarnings("unchecked")
    public static String createSelectBatchSQL(Class<?> clazz, Collection<?> ids) {
        if (clazz == null || ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("实体类/主键列表不能为null/空");
        }

        String className = clazz.getName();
        Map<String, Object> tableMeta = tableMetaCache.get(className);
        if (tableMeta == null) {
            tableMeta = tableMetaCache.get(clazz.getSimpleName());
            if (tableMeta == null) {
                throw new RuntimeException("未找到[" + className + "]的orm配置，请检查classname配置");
            }
        }

        Map<String, Object> pkInfo = parsePrimaryKeyAnnotation(clazz);
        String tableName = (String) tableMeta.get("tableName");
        Map<String, Map<String, String>> columns = (Map<String, Map<String, String>>) tableMeta.get("columns");
        String primaryKeyField = (String) pkInfo.get("primaryKeyField");

        if (!columns.containsKey(primaryKeyField)) {
            throw new RuntimeException("实体类[" + className + "]的主键字段[" + primaryKeyField + "]未在orm.json中配置");
        }
        String primaryKeyCol = columns.get(primaryKeyField).get("colName");

        StringBuilder idClause = new StringBuilder();
        for (Object id : ids) {
            if (id == null) continue; // 跳过空主键，避免无效SQL
            if (!idClause.isEmpty()) idClause.append(",");
            idClause.append(formatSqlValue(id));
        }

        if (idClause.isEmpty()) {
            throw new IllegalArgumentException("主键列表中无有效主键值");
        }

        return String.format("SELECT * FROM %s WHERE %s IN (%s)", tableName, primaryKeyCol, idClause);
    }

    /**
     * 生成全量查询SQL
     */
    public static String createSelectAllSQL(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("实体类不能为null");
        }

        String className = clazz.getName();
        Map<String, Object> tableMeta = tableMetaCache.get(className);
        if (tableMeta == null) {
            tableMeta = tableMetaCache.get(clazz.getSimpleName());
            if (tableMeta == null) {
                throw new RuntimeException("未找到[" + className + "]的orm配置，请检查classname配置");
            }
        }

        String tableName = (String) tableMeta.get("tableName");
        return String.format("SELECT * FROM %s", tableName);
    }

    /**
     * 生成删除SQL（根据主键）
     */
    @SuppressWarnings("unchecked")
    public static String createDeleteSQL(Class<?> clazz, Object id) {
        if (clazz == null || id == null) {
            throw new IllegalArgumentException("实体类/主键不能为null");
        }

        String className = clazz.getName();
        Map<String, Object> tableMeta = tableMetaCache.get(className);
        if (tableMeta == null) {
            tableMeta = tableMetaCache.get(clazz.getSimpleName());
            if (tableMeta == null) {
                throw new RuntimeException("未找到[" + className + "]的orm配置，请检查classname配置");
            }
        }

        Map<String, Object> pkInfo = parsePrimaryKeyAnnotation(clazz);
        String tableName = (String) tableMeta.get("tableName");
        Map<String, Map<String, String>> columns = (Map<String, Map<String, String>>) tableMeta.get("columns");
        String primaryKeyField = (String) pkInfo.get("primaryKeyField");

        if (!columns.containsKey(primaryKeyField)) {
            throw new RuntimeException("实体类[" + className + "]的主键字段[" + primaryKeyField + "]未在orm.json中配置");
        }
        String primaryKeyCol = columns.get(primaryKeyField).get("colName");

        return String.format("DELETE FROM %s WHERE %s=%s", tableName, primaryKeyCol, formatSqlValue(id));
    }
}