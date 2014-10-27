package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import org.json.simple.JSONObject;
import ValidalityCheckSearchRestaurantByName.ValidChackerSearchRestaruantByName;
import javax.servlet.http.HttpSession;
import UtitlityClasses.RateCalculator;
import ValidalityCheckSignIn.ValidChackerSignIn;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@WebServlet(name = "SearchRestaurantByNameServlet", urlPatterns = {"/SearchRestaurantByNameServlet"})
public class SearchRestaurantByNameServlet extends HttpServlet 
{    
    Jedis jedis = null;
    
     public SearchRestaurantByNameServlet() 
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
            String restName = (String) jo.get("restName");
            
            // End of JSON part.

            // Try submit
             ValidChackerSearchRestaruantByName currConnectionValidChack = new ValidChackerSearchRestaruantByName (jedis, restName);
             ValidChackerSearchRestaruantByName.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();

             HttpSession session = request.getSession();
            String user_uid = (session.getAttribute("uid")).toString();
            if (e_msgAccordingToInput.compareTo(ValidChackerSearchRestaruantByName.e_msgType.e_success)==0)
            {    
               createJsonIndicateSuccess(restName, response, user_uid);     
            }
            
            else if (e_msgAccordingToInput.compareTo(ValidChackerSearchRestaruantByName.e_msgType.e_restaurantWasNotFoundInDB)==0) 
            {          
                createJsonIndicateFailiure(e_msgAccordingToInput, response, restName);
            }  
           
            else if (e_msgAccordingToInput.compareTo(ValidChackerSearchRestaruantByName.e_msgType.e_restaurantNameIllegal)==0)
            {
                createJsonIndicateFailiure(e_msgAccordingToInput, response, restName);
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
    
     private void createJsonIndicateFailiure(ValidChackerSearchRestaruantByName.e_msgType e_ErrorMsgInfo,  HttpServletResponse response, String restName)
     throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
       
        try 
        {
            JSONObject content = new JSONObject(); 
            if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_restaurantNameIllegal) == 0)
            {
                 content.put("Error Massge", "Illegal input, can not be empty.");
            }
            else if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_restaurantWasNotFoundInDB) == 0)
            {
                 content.put("Error Massge", "We did not found what you were looking for.");
                 content.put("restName",restName);
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
    
    private void createJsonIndicateSuccess(String name, HttpServletResponse response, String user_uid)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
         String restSearched = name;
         
        
         String rid = jedis.hget("restaurants_rname_rid",restSearched);
         String rest_name = jedis.hget("restaurants_rid_rname",rid);
         String rest_adress = jedis.hget("restaurants_rid_adress",rid);
         String rest_link = jedis.hget("restaurants_rid_link",rid);
             
         RateCalculator rater = new RateCalculator();
         double scoreOfRest = rater.calculate(user_uid, jedis, rid);
              
        try 
        {
            JSONObject content = new JSONObject(); 
        
            content.put("userName", name);
            content.put("uid",jedis.hget("users_uname_uid",name));
            content.put("rest_name", rest_name);
            content.put("rest_adress", rest_adress);
            content.put("rest_link", rest_link);
            content.put("rest_score", scoreOfRest);
            
             if(jedis.sismember("meat_restaurant_rid", rid))
             {
                  content.put("isMeat", "true");
             }
             else
             {
                 content.put("isMeat", "false");
             }
             
             if(jedis.sismember("fish_restaurant_rid", rid))
             {
                  content.put("isFish", "true");
             }
             else
             {
                 content.put("isFish", "false");
             }
             
             if(jedis.sismember("salads_restaurant_rid", rid))
             {
                  content.put("isSalads", "true");
             }
             else
             {
                 content.put("isSalads", "false");
             }
             
            content.put("Success", "We found what you were looking for.");
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

