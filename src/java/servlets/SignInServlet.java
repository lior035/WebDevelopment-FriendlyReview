/*-  Finalized and working 27/11/13 */

package servlets;

import ValidalityCheckSignIn.ValidChackerSignIn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;

@WebServlet(name = "SignInServlet", urlPatterns = {"/SignInServlet"})
public class SignInServlet extends HttpServlet 
{
    Jedis jedis = null;
    
     public SignInServlet()
    {
        super();
       
        jedis= new Jedis("localhost");  
    }
     
    /**
     * Handles the HTTP
     * <code>POST</code> method.
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
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Json handle part 
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        try 
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append('\n');
            }

            String a = sb.toString();
            JSONParser jp = new JSONParser();
            Object o  = jp.parse(a);
            JSONObject jo = (JSONObject)o;

            // JSON part , get values from json.
            String name = (String) jo.get("name");
            String pass = (String) jo.get("password");
           
            // End of JSON part.

            // Try submit
             ValidChackerSignIn currConnectionValidChack = new ValidChackerSignIn (name,pass, jedis);
             ValidChackerSignIn.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();

            if (e_msgAccordingToInput.compareTo(ValidChackerSignIn.e_msgType.e_success) == 0)
            {
                HttpSession session = request.getSession();
                session.setAttribute("userName",name);
                session.setAttribute("uid",jedis.hget("users_uname_uid",name ));
//                   setAttributeOfUser(request);
                 createJsonIndicateSuccess(name, response);
            }
            else
            {
                 createJsonIndicateFailiure(e_msgAccordingToInput, response);
            }            
        }

        catch (ParseException ex)
        {
             Logger.getLogger(SignInServlet.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally 
        {
             reader.close();
             out.close();   
        }
 
    }
 
    private void createJsonIndicateFailiure(ValidChackerSignIn.e_msgType e_ErrorMsgInfo,  HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
       
        try 
        {
            JSONObject content = new JSONObject(); 
            
            if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_errorNotAllSignInFieldsFilled) == 0)
            {
                 content.put("Error Massge", "Not all fields were filled");
            }
            else if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_userNotExists) == 0)
            {
                 content.put("Error Massge", "User name is not exsits");
            }
            else
            {
                content.put("Error Massge","Password does not match user name");
            }
            
            content.put("Status", "Error");
            out.print(content);
            out.flush();
        } 
        finally 
        {
            out.close();
        }
    }
    
//      private void setAttributeOfUser(HttpServletRequest request)
//    {
//         
//    }
      
      
    private void createJsonIndicateSuccess(String name, HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try 
        {
            JSONObject content = new JSONObject();  
            content.put("userName", name);
            content.put("uid",jedis.hget("users_uname_uid",name));
            content.put("Success", "Success login");
            content.put("Status", "Success");
            out.print(content);
            out.flush();
        } 
        finally 
        {
            out.close();
        }
   }
}
