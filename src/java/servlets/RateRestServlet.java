/*Finalized and working 27/11/13*/

package servlets;

import ValidalityCheckRestaurantRate.ValidChackerRestaurantRate;
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

@WebServlet(name = "RateRestServlet", urlPatterns = {"/RateRestServlet"})
public class RateRestServlet extends HttpServlet
{
     Jedis jedis = null;
     
     public RateRestServlet()
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
         HttpSession session = request.getSession();
         String uid = (session.getAttribute("uid")).toString();

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
            String nameRest = (String) jo.get("restaurantName");
            String userNewScore = (String) jo.get("userScore");
         
            // End of JSON part.

             ValidChackerRestaurantRate currConnectionValidChack = new ValidChackerRestaurantRate (jedis, nameRest, userNewScore, uid);
             ValidChackerRestaurantRate.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();
             
             String rid = jedis.hget("restaurants_rname_rid", nameRest);
             
             if (e_msgAccordingToInput.compareTo(ValidChackerRestaurantRate.e_msgType.e_success) == 0)
             {
                JSONObject json = new JSONObject();
                json.put("Success", "Your rate was successful, thanks for your opinion.");
                json.put("Status", "Success");
                out.print(json);
                out.flush();
                jedis.hset("ranked_restaurants_of_"+uid+"_score",rid,userNewScore);
             }
            
            else
            {
                JSONObject json = new JSONObject();  
                json.put("Error Massge","Something went wrong...");
                json.put("Status", "Error");
                out.print(json);
                out.flush();
            }         
        }

        catch (ParseException ex)
        {
             Logger.getLogger(RateRestServlet.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally 
        {
             reader.close();
             out.close();   
        }
    }
}
