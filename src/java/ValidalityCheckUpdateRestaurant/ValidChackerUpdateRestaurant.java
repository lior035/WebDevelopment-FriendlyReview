//update by lior asulin 27/11/13

package ValidalityCheckUpdateRestaurant;

import redis.clients.jedis.Jedis;

public class ValidChackerUpdateRestaurant 
{
        Jedis jedis;  
        String inputRest;
        
        public enum e_msgType
        {
            e_NotInDB,
            e_EmptyInput,
            e_success,
        }
        
        public ValidChackerUpdateRestaurant(Jedis jedis, String rName)
        {
            this.jedis = jedis;
            inputRest = rName;
        }
        
        public ValidChackerUpdateRestaurant.e_msgType doPost()
        {
            if(Chacker.isEmptyInput(inputRest))
            {
                return ValidChackerUpdateRestaurant.e_msgType.e_EmptyInput;
            }
            else if (!(Chacker.isInDB(inputRest, jedis)))
            {
                return ValidChackerUpdateRestaurant.e_msgType.e_NotInDB;
            }
            else
            {
                return ValidChackerUpdateRestaurant.e_msgType.e_success;
            }
        }
}
