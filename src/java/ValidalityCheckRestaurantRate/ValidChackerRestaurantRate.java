/*Finalized and working 27/11/13*/
package ValidalityCheckRestaurantRate;
import redis.clients.jedis.Jedis;

public class ValidChackerRestaurantRate 
{
    final int MaxAllowed = 5;
    Jedis jedis;
    String restaurnName;
    String gradeGivenByUser;
    String idOfUserRatedTheRestaurant;
    
    public static enum e_msgType
    {
        e_IllegalRateFromUser,
        e_RestaurantNotInDB,
        e_success;
    }
    
    public ValidChackerRestaurantRate (Jedis jedis, String restName, String grade, String uid)
    {
        this.jedis = jedis;
        this.gradeGivenByUser = grade;
        this.restaurnName = restName;
        this.idOfUserRatedTheRestaurant = uid;
    }
    
    public ValidChackerRestaurantRate.e_msgType doPost()
    {
        if (!Chacker.isRateIsLegal(jedis,gradeGivenByUser, MaxAllowed))
        {
            return ValidChackerRestaurantRate.e_msgType.e_IllegalRateFromUser;
        }
        else if (!Chacker.isRestaurantExistsInDB(jedis, restaurnName))
        {
            return ValidChackerRestaurantRate.e_msgType.e_RestaurantNotInDB;
        }
        else
        {
            return ValidChackerRestaurantRate.e_msgType.e_success;
        }
    }
}
