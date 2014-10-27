/* Finalized and working 27/11/13 */
package ValidalityCheckSearchRestaurantByName;

import redis.clients.jedis.Jedis;

public class Chacker
{    
    public static boolean isRestaurantExistsInDB(Jedis jedis, String restName)
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
    
    public static boolean isEmptyInput(String restName)
    {
        if(restName.trim().isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
