/*-  Finalized and working 27/11/13 */
package servlets;

import ValidalityCheckSignUp.Chacker;
import ValidalityCheckSignUp.ValidChackerSignUp;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;


@WebServlet(name = "SignUpServlet", urlPatterns = {"/SignUpServlet"})
public class SignUpServlet extends HttpServlet 
{
    Jedis jedis = null;
    
    public SignUpServlet()
    {
        super();
       
        jedis= new Jedis("localhost");  
    }
   
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
 
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
            String email = (String) jo.get("email");
            String rePass = (String) jo.get("rePassword");
           
            // End of JSON part.

            ValidChackerSignUp currConnectionValidChack = new ValidChackerSignUp (name,pass,email,rePass, jedis);
            ValidChackerSignUp.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();
        
            if (e_msgAccordingToInput.compareTo(ValidChackerSignUp.e_msgType.e_success) == 0)
            {
                createJsonIndicateSuccessAndSetDetailsOnDB(name, pass, email, response) ;
            }
            else
            {
                createJsonIndicateFailiure(e_msgAccordingToInput,  response);
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

    private void createJsonIndicateSuccessAndSetDetailsOnDB(String i_name,String i_pass ,String i_Email, HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try 
        {
            JSONObject content = new JSONObject();  
            content.put("userName", i_name);
            content.put("uid",jedis.hget("users_uname_uid",i_name));
            content.put("Success", "Registration succeeded, enter your details to log in.");
            content.put("Status", "Success");
            
            String userId = Long.toString(this.jedis.incr("local:userid"));
            this.jedis.hset("users_uname_uid",i_name,userId) ;
            this.jedis.hset("users_uid_uname",userId,i_name) ;
            this.jedis.hset("users_uid_uemail", userId , i_Email);
            this.jedis.hset("users_uid_pass", userId , i_pass);
        
            out.print(content);
            out.flush();
        } 
        finally 
        {
            out.close();
        }
    }
    
    private void createJsonIndicateFailiure(ValidChackerSignUp.e_msgType e_ErrorMsgInfo,  HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
       
        try 
        {
            JSONObject content = new JSONObject(); 
            
            if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_errorEmailAlreadyExists) == 0)
            {
                 content.put("Error Massge", "Email is already taken");
            }
            else if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_errorNotAllSignUpFieldsFilled) == 0)
            {
                 content.put("Error Massge",  "Not all fields were filled");
            }
            else if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_errorPasswordsNotMatch) == 0)
            {
                 content.put("Error Massge", "Passwords don't match");
            }
            else if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_errorUserNameAlreadyExists) == 0)
            {
                 content.put("Error Massge", "User name is already taken");
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
}

