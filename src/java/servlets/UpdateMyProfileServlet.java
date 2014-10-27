/*Editing*/

package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import redis.clients.jedis.Jedis;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;

@WebServlet(name = "UpdateMyProfileServlet", urlPatterns = {"/UpdateMyProfileServlet"})
public class UpdateMyProfileServlet extends HttpServlet 
{
    private static Jedis jedis;
//    private static Jdbc jdbc;
    private static JSONObject update_profile_on_mysql(String uid,String name,String email,String address,String birthday)
    {
        String status_String = "SqlFine";
        Connection conn = null;
        Statement s = null;
        String table_name = "profile";
        String in = '"'+uid+'"'+','+ '"'+name+'"'+','+'"'+email+'"'+',';
        in += '"'+address+'"'+','+ '"'+birthday+'"';
        try
        {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://localhost/test","root","");
            
                s = conn.createStatement();
                System.out.println("Deleting profile from" + table_name);
                s.executeUpdate("DELETE FROM " + table_name+" WHERE UID = "+uid +" ;");//s.executeUpdate("DELETE FROM " + table_name);
                System.out.println("Inserting "+ uid +" to" + table_name);
                s.executeUpdate("INSERT INTO "+ table_name +" VALUES (" + in + ");");
        }
        catch (SQLException ex)
        {
            status_String = "Error: MYSQL Server<br>";
            status_String += "SQL Exception:" + ex.getMessage() + "<br>";
            status_String += "SQL State:" + ex.getSQLState() + "<br>";
            status_String += "SQL Error Code:" + ex.getErrorCode() + "<br>";            
            
        }
        catch (ClassNotFoundException ex)
        { 
            status_String = "Error: MYSQL Server: Class not found<br>";
        }
        finally 
        {
            if (s != null)
            {
                try 
                {
                    s.close();
                } 
                catch (SQLException sqlEx) 
                {
                }
                s = null;
            }
        }
        
        JSONObject returned_status = new JSONObject();
        returned_status.put("status",status_String);
        return returned_status;
    }
    
    private static void update_profile_on_redis(String uid,String name,String email,String address,String birthday)
    {
        Jedis jedis = new Jedis("localhost");
        String db_profile_collection = "profile_of_u" + uid;
        jedis.hset(db_profile_collection, "name",name);
        jedis.hset(db_profile_collection, "email",email);
        jedis.hset(db_profile_collection, "address",address);
        jedis.hset(db_profile_collection, "birthday",birthday);
    }
    
    private  static boolean checkAllLegalInput(String name,String email,String address,String birthday)
    {
        if((name.trim().isEmpty())||(email.trim().isEmpty())||(address.trim().isEmpty())||(birthday.trim().isEmpty()))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    public static JSONObject update_profile(String uid,String name,String email,String address,String birthday)
    {
           if(UpdateMyProfileServlet.checkAllLegalInput(name, email, address, birthday))
           {
               update_profile_on_redis(uid,name,email,address,birthday);
               JSONObject returned_status = update_profile_on_mysql(uid,name,email,address,birthday);
               return returned_status;
           }
           else
           {
               return null;
           }
           
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Json handle part 
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            String a = sb.toString();
            JSONParser jp = new JSONParser();
            Object o  = jp.parse(a);
            JSONObject jo = (JSONObject)o;

            // JSON part , get values from json.
            String name = (String) jo.get("name");
            String email = (String) jo.get("email");
            String address = (String) jo.get("address");
            String birthday = (String) jo.get("birthday");

            // End of JSON part.

            // Update the databases.
            HttpSession session = request.getSession();
            String uid = (session.getAttribute("uid")).toString();        
            JSONObject returned_status = update_profile(uid,name,email,address,birthday);
            out.print(returned_status);
            out.flush();
        }

        catch (ParseException ex) {
                    Logger.getLogger(TestServlet.class.getName()).log(Level.SEVERE, null, ex);

        } 
        finally {

                /* TODO output your page here. You may use following sample code. */

                reader.close();
                out.close();
            }
        }

        // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
        /**
         * Handles the HTTP <code>GET</code> method.
         *
         * @param request servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException if an I/O error occurs
         */
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            processRequest(request, response);
        }

        /**
         * Handles the HTTP <code>POST</code> method.
         *
         * @param request servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException if an I/O error occurs
         */
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            processRequest(request, response);
        }

        /**
         * Returns a short description of the servlet.
         *
         * @return a String containing servlet description
         */
        @Override
        public String getServletInfo() {
            return "Short description";
        }// </editor-fold>

}
