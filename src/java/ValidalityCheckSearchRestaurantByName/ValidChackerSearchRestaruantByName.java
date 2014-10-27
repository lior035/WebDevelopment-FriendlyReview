/* Finalized and working 27/11/13 */
package ValidalityCheckSearchRestaurantByName;

import redis.clients.jedis.Jedis;

public class ValidChackerSearchRestaruantByName
{
    String restNameSearchedFor;
    Jedis jedis;
    
    public static enum e_msgType
    {
        e_restaurantWasNotFoundInDB,
        e_restaurantNameIllegal,
        e_success;
    }
    
    public ValidChackerSearchRestaruantByName(Jedis jedis, String lookFor)
    {
        this.restNameSearchedFor = lookFor;
        this.jedis = jedis;
    }
    
    public ValidChackerSearchRestaruantByName.e_msgType doPost()
    {
        if (Chacker.isEmptyInput(restNameSearchedFor))
        {
            return ValidChackerSearchRestaruantByName.e_msgType.e_restaurantNameIllegal;
        }
        
        else if (!(Chacker.isRestaurantExistsInDB(jedis, restNameSearchedFor)))
        {
            return ValidChackerSearchRestaruantByName.e_msgType.e_restaurantWasNotFoundInDB;
        }
        else
        {
             return ValidChackerSearchRestaruantByName.e_msgType.e_success;
        }
    }
}
