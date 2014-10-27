/*-  Finalized and working 27/11/13 */
package ValidalityCheckSearchRestaurantByFood;

import javax.servlet.http.HttpServletRequest;
import redis.clients.jedis.Jedis;

public class ValidChackerSearchRestaruantByFood 
{
    Jedis jedis;
    String [] foodServed;
    
     public static enum e_msgType
    {
        e_emptyTypeSelectionByUser,
        e_success;
    }
     
    public ValidChackerSearchRestaruantByFood (Jedis jedis,String[] foodServed)
    {
        this.jedis = jedis;
        this.foodServed = foodServed;
    }
    
    public ValidChackerSearchRestaruantByFood.e_msgType doPost()
    {
        if(!Chacker.isAtLeastOneTypeSelected(foodServed))
        {
            return ValidChackerSearchRestaruantByFood.e_msgType.e_emptyTypeSelectionByUser;
        }
        else
        {
            return ValidChackerSearchRestaruantByFood.e_msgType.e_success;
        }
    }
}
