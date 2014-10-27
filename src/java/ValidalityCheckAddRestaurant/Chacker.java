/*-  Finalized and working 27/11/13 */
package ValidalityCheckAddRestaurant;

import redis.clients.jedis.Jedis;

public class Chacker
{
    public static boolean isAllFieldFillds(String rName, String rAddr, String rLink)  
    {
        if ((rName.trim().isEmpty()) || (rAddr.trim().isEmpty()) || (rLink.trim().isEmpty()))
        {
            return false;
        }
        
        else
        {
            return true;
        }
    }     
    
    public static boolean isAtLeastOneTypeWasChosen(boolean i_fish, boolean i_meat, boolean i_salads)
    {
        if ((i_fish == false)&&(i_meat==false)&&(i_salads==false))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    public static boolean isRasturantExistsInDB(String restName, Jedis jedis)
    {
        if (jedis.hexists("restaurants_rname_rid",restName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
