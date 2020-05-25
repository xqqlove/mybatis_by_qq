package utils;

import java.sql.*;
import java.util.List;

public final class JDBCUtils {
   private static String connect;
   private static String driverClassName;
   private static String URL;
   private static String username;
   private static String password;
   private static boolean autoCommit;
   /**声明一个Connection类型的静态属性，用来缓存一个已经存在的连接对象**/
   private static Connection conn;

   static {
       config();
   }
    /**
     * 配置自己的数据库信息
     */
   private static void config(){
       /**
        * 获取驱动
        */
       driverClassName="com.mysql.jdbc.Driver";
       URL="jdbc:mysql://localhost:3306/mybatis?useUnicode=true&characterEncoding=utf8";
       username="root";
       password="21040408";
       autoCommit=false;
   }

    /**
     * 载入数据库驱动类
     * @return
     */
   private static boolean load(){
       try {
           Class.forName(driverClassName);
           return true;
       } catch (ClassNotFoundException e) {
           System.out.println("驱动类 "+driverClassName+"加载失败");
       }
       return false;
   }
   private static boolean invalid(){
       if (conn!=null){
           try {
               if (conn.isClosed()||!conn.isValid(3)){
                   return true;
                   /**
                    * isValid方法判断Connection是否有效，如果连接尚未关闭并且任然有效，则返回true
                    */
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
           return false;

//		 * conn 既不是 null 且也没有关闭 ，且 isValid 返回 true，说明是可以使用的 (返回false)
       }else {
          return true;
       }
   }

    /**
     * 建立数据库链接
     */
    public static Connection connect(){
        if (invalid()){/*invalid为true时，说明连接失败*/
            /*加载驱动*/
            load();
            try {
                conn= DriverManager.getConnection(URL,username,password);
            } catch (SQLException e) {
                System.out.println("建立 "+connect+" 数据库链接失败，"+e.getMessage());
            }
        }
        return conn;
    }

    /**
     * 设置是否自动提交事务
     */
    public static void transaction(){
        try {
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            System.out.println("设置事务的提交方式为："+(autoCommit ? "自动提交":"手动提交"));
        }
    }

    /**
     * 创建statement对象
     */
    public static Statement statement(){
        Statement st=null;
        connect();
        transaction();
        try {
            st=conn.createStatement();
        } catch (SQLException e) {
            System.out.println("创建statement对象失败："+e.getMessage());
        }
        return st;
    }
    /**
     * 根据给定带参数的占位符的sql创建preparedStatement对象
     */
    private static PreparedStatement prepare(String SQL,boolean autoGeneratedKeys){
        PreparedStatement ps=null;
        connect();
        transaction();
        try {
            if (autoGeneratedKeys){
                ps=conn.prepareStatement(SQL,Statement.RETURN_GENERATED_KEYS);
            }else {
                ps=conn.prepareStatement(SQL);
            }
        } catch (SQLException e) {
            System.out.println("创建PrepareStatement对象失败："+e.getMessage());
        }
        return ps;
    }

    public static ResultSet query(String SQL, List<Object> params){
        if (SQL==null||SQL.trim().isEmpty()||!SQL.trim().toLowerCase().startsWith("select")){
            throw new RuntimeException("您的sql语句为空，或者不是查询语句");
        }
        ResultSet rs=null;
        if (params.size()>0){
            PreparedStatement ps=prepare(SQL,false);
            try {
                for (int i=0;i<params.size();i++){
                    ps.setObject(i+1,params.get(i));
                }
                rs=ps.executeQuery();
            } catch (SQLException e) {
                System.out.println("执行SQL失败："+e.getMessage());
            }
        }else {
            Statement st=statement();
            try {
                rs=st.executeQuery(SQL);
            } catch (SQLException e) {
                System.out.println("执行sql失败："+e.getMessage());
            }
        }
        return rs;
    }


}
