
package servlets;

import java.io.IOException;
import ValidalityCheckAddRemoveFriend.ValidChackerAddRemoveFriend;
import java.io.BufferedReader;
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

@WebServlet(name = "RemoveFriendServlet", urlPatterns = {"/RemoveFriendServlet"})
public class RemoveFriendServlet extends HttpServlet
{
    
     Jedis jedis = null;
     
       public RemoveFriendServlet() 
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
        HttpSession session = request.getSession();
        
        // Json handle part 
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String name;
        JSONObject returned_status = new JSONObject();
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
            name = (String) jo.get("name");
            
       
        String user_uid = (request.getSession().getAttribute("uid")).toString();
        ValidChackerAddRemoveFriend currConnectionValidChack = new ValidChackerAddRemoveFriend(jedis, name, user_uid);
        ValidChackerAddRemoveFriend.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();
        
        if (e_msgAccordingToInput.compareTo(ValidChackerAddRemoveFriend.e_msgType.e_UserIsYourFriend) ==0)
        {
             String friend_uid = jedis.hget("users_uname_uid",name);
             jedis.hdel("friends_of_"+user_uid,friend_uid);
             
            
             String status_String = name+" was successfully removed from your friend.";
             returned_status.put("status",status_String);
             returned_status.put("success","1");
             
        }
        
        else
        {
            String status_String; 
            status_String = this.AddAttributeAccordingMassge(e_msgAccordingToInput,  request, name);
            returned_status.put("status",status_String);
            returned_status.put("success","-1");
           
        }
        
        out.print(returned_status);
        out.flush();
    }
        
        catch (ParseException ex) {
                   Logger.getLogger(RemoveFriendServlet.class.getName()).log(Level.SEVERE, null, ex);

        } 
            finally {

                /* TODO output your page here. You may use following sample code. */

                reader.close();
                out.close();
            }
        
    }
    
        
        
        
    private String AddAttributeAccordingMassge(ValidChackerAddRemoveFriend.e_msgType e_ErrorMsgInfo, HttpServletRequest request, String name)
    throws IOException 
    {
         return this.getErrorMsg(e_ErrorMsgInfo, request, name); 
    }
    
    private String getErrorMsg(ValidChackerAddRemoveFriend.e_msgType e_ErrorMsgInfo,HttpServletRequest request, String name)
    {
        String returnStr = new String();
        if (e_ErrorMsgInfo.compareTo(ValidChackerAddRemoveFriend.e_msgType.e_FieldIsEmpty) == 0)
        {
             returnStr = "You need to enter friend name";
        }

        else  if (e_ErrorMsgInfo.compareTo(ValidChackerAddRemoveFriend.e_msgType.e_success)==0)
        {
            //meaning that friend name exists but he is not your friend so can't remove him
            returnStr = name+" is not your friend.";
        }
        
        
        else if (e_ErrorMsgInfo.compareTo(ValidChackerAddRemoveFriend.e_msgType.e_canNotAddOrRemoveYourselveAsFriend)==0)
        {
            returnStr = "You can not add yourselve as your friend";
        }
        
        else if (e_ErrorMsgInfo.compareTo(ValidChackerAddRemoveFriend.e_msgType.e_FriendNotExist) == 0)
        {
            returnStr = "This user is not in our database";
        }   
        
        return  returnStr;
    }
}
