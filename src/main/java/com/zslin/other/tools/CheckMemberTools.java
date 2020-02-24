package com.zslin.other.tools;

import com.zslin.other.dto.JDBCObj;

import java.sql.*;

/**
 * Created by zsl on 2019/4/1.
 * 校验会员资金工具类
 */
public class CheckMemberTools {

    public void run() throws Exception {
        JDBCObj local = new JDBCObj("localhost:3306", "hlx_client", "root", "123");
        JDBCObj server = new JDBCObj("localhost:3306", "hlx", "root", "123");
//        JDBCObj server = new JDBCObj("123.58.6.13:13396", "hlx", "root", "ynzslzsl**");
        Connection serverCon = getConn(server);

        Connection localCon = getConn(local);
        Statement localState = localCon.createStatement();
        String sql = "select * FROM t_member";
        ResultSet localRs = localState.executeQuery(sql);
        int count = 0;
        while(localRs.next()) {
            String phone = localRs.getString("phone");
            Integer money = localRs.getInt("surplus");
            //System.out.println("姓名："+localRs.getString("name")+":"+phone+"-->"+money);
            count += check(serverCon, phone, money);
        }
        System.out.println("数据异常数量：【"+count+"】");
    }

    private int check(Connection con, String phone, Integer money) throws Exception {
//        JDBCObj server = new JDBCObj("localhost:3306", "hlx", "root", "123");
//        Connection con = getConn(server);
        Statement sm = con.createStatement();
        String sql = "select * FROM t_wallet WHERE phone='"+phone+"'";
        ResultSet rs = sm.executeQuery(sql);
        int count = 0;
        while(rs.next()) {
            int m = rs.getInt("money");
            if(m!=money) {
                count ++;
                System.out.println(phone + "--> 本地：" + money + "--> 服务器：" + rs.getInt("money"));
            }
        }
        return count;
    }

    private Connection getConn(JDBCObj obj) {
        try {
            String driver = "com.mysql.jdbc.Driver";
            Class.forName(driver);
            Connection con = DriverManager.getConnection(obj.getUrl(), obj.getUser(), obj.getPwd());
            return con;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
