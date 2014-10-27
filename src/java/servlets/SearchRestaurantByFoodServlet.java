/*-  Finalized and working 27/11/13 */
package servlets;

import UtitlityClasses.RateCalculator;
import ValidalityCheckSearchRestaurantByFood.ValidChackerSearchRestaruantByFood;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;

@WebServlet(name = "SearchRestaurantByFoodServlet", urlPatterns = {"/SearchRestaurantByFoodServlet"})
public class SearchRestaurantByFoodServlet extends HttpServlet 
{
     Jedis jedis = null;

     public SearchRestaurantByFoodServlet() 
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
            String isUnion = (String) jo.get("union");
            String isIntersact = (String) jo.get("intersact");
            String isMeat = (String) jo.get("meat");
            String isFish = (String) jo.get("fish");
            String isSalads = (String) jo.get("salads");
            String [] foodServed;
            int count = 0;
            
            if (!(isMeat.equals("null")))
            {
                count++;
            }
            if (!(isFish.equals("null")))
            {
                count++;
            }
            if(!(isSalads).equals("null"))
            {
                count++;
            }
            
            if(count!=0)
            {
                foodServed = new String[count];
            }
            else
            {
                foodServed =null;
            }
            
            if(count!=0)
            {
                count = 0;
                 if (!(isMeat.equals("null")))
                {
                    foodServed[count] = "meat";
                    count++;
                }
                if (!(isFish.equals("null")))
                {
                    foodServed[count] = "fish";
                    count++;
                }
                if(!(isSalads).equals("null"))
                {
                    foodServed[count] = "salads";
                    count++;
                }
            }
            // End of JSON part.

            // Try submit
             ValidChackerSearchRestaruantByFood currConnectionValidChack = new ValidChackerSearchRestaruantByFood(jedis,foodServed);
             ValidChackerSearchRestaruantByFood.e_msgType e_msgAccordingToInput = currConnectionValidChack.doPost();

             HttpSession session = request.getSession();
            String user_uid = (session.getAttribute("uid")).toString();
        
             if (e_msgAccordingToInput.compareTo(ValidChackerSearchRestaruantByFood.e_msgType.e_emptyTypeSelectionByUser)==0)
            {
                JSONObject json = new JSONObject();
                json.put("Error Massge", "Not all fields were filled");
                 json.put("Status", "Error");
                 out.print(json);
                 out.flush();
           }
           else
          {
              JSONObject json = new JSONObject();
               JSONArray rest_objects;// = new JSONArray();
               Set<String> ridFound;
            String searchType;
               if (!(isUnion.equals("null")))
                {
                     searchType =  "Union";
                }
               else
               {
                   searchType =  "Intersact";
               }
              
               ridFound = allRidFoundAccordingToQueryType(searchType, foodServed);
      
              rest_objects = buildJSONArrayObject(ridFound, user_uid);
              
              
              json.put("Success", "Success Search");
              json.put("Status", "Success");
              json.put("Results", rest_objects);
              
              out.print(json);
                 out.flush();
          }
     }

        catch (ParseException ex)
        {
             Logger.getLogger(SearchRestaurantByFoodServlet.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally 
        {
             reader.close();
             out.close();   
        }      
        
     }
    
    private Set<String> allRidFoundAccordingToQueryType(String queryType,  String [] foodServed)
    {
        Set<String> ridFound;
        
        if (queryType.compareTo("Union") == 0)
        {
            ridFound = unionOfSelectedFoodType(foodServed);
        }
        
        else
        {
            ridFound = intersectionOfSelectedFoodType(foodServed);
        }
        
        return ridFound;    
    }
    
    private Set<String> unionOfSelectedFoodType(String [] foodServed)
    {
         Set<String> ridFound = new HashSet<String>();
         for(String foodserved:foodServed)
         {
              ridFound.addAll(jedis.smembers(foodserved+"_restaurant_rid"));
         }           
         
         return ridFound;
    }
    
    private Set<String> intersectionOfSelectedFoodType(String [] foodServed)
    {
          Set<String> ridFound = new HashSet<String>();

          for (String foodserved:foodServed)
          {
               if (ridFound.isEmpty())
               {
                     ridFound.addAll(jedis.smembers(foodserved+"_restaurant_rid"));
               }
                     
               else
               {
                     Set<String> tempSet = new HashSet<String>();
                     tempSet.addAll(jedis.smembers(foodserved+"_restaurant_rid"));
                     ridFound.retainAll(tempSet);
              } 
         }
          
          return ridFound;
    }

    private JSONArray buildJSONArrayObject(Set<String>ridFound, String user_uid)
    {
        JSONArray rest_objects = new JSONArray();

        RateCalculator rater = new RateCalculator();
              
        Iterator iter = ridFound.iterator();
        while (iter.hasNext())
        {
           String rid = iter.next().toString();
           String myRate;
           if (jedis.hexists("ranked_restaurants_of_"+user_uid+"_score", rid))
           {
               myRate =  jedis.hget("ranked_restaurants_of_"+user_uid+"_score", rid);
           }
           else
           {
               myRate = "0";
           }
           String RestName = jedis.hget("restaurants_rid_rname",rid);
           String RestAddress = jedis.hget("restaurants_rid_adress",rid);
           String Restlink = jedis.hget("restaurants_rid_link",rid);
           double scoreOfRest = rater.calculate(user_uid, jedis, rid);
              
           
           JSONObject json = new JSONObject();
           json.put("rest_name", RestName);
           json.put("rest_adress", RestAddress);
           json.put("rest_link", Restlink);
           json.put("rest_score", scoreOfRest);
           json.put("user_Rate", myRate);
           
           if(jedis.sismember("meat_restaurant_rid", rid))
             {
                  json.put("isMeat", "true");
             }
             else
             {
                 json.put("isMeat", "false");
             }

           if(jedis.sismember("fish_restaurant_rid", rid))
             {
                  json.put("isFish", "true");
             }
             else
             {
                 json.put("isFish", "false");
             }
           
           if(jedis.sismember("salads_restaurant_rid", rid))
             {
                  json.put("isSalads", "true");
             }
             else
             {
                 json.put("isSalads", "false");
             }


           rest_objects.add(json);
       }
        
       return rest_objects;
    }
   
}
