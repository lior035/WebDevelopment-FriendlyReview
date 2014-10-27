/*update by lior asulin 27/11/13*/

package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import redis.clients.jedis.Jedis;
import org.json.simple.JSONObject;

@WebServlet(name = "GetMyProfileServlet", urlPatterns = {"/GetMyProfileServlet"})
public class GetMyProfileServlet extends HttpServlet 
{
    private static Jedis jedis;
    
    private static JSONObject get_profile_from_mysql(String uid)
    {
        JSONObject returned = new JSONObject();
        Connection conn = null;
        Statement s = null;
        String table_name = "profile";
        try 
        {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/test","root","");                
            s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM " + table_name+" WHERE UID = "+uid+";");
            boolean found_value_in_database = false;
            while(rs.next())
            {
                returned.put("name",rs.getString(2));
                returned.put("email",rs.getString(3));
                returned.put("address",rs.getString(4));
                returned.put("birthday",rs.getString(5));
                
                found_value_in_database = true;
            }
    
            if (! found_value_in_database)
            {
                returned.put("error_sql","no values in mysql");
            }
        }
        
        catch (SQLException ex)
        {
            returned.put("error_sql","SQL Exception:" + ex.getMessage());            
        }
        
        catch (ClassNotFoundException ex) 
        { 
            returned.put("error_sql","MYSQL Server: Class not found");
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
        return returned;
    }
    
    private static JSONObject get_profile_from_redis(String uid)
    {
        JSONObject content = new JSONObject();
        String db_profile_collection = "profile_of_u" + uid;
        Jedis jedis = new Jedis("localhost");        
                
        if (jedis.hexists(db_profile_collection, "name"))
        {
            Set <String> profile_fields = jedis.hkeys(db_profile_collection);
            
            for (String str : profile_fields)
            {
                      String value = jedis.hget(db_profile_collection, str);
                      content.put(str,value);
            }    
        }
        
        else
        {
            content.put("error_redis","No profile values in redis");                        
        }

        return content;
    }
    
    public static JSONObject get_profile(String uid)
    {
           JSONObject content = get_profile_from_mysql(uid);
           if (content.containsKey("error_sql"))
           {
               content = get_profile_from_redis(uid);
           }
           
           return content;
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
        try 
        {
            HttpSession session = request.getSession();
            String uid = (session.getAttribute("uid")).toString();
            JSONObject content = this.get_profile(uid);            
            out.print(content);
            out.flush();
        } 
        finally 
        {
            out.close();
        }
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
    throws ServletException, IOException 
    {
        processRequest(request, response);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException 
    {
        processRequest(request, response);
    }
}
