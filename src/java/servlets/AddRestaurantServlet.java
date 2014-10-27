/*-  Finalized and working 27/11/13 */

package servlets;

import ValidalityCheckAddRestaurant.ValidChackerAddRestaurant;
import ValidalityCheckSignIn.ValidChackerSignIn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@WebServlet(name = "AddRestaurantServlet", urlPatterns = {"/AddRestaurantServlet"})
public class AddRestaurantServlet extends HttpServlet 
{
     Jedis jedis = null;

    public AddRestaurantServlet() 
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
            String name = (String) jo.get("restName");
            String addr = (String) jo.get("restAddress");
            String link = (String) jo.get("restLink");
            
            String  meat = (String) jo.get("meat");
            String  fish = (String) jo.get("fish");
            String  salads = (String) jo.get("salads");
            // End of JSON part.

            String rid = Long.toString(jedis.incr("local:restid"));
            // Try submit
             ValidChackerAddRestaurant currConnectionValidChack = new ValidChackerAddRestaurant (rid, name,addr, link,meat,fish,salads,jedis);
             ValidChackerAddRestaurant.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();

            if (e_msgAccordingToInput.compareTo(ValidChackerAddRestaurant.e_msgType.e_restaurantNotExist) == 0)
            {
                 createJsonIndicateSuccessAndSetDB(name,addr,link,meat,fish,salads,rid, response);
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
    
    private void createJsonIndicateSuccessAndSetDB(String i_name,String i_addr,String i_link,String i_meat, String i_fish,String i_salads,String i_rid, HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        setAllProperAttrbuteInDB(i_name, i_link, i_addr, i_meat,i_fish,i_salads, i_rid, jedis);
     

        try 
        {
            JSONObject content = new JSONObject();  
            content.put("restName", i_name);
            content.put("Success", "\"We have succesfuly added the restaurent to our Database. \\n\\nYou can add more rasturant if you wish.\"");
            content.put("Status", "Success");
            out.print(content);
            out.flush();
        } 
        finally 
        {
            out.close();
        }
   }

    private void setAllProperAttrbuteInDB(String rName, String rLink, String rAddr,String i_meat, String i_fish, String i_salads, String rid, Jedis jedis)
    {
            String foodServedString = new String();

            jedis.hset("restaurants_rname_rid",rName,rid);
            jedis.hset("restaurants_rid_adress",rid,rAddr);
            jedis.hset("restaurants_rid_link",rid,rLink);
            jedis.hset("restaurants_rid_rname",rid,rName);
            
            if(!(i_meat.equals("undefined")))
            {
                foodServedString +="meat#"; 
                jedis.sadd("meat_restaurant_rid",rid);
            }
        
            if(!(i_fish.equals("undefined")))
            {
                foodServedString +="fish#"; 
                jedis.sadd("fish_restaurant_rid",rid);
            }
        
            if(!(i_salads.equals("undefined")))
            {
                foodServedString +="salads#"; 
                jedis.sadd("salads_restaurant_rid",rid);
            }
                       
            jedis.hset("restaurants_rid_foodServed", rid, foodServedString);     
    }
    
    private void createJsonIndicateFailiure(ValidChackerAddRestaurant.e_msgType e_ErrorMsgInfo,  HttpServletResponse response)
    throws IOException 
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
       
        try 
        {
            JSONObject content = new JSONObject(); 
            
            if(e_ErrorMsgInfo.compareTo(ValidChackerAddRestaurant.e_msgType.e_SomeFieldsEmpty)==0)
            {
                 content.put("Error Massge", "You must fill all fields in order to add restaurant");
            }
            else if(e_ErrorMsgInfo.compareTo(ValidChackerAddRestaurant.e_msgType.e_NoSelectedTypeAtAll)==0)
            {
                content.put("Error Massge", "You must select at least one type for the restaurant");
            }
            else// if (e_ErrorMsgInfo.compareTo(ValidChackerAddRestaurant.e_msgType.e_RestaurantInDB) == 0)
            {
                content.put("Error Massge", "Restaurant in the database already");
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
