/*Finalized and working 27/11/13*/

package ValidalityCheckRestaurantRate;

import redis.clients.jedis.Jedis;

public class Chacker
{   
    public static boolean isRateIsLegal(Jedis jedis, String grade, int maxAllowed)  
    {  
        int rate;
        
        try  
        {  
            rate = Integer.parseInt(grade);    
        }        
        catch(NumberFormatException nfe)  
        {  
            return false;  
        }  
        
        if ((rate > maxAllowed) || (rate < 1))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    public static boolean isRestaurantExistsInDB (Jedis jedis, String restName)
    {
        if (jedis.hexists("restaurants_rname_rid", restName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean isUserAlreadyRateTheRestaurant (Jedis jedis, String restName, String uid)
    {
        String rid = jedis.hget("restaurants_rname_rid", restName);
        if (jedis.hexists("ranked_restaurants_of_"+uid+"_score",rid))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
