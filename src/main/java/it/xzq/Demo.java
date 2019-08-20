package it.xzq;

import java.util.HashMap;
import java.util.Map;


/**
 * 生成模板
 */
public class Demo {
//    private static Logger log = LoggerFactory.getLogger(GenCode.class);
    public static void main(String[] args) throws Exception {

        System.setProperty("app.id", "ngcos");
        System.setProperty("env", "local");
//        Config config = ConfigService.getConfig("database");
//        String url = config.getProperty("spring.datasource.url", "jdbc:mysql://118.31.49.116:3406/ngcos?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&noAccessToProcedureBodies=true&useSSL=true");
//        String username = config.getProperty("spring.datasource.username", "bytest_cs");
//        String password = config.getProperty("spring.datasource.password", "bytest_cs");    Config config = ConfigService.getConfig("database");
        //设置数据库要素
        String url =  "jdbc:mysql://118.31.49.116:3406/ngcos?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&noAccessToProcedureBodies=true&useSSL=true";
        String username = "spring.datasource.username";
        String password ="spring.datasource.password";

        Map<String,String> map=new HashMap<String,String>();
        //数据库地址
        map.put("URL", url.replace("CONVERT_TO_NULL", "convertToNull"));
        //数据库驱动
        map.put("DRIVER", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        //数据库登录名(
        map.put("NAME", username);
        //数据库登录密码
        map.put("PASS", password);
        //schema
        map.put("schema", "");
        //表名
        map.put("tablename", "case_call_records");
        //实体类生成路径
        map.put("entityPath", "com.boyacx.ngcos.dal.pojo");
        //dao生成路径
        map.put("mapperPath", "com.boyacx.ngcos.dal.dao");
        map.put("mapperXmlPath", "mybatis");

        Generator util=new Generator(map);
        util.genEntity();
        util.genMapper();
        System.out.println("生成成功!按F5刷新");
    }
}
