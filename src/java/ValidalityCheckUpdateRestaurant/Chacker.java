//update by lior asulin 27/11/13
package ValidalityCheckUpdateRestaurant;

import redis.clients.jedis.Jedis;

public class Chacker 
{
    public static boolean isEmptyInput(String rName)
    {
        if((rName == null)||(rName.trim().isEmpty()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean isInDB (String namg, Jedis jedis)
    {
        if (jedis.hexists("restaurants_rname_rid", namg))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
