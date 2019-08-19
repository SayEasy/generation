package it.xzq;


import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.sql.*;
import java.util.Map;
import java.util.Properties;


public class Generator {

       private String tablename;
    private String schema;
    private String entityPath;
    private String mapperPath;
    private String mapperXmlPath;
    private String URL;
    private String NAME;
    private String PASS;
    private String DRIVER;
    private String pks = "";
    private String[] colnames;
    private String[] colTypes;
    private int[] colSizes;
    private int[] colNullables;
    private String[] remarks;
    private String entityName;
    private GenUtil genUtil = new GenUtil();

    public Generator(Map<String, String> map) {

        this.DRIVER = ((String) map.get("DRIVER"));
        if (StringUtils.isEmpty(this.DRIVER)) {
            throw new NullPointerException("DRIVER is null");
        }
        this.URL = ((String) map.get("URL"));
        if (StringUtils.isEmpty(this.URL)) {
            throw new NullPointerException("URL is null");
        }
        this.NAME = ((String) map.get("NAME"));
        if (StringUtils.isEmpty(this.NAME)) {
            throw new NullPointerException("NAME is null");
        }
        this.PASS = ((String) map.get("PASS"));
        if (StringUtils.isEmpty(this.PASS)) {
            throw new NullPointerException("PASS is null");
        }
        this.schema = ((String) map.get("schema"));
        this.tablename = ((String) map.get("tablename"));
        if (StringUtils.isEmpty(this.tablename)) {
            throw new NullPointerException("tablename is null");
        }
        this.entityPath = ((String) map.get("entityPath"));
        this.mapperPath = ((String) map.get("mapperPath"));
        this.mapperXmlPath = ((String) map.get("mapperXmlPath"));

        Connection con = null;
        this.tablename = this.tablename.toUpperCase();
        String sql = new StringBuilder().append("SELECT * FROM ").append(this.tablename).toString();
        Statement pStemt = null;
        try {
            try {
                Class.forName(this.DRIVER);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            Properties props = new Properties();
            props.put("user", this.NAME);
            props.put("password", this.PASS);
            props.put("remarksReporting", "true");
            con = DriverManager.getConnection(this.URL, props);

            pStemt = con.createStatement();
            ResultSet rs = pStemt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            ResultSet r = con.getMetaData().getPrimaryKeys(null, this.schema, this.tablename);
            while (r.next()) {
                this.pks = new StringBuilder().append(this.pks).append(r.getString("COLUMN_NAME")).append(",").toString();
            }
            if (this.pks == "") {
                System.out.println(new StringBuilder().append("WARM:").append(this.tablename).append(" has no primary key!").toString());
            }
            int size = rsmd.getColumnCount();
            this.colnames = new String[size];
            this.colTypes = new String[size];
            this.colSizes = new int[size];
            this.colNullables = new int[size];
            for (int i = 0; i < size; i++) {
                this.colnames[i] = rsmd.getColumnName(i + 1);
                this.colTypes[i] = rsmd.getColumnTypeName(i + 1);
                this.colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
                this.colNullables[i] = rsmd.isNullable(i + 1);
            }
            this.entityName = this.genUtil.underlineToHump(this.tablename);
            ResultSet rr = null;
            if (this.DRIVER.contains("oracle")) {
                if ((this.schema == null) || ("".equals(this.schema.trim()))) {
                    System.out.println("WARM:Please fill in schema, annotations can not be generated!");
                }
                rr = con.getMetaData().getColumns(null, this.schema, this.tablename, "%");
            } else if (this.DRIVER.contains("mysql")) {
                rr = con.getMetaData().getColumns(null, "%", this.tablename, "%");
            }
            this.remarks = new String[size];
            int i = 0;
            while (rr.next()) {
                this.remarks[i] = rr.getString("REMARKS");
                i++;
            }
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getEntityContent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(new StringBuilder().append("package ").append(this.entityPath).append(";\r\n").toString());
        sb.append("\r\n");
        sb.append("import java.util.Date;\r\n");
        sb.append("import java.math.BigDecimal;\r\n");
        sb.append("import javax.persistence.Entity;\r\n");
        sb.append("import lombok.AllArgsConstructor;\r\n");
        sb.append("import lombok.Data;\r\n");
        sb.append("import lombok.NoArgsConstructor;\r\n");
        sb.append("\r\n");
        sb.append("@Entity\r\n");
        sb.append("@Data\r\n");
        sb.append("@NoArgsConstructor\r\n");
        sb.append("@AllArgsConstructor\r\n");
        sb.append("/** ");
        sb.append(this.tablename);
        sb.append(" */\r\n");
        sb.append(new StringBuilder().append("public class ").append(this.entityName).append("Pojo implements java.io.Serializable{\r\n").toString());
        sb.append("\tprivate static final long serialVersionUID = 1L;\r\n");
        for (int i = 0; i < this.colnames.length; i++)
        {
            sb.append(new StringBuilder().append("\t/** ").append(this.remarks[i]).append(" */\r\n").toString());
            sb.append(new StringBuilder().append("\tprivate ").append(sqlType2JavaType(this.colTypes[i])).append(" ").append(this.genUtil.underlineToHump(this.colnames[i], false)).append(";\r\n").toString());
        }
        sb.append("}\r\n");
        return sb.toString();
    }

    private String getMapperContent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(new StringBuilder().append("package ").append(this.mapperPath).append(";\r\n").toString());
        sb.append(new StringBuilder().append("import ").append(this.entityPath).append(".").append(this.entityName).append("Pojo;\r\n").toString());
        sb.append("import java.util.List;\r\n");
        sb.append("import java.util.Map;\r\n");
        sb.append("\r\n");
        sb.append(new StringBuilder().append("public interface ").append(this.entityName).append("Mapper{\r\n").toString());
        sb.append(new StringBuilder().append("\tpublic ").append(this.entityName).append("Pojo getByPK(String pk);\r\n").toString());
        sb.append(new StringBuilder().append("\tpublic ").append(this.entityName).append("Pojo get(Map<String,Object> params);\r\n").toString());
        sb.append(new StringBuilder().append("\tpublic List<").append(this.entityName).append("Pojo> getList(Map<String,Object> params);\r\n").toString());
        sb.append(new StringBuilder().append("\tpublic void insert(").append(this.entityName).append("Pojo obj);\r\n").toString());
        sb.append(new StringBuilder().append("\tpublic void updateByPK(").append(this.entityName).append("Pojo obj);\r\n").toString());
        sb.append("\tpublic void deleteByPK(String pk);\r\n");
        sb.append("}\r\n");
        return sb.toString();
    }

    private String getMapperXmlContent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n");
        sb.append("\r\n");
        sb.append(new StringBuilder().append("<mapper namespace=\"").append(this.mapperPath).append(".").append(this.entityName).append("Mapper\">\r\n").toString());
        sb.append("\t<sql id=\"column\">\r\n");
        for (int i = 0; i < this.colnames.length; i++) {
            sb.append(new StringBuilder().append("\t\tt.").append(this.colnames[i]).append(" ").append(this.genUtil.underlineToHump(this.colnames[i], false)).append(",\r\n").toString());
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("\t</sql>\r\n");
        sb.append(new StringBuilder().append("\t<select id=\"get\" parameterType=\"java.util.Map\" resultType=\"").append(this.entityPath).append(".").append(this.entityName).append("Pojo\">\r\n").toString());
        sb.append("\t\tSELECT\r\n");
        sb.append("\t\t\t<include refid=\"column\"/>\r\n");
        if ((this.schema == null) || ("".equals(this.schema.trim())))
            sb.append(new StringBuilder().append("\t\tFROM ").append(this.tablename).append(" t\r\n").toString());
        else {
            sb.append(new StringBuilder().append("\t\tFROM ").append(this.schema).append(".").append(this.tablename).append(" t\r\n").toString());
        }
        sb.append("\t\t<trim prefix=\"WHERE\" prefixOverrides=\"AND |OR \">\r\n");
        for (int i = 0; i < this.colnames.length; i++)
        {
            String attrNameFirstLower = this.genUtil.underlineToHump(this.colnames[i], false);
            sb.append(new StringBuilder().append("\t\t\t<if test=\"").append(attrNameFirstLower).append(" != null and ").append(attrNameFirstLower).append(" != ''\">\r\n").toString());
            sb.append(new StringBuilder().append("\t\t\t\tAND t.").append(this.colnames[i]).append(" = #{").append(attrNameFirstLower).append("}\r\n").toString());
            sb.append("\t\t\t</if>\r\n");
        }
        sb.append("\t\t</trim>\r\n");
        sb.append("\t</select>\r\n");
        sb.append(new StringBuilder().append("\t<select id=\"getList\" parameterType=\"java.util.Map\" resultType=\"").append(this.entityPath).append(".").append(this.entityName).append("Pojo\">\r\n").toString());
        sb.append("\t\tSELECT\r\n");
        sb.append("\t\t\t<include refid=\"column\"/>\r\n");
        if ((this.schema == null) || ("".equals(this.schema.trim())))
            sb.append(new StringBuilder().append("\t\tFROM ").append(this.tablename).append(" t\r\n").toString());
        else {
            sb.append(new StringBuilder().append("\t\tFROM ").append(this.schema).append(".").append(this.tablename).append(" t\r\n").toString());
        }
        sb.append("\t\t<trim prefix=\"WHERE\" prefixOverrides=\"AND |OR \">\r\n");
        for (int i = 0; i < this.colnames.length; i++)
        {
            String attrNameFirstLower = this.genUtil.underlineToHump(this.colnames[i], false);
            sb.append(new StringBuilder().append("\t\t\t<if test=\"").append(attrNameFirstLower).append(" != null and ").append(attrNameFirstLower).append(" != ''\">\r\n").toString());
            sb.append(new StringBuilder().append("\t\t\t\tAND t.").append(this.colnames[i]).append(" = #{").append(attrNameFirstLower).append("}\r\n").toString());
            sb.append("\t\t\t</if>\r\n");
        }
        sb.append("\t\t</trim>\r\n");
        sb.append("\t</select>\r\n");
        sb.append(new StringBuilder().append("\t<insert id=\"insert\" parameterType=\"").append(this.entityPath).append(".").append(this.entityName).append("Pojo\">\r\n").toString());
        sb.append(new StringBuilder().append("\tINSERT INTO ").append(this.tablename).append("\r\n").toString());
        sb.append("\t(\r\n");
        for (int i = 0; i < this.colnames.length; i++) {
            sb.append(new StringBuilder().append("\t\t").append(this.colnames[i]).append(",\r\n").toString());
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("\t)\r\n");
        sb.append("\tvalues\r\n");
        sb.append("\t(\r\n");
        for (int i = 0; i < this.colnames.length; i++) {
            sb.append(new StringBuilder().append("\t\t#{").append(this.genUtil.underlineToHump(this.colnames[i], false)).append("},\r\n").toString());
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("\t)\r\n");
        sb.append("\t</insert>\r\n");
        sb.append(new StringBuilder().append("\t<update id=\"updateByPK\" parameterType=\"").append(this.entityPath).append(".").append(this.entityName).append("Pojo\">\r\n").toString());
        sb.append(new StringBuilder().append("\tUPDATE ").append(this.tablename).append("\r\n").toString());
        sb.append("\t\t<trim prefix=\"SET\" suffixOverrides=\",\">\r\n");
        for (int i = 0; i < this.colnames.length; i++) {
            if ("ID".equals(this.colnames[i]))
                continue;
            String attrNameFirstLower = this.genUtil.underlineToHump(this.colnames[i], false);
            sb.append(new StringBuilder().append("\t\t\t<if test=\"").append(attrNameFirstLower).append(" != null\">\r\n").toString());
            sb.append(new StringBuilder().append("\t\t\t\t").append(this.colnames[i]).append(" = #{").append(attrNameFirstLower).append("},\r\n").toString());
            sb.append("\t\t\t</if>\r\n");
        }

        sb.append("\t\t</trim>\r\n");
        String[] pksArr = this.pks.split(",");
        for (int i = 0; i < pksArr.length; i++)
        {
            if (i == 0)
                sb.append("\t\tWHERE ");
            else {
                sb.append(" AND ");
            }
            sb.append(new StringBuilder().append(pksArr[i]).append("=#{").append(this.genUtil.underlineToHump(pksArr[i], false)).append("}").toString());
        }
        sb.append("\r\n");
        sb.append("\t</update>\r\n");
        sb.append("\t<delete id=\"deleteByPK\" parameterType=\"java.lang.String\">\r\n");
        sb.append(new StringBuilder().append("\t\tDELETE FROM ").append(this.tablename).toString());
        for (int i = 0; i < pksArr.length; i++)
        {
            if (i == 0)
                sb.append(" WHERE ");
            else {
                sb.append(" AND ");
            }
            sb.append(new StringBuilder().append(pksArr[i]).append("=#{").append(this.genUtil.underlineToHump(pksArr[i], false)).append("}").toString());
        }
        sb.append("\r\n");
        sb.append("\t</delete>\r\n");
        sb.append(new StringBuilder().append("\t<select id=\"getByPK\" parameterType=\"java.lang.String\" resultType=\"").append(this.entityPath).append(".").append(this.entityName).append("Pojo\">\r\n").toString());
        sb.append("\t\tSELECT\r\n");
        sb.append("\t\t\t<include refid=\"column\"/>\r\n");
        if ((this.schema == null) || ("".equals(this.schema.trim())))
            sb.append(new StringBuilder().append("\t\tFROM ").append(this.tablename).append(" t\r\n").toString());
        else {
            sb.append(new StringBuilder().append("\t\tFROM ").append(this.schema).append(".").append(this.tablename).append(" t\r\n").toString());
        }
        for (int i = 0; i < pksArr.length; i++)
        {
            if (i == 0)
                sb.append("\t\tWHERE ");
            else {
                sb.append(" AND ");
            }
            sb.append(new StringBuilder().append(pksArr[i]).append("=#{").append(this.genUtil.underlineToHump(pksArr[i], false)).append("}").toString());
        }
        sb.append("\r\n");
        sb.append("\t</select>\r\n");
        sb.append("</mapper>\r\n");
        sb.append("\r\n");
        return sb.toString();
    }

    private String sqlType2JavaType(String sqlType)
    {
        if ((sqlType.equalsIgnoreCase("binary_double")) || (sqlType.equalsIgnoreCase("double"))) {
            return "Double";
        }
        if ((sqlType.equalsIgnoreCase("binary_float")) || (sqlType.equalsIgnoreCase("float"))) {
            return "float";
        }
        if (sqlType.equalsIgnoreCase("blob")) {
            return "byte[]";
        }
        if ((sqlType.equalsIgnoreCase("int")) || (sqlType.equalsIgnoreCase("integer")) || (sqlType.equalsIgnoreCase("smallint"))) {
            return "Integer";
        }
        if ((sqlType.equalsIgnoreCase("char")) || (sqlType.equalsIgnoreCase("nvarchar2")) || (sqlType.equalsIgnoreCase("varchar2")) || (sqlType.equalsIgnoreCase("varchar"))) {
            return "String";
        }
        if ((sqlType.equalsIgnoreCase("date")) || (sqlType.equalsIgnoreCase("datetime")) || (sqlType.equalsIgnoreCase("timestamp")) || (sqlType.equalsIgnoreCase("timestamp with local time zone")) || (sqlType.equalsIgnoreCase("timestamp with time zone"))) {
            return "Date";
        }
        if (sqlType.equalsIgnoreCase("number")) {
            return "Double";
        }
        if (sqlType.equalsIgnoreCase("bigint")) {
            return "Long";
        }
        if (sqlType.equalsIgnoreCase("decimal")) {
            return "BigDecimal";
        }
        return "未添加映射";
    }

    public void genEntity() throws Exception
    {
        if (StringUtils.isEmpty(this.entityPath)) {
            throw new NullPointerException("entityPath is null");
        }
        this.genUtil.OutFile(getEntityContent(), new StringBuilder().append(this.entityName).append("Pojo.java").toString(), this.entityPath);
    }

    public void genMapper() throws Exception
    {
        if (StringUtils.isEmpty(this.mapperPath)) {
            throw new NullPointerException("mapperPath is null");
        }
        this.genUtil.OutFile(getMapperContent(), new StringBuilder().append(this.entityName).append("Mapper.java").toString(), this.mapperPath);
        this.genUtil.OutFile(getMapperXmlContent(), new StringBuilder().append(this.entityName).append("Mapper.xml").toString(), this.mapperXmlPath);
    }
}
