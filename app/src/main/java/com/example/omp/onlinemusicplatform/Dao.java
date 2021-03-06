package com.example.omp.onlinemusicplatform;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Dao {
    boolean IsLoginSuccess = false;


    protected void LoginSuccess()
    {
        this.IsLoginSuccess = true;
    }

    protected boolean CheckLogin(final String UserName, final String HashedPassword, final boolean isAdmin) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() {
                try {
                    Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    String sql = null;
                    Connection conn =null;
                    //1. MySQL(http://www.mysql.com)mm.mysql-2.0.2-bin.jar
                    Class.forName("com.mysql.jdbc.Driver");
                    DriverManager.setLoginTimeout(10);
                    conn =  DriverManager.getConnection("jdbc:mysql://50.62.209.122:3306/OMP", "rife-omp", "Welcome1234");
                    //con = DriverManager.getConnection("jdbc:mysql://50.62.209.122:3306/OMP?user=StupidLaw&password=Iam87&&useUnicode=true&characterEncoding=UTF-8");
                    if(isAdmin == true) {
                        sql = "SELECT * FROM users where name='" + UserName + "' and hashed_password='" + HashedPassword + "' and isAdmin = '1'";
                    }
                    else {
                        sql = "SELECT * FROM users where name='" + UserName + "' and hashed_password='" + HashedPassword + "'";
                    }

                    Statement statement = conn.createStatement();
                    ResultSet rs = statement.executeQuery(sql);
                    if (rs.next()) {
                        return 1;

                        //result[0] = true;
                        //latch.countDown();
                        //Toast.makeText(getApplicationContext(), rs.getString("name"), Toast.LENGTH_LONG).show();
                    }
                    if(conn!=null)
                        conn.close();
                    if(rs!=null)
                        rs.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return 0;
            }
        };
        Future<Integer> future = executor.submit(callable);
        if(future.get()==1)
            this.IsLoginSuccess = true;
        else
            this.IsLoginSuccess = false;
        executor.shutdown();
        return this.IsLoginSuccess;
        /*new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                 Class.forName("net.sourceforge.jtds.jdbc.Driver");

                Connection conn =null;
                //1. MySQL(http://www.mysql.com)mm.mysql-2.0.2-bin.jar
                Class.forName("com.mysql.jdbc.Driver");
                DriverManager.setLoginTimeout(10);
                conn =  DriverManager.getConnection("jdbc:mysql://50.62.209.122:3306/OMP", "rife-omp", "Welcome1234");
                //con = DriverManager.getConnection("jdbc:mysql://50.62.209.122:3306/OMP?user=StupidLaw&password=Iam87&&useUnicode=true&characterEncoding=UTF-8");
                String sql = "SELECT * FROM users where name='" + UserName + "' and hashed_password='" + HashedPassword + "'";
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) {
                    LoginSuccess();
                    //result[0] = true;
                    //latch.countDown();
                    //Toast.makeText(getApplicationContext(), rs.getString("name"), Toast.LENGTH_LONG).show();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // your code goes here...
        }}).start();
        //final boolean[] result = new boolean[1];

        return this.IsLoginSuccess;*/
    }
}
