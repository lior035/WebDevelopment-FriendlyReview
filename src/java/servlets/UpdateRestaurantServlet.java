/* */
package servlets;

import ValidalityCheckUpdateRestaurant.ValidChackerUpdateRestaurant;
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

@WebServlet(name = "UpdateRestaurantServlet", urlPatterns = {"/UpdateRestaurantServlet"})
public class UpdateRestaurantServlet extends HttpServlet
{
     Jedis jedis = null;
     
      public UpdateRestaurantServlet() 
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
              String meat = (String) jo.get("meat");
              String salads = (String) jo.get("salads");
              String fish = (String) jo.get("fish");
           
              // End of JSON part.

            ValidChackerUpdateRestaurant currConnectionValidChack = new ValidChackerUpdateRestaurant (jedis,restName);
            ValidChackerUpdateRestaurant.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();
        
            if (e_msgAccordingToInput.compareTo(ValidChackerUpdateRestaurant.e_msgType.e_success) == 0)
            {
                createJsonIndicateSuccessAndSetDetailsOnDB(restName, meat, fish, salads, response) ;
            }
            else if (e_msgAccordingToInput.compareTo(ValidChackerUpdateRestaurant.e_msgType.e_EmptyInput) == 0)
            {
                createJsonIndicateFailiure(e_msgAccordingToInput,  response);
            }
            else //if (e_msgAccordingToInput.compareTo(ValidChackerUpdateRestaurant.e_msgType.e_NotInDB) == 0)
            {
                createJsonIndicateFailiure(e_msgAccordingToInput,  response);
            }      
         }

        catch (ParseException ex)
        {
             Logger.getLogger(UpdateRestaurantServlet.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally 
        {
             reader.close();
             out.close();   
        }
    }
    
    private void createJsonIndicateFailiure(ValidChackerUpdateRestaurant.e_msgType e_ErrorMsgInfo, HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
       
        try 
        {
            JSONObject content = new JSONObject(); 
            
            if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_EmptyInput) == 0)
            {
                 content.put("Error Massge", "Empty input is not allowed");
            }
            else if (e_ErrorMsgInfo.compareTo(e_ErrorMsgInfo.e_NotInDB) == 0)
            {
                 content.put("Error Massge",  "Restaurant is not in our Database");
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
    
    private void createJsonIndicateSuccessAndSetDetailsOnDB(String restName, String meat ,String fish,String salads, HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
       
        try 
        {
            JSONObject content = new JSONObject();  
            content.put("restName", restName);
            content.put("Success", "successfully updated restaurant.");
            content.put("Status", "Success");
            
            String restId = jedis.hget("restaurants_rname_rid",restName);
            String foodServed  = jedis.hget("restaurants_rid_foodServed",restId);
              
            if(!(meat.equals("undefined")))
            {
                foodServed +="meat#"; 
                jedis.sadd("meat_restaurant_rid",restId);
            }
        
            if(!(fish.equals("undefined")))
            {
                foodServed +="fish#"; 
                jedis.sadd("fish_restaurant_rid",restId);
            }
        
            if(!(salads.equals("undefined")))
            {
                foodServed +="salads#"; 
                jedis.sadd("salads_restaurant_rid",restId);
            }
           
            jedis.hset("restaurants_rid_foodServed",restId,foodServed);

            out.print(content);
            out.flush();
        } 
        finally 
        {
            out.close();
        }
    }
}
