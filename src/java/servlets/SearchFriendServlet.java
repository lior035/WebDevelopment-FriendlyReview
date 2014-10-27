package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
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

@WebServlet(name = "SearchFriendServlet", urlPatterns = {"/SearchFriendServlet"})
public class SearchFriendServlet extends HttpServlet 
{
        Jedis jedis = null;

        public SearchFriendServlet() 
        {
          super();
          jedis= new Jedis("localhost");  
        }
    
    private JSONArray setArrayByCritetion (String user_id, String criterion,  HttpServletRequest request)
    {
        
        JSONArray jArrayAll = new JSONArray();
        JSONArray jArrayRegular = new JSONArray();
        JSONArray jArrayFavorite = new JSONArray();
        
        Set<String>hashField = jedis.hkeys("friends_of_"+user_id);
        List<String>hashValue = jedis.hvals("friends_of_"+user_id);
        
        Iterator fieldItr = hashField.iterator();
        Iterator valItr = hashValue.iterator();
        
        while (fieldItr.hasNext())
        {
            JSONObject json = new JSONObject();
        
            String friendId = fieldItr.next().toString();
            String friendName = jedis.hget("users_uid_uname", friendId);
            String friendType = valItr.next().toString();
                      
            json.put("Friend name", friendName);
            json.put("User name", jedis.hget("profile_of_u"+friendId, "name"));
            json.put("Email", jedis.hget("profile_of_u"+friendId, "email"));
            json.put("Birthday", jedis.hget("profile_of_u"+friendId, "birthday"));
            json.put("Address", jedis.hget("profile_of_u"+friendId, "address"));

            if(friendType.compareTo("0")==0)
            {
                friendType = "Regular";
                json.put("Friend type", friendType);
                jArrayRegular.add(json);
            }
            else
            {
                friendType = "Favorite";
                json.put("Friend type", friendType);
                jArrayFavorite.add(json);
            }
            jArrayAll.add(json);
        }
        
        if (criterion.compareTo("All")==0)
        {
            return jArrayAll;
        }
        else if (criterion.compareTo("Favorite")==0)
        {
            return jArrayFavorite;
        }
        else //if (criterion.compareTo("Regular")==0)
        {
            return jArrayRegular;
        }
    }
    
    private String takeCareSearchByCriterion (String user_id, String criterion,  HttpServletRequest request, PrintWriter out )
    {        
        String error = null;
        if (criterion.compareTo("All")==0)
        {
            JSONArray jArry = setArrayByCritetion(user_id, criterion, request);
            
            if((jArry == null)||(jArry.size()==0))
            {
              error = "Error - you don't have friends";
            }
            else
            {
                out.print(jArry);
                out.flush();
            }
        }
        else if (criterion.compareTo("Favorite")==0)
        {
            JSONArray jArry = setArrayByCritetion(user_id, criterion, request);
            
            if((jArry == null)||(jArry.size()==0))
            {
                error = "Error - you don't have any favorite friends";
            }
            else
            {
                out.print(jArry);
                 out.flush();
            }
        }
        else if(criterion.compareTo("Regular")==0)
        {
            JSONArray jArry = setArrayByCritetion(user_id, criterion, request);
            
            
            if((jArry == null)||(jArry.size()==0))
            {
                error =  "Error - you don't have any regular friends";
            }
            else
            {
                 out.print(jArry);
                 out.flush();
            }
        }
        
        return error;
    }
    
    private String takeCareSearchByName(String user_id, String friendName, PrintWriter out)
    {
       
        String friend_id = jedis.hget("users_uname_uid", friendName);
        String returnStr = null;
        
        if (friendName.trim().isEmpty())
        {
            returnStr =  "Error - you can not enter empty friend name to search";
            
        }
        
        else if(friend_id == null)
        {
            returnStr = "The friends you searched for is not in our DB";
           
        }
        else if(jedis.hexists("friends_of_"+user_id, friend_id))
        {
            JSONObject json = new JSONObject();

            String friend_Type = jedis.hget("friends_of_"+user_id, friend_id);
            
            if(friend_Type.compareTo("0")==0)
            {
                friend_Type="Regular";
            }
            else
            {
                friend_Type="Favorite";
            }
            
            json.put("Friend name", friendName);
            json.put("Friend type", friend_Type);
         
            json.put("User name", jedis.hget("profile_of_u"+friend_id, "name"));
            json.put("Email", jedis.hget("profile_of_u"+friend_id, "email"));
            json.put("Birthday", jedis.hget("profile_of_u"+friend_id, "birthday"));
            json.put("Address", jedis.hget("profile_of_u"+friend_id, "address"));
            JSONArray jArry = new JSONArray();
            jArry.add(json);
            out.print(jArry);
            out.flush();
             
        }
        else
        {
             returnStr = "We couldn't find the user you are looking for among your friends";
                       
        }
        
        return  returnStr;
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String requestType;
        String name;
        String searchCriterion;
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
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
            requestType = (String) jo.get("type");
            searchCriterion = (String) jo.get("searchCriterion");
        
        
            HttpSession session = request.getSession();
            String user_uid = (session.getAttribute("uid")).toString();
        
            if(requestType.compareTo("byName")==0)
            {
               
                String error = takeCareSearchByName(user_uid, name,out);
                if (error != null)
                {
                    JSONObject json = new JSONObject();
                    JSONArray jArry = new JSONArray();
                    jArry.add(json);
                    json.put("status","error");
                    json.put("errorMsg",error);
                    out.print(jArry);
                    out.flush();
                    
                }
          
            }
           else if(requestType.compareTo("byType")==0)
            {
               String criterionType =  searchCriterion;
               String error = null;
               error = takeCareSearchByCriterion(user_uid, criterionType, request,out );
                 if (error != null)
                {
                    JSONObject json = new JSONObject();
                    JSONArray jArry = new JSONArray();
                    jArry.add(json);
                    json.put("status","error");
                    json.put("errorMsg",error);
                    out.print(jArry);
                    out.flush();
                    
                }
            
            }
        
       
      }
        
        catch (ParseException ex)
        {
             Logger.getLogger(SearchFriendServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            reader.close();
             out.close();
        }
        
        }
    }


