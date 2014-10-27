/*-  Finalized and working 27/11/13 */
package ValidalityCheckAddRestaurant;

import redis.clients.jedis.Jedis;

public class ValidChackerAddRestaurant
{
    Jedis jedis;
    String restName;
    String restAddress;
    String restLink;
    boolean isSalads;
    boolean isMeat;
    boolean isFish;
    String rid;
    
    public static enum e_msgType
    {
        e_SomeFieldsEmpty,
        e_NoSelectedTypeAtAll,
        e_restaurantNotExist,
        e_RestaurantInDB;
    }
        
    public ValidChackerAddRestaurant(String i_rid, String i_name,String i_addr, String i_link, String i_meat, String i_fish,String i_salads,Jedis i_jedis)
    {
        restName = i_name;
        restAddress = i_addr;
        restLink = i_link;
        
        if(!(i_meat.equals("undefined")))
        {
            isMeat = true;
        }
        
        if(!(i_fish.equals("undefined")))
        {
            isFish = true;
        }
        
        if(!(i_salads.equals("undefined")))
        {
            isSalads = true;
        }
        
        this.rid = i_rid;
        this.jedis = i_jedis;
    }
    
    public ValidChackerAddRestaurant.e_msgType doPost()
    {
        if (!(Chacker.isAllFieldFillds(restName, restAddress, restLink)))
        {
            return ValidChackerAddRestaurant.e_msgType.e_SomeFieldsEmpty;
        }
        else if (!(Chacker.isAtLeastOneTypeWasChosen(isFish,isMeat,isSalads)))
        {
            return ValidChackerAddRestaurant.e_msgType.e_NoSelectedTypeAtAll;
        }
        else if (!(Chacker.isRasturantExistsInDB(restName, jedis)))
        {
            return ValidChackerAddRestaurant.e_msgType.e_restaurantNotExist;
        }
        else
        {
            return ValidChackerAddRestaurant.e_msgType.e_RestaurantInDB;
        }
    }
    
}


