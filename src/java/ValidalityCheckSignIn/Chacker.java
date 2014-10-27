/*-  Finalized and working 27/11/13 */

package ValidalityCheckSignIn;

import redis.clients.jedis.Jedis;

public class Chacker
{   
    public static boolean isAllSignInFieldsFilled(String i_Name, String i_Pass, Jedis jedis)
    {
        if((i_Name.trim().isEmpty()) || (i_Pass.trim().isEmpty()))
        {
            return false;    
        }
        else
        {
            return true;
        }
    }
    
    public static boolean isUserNameExists(String i_Name, Jedis jedis)
    {
        boolean result;
        if(jedis.hexists("users_uname_uid", i_Name))
        {
            result=  true;
        }
        else
        {
            result= false;
        }
        return result;
    }
    
       
    public static boolean isPassswordMatchUserName(String i_Name, String i_Pass, Jedis jedis)
    {
        boolean result;
        if (isUserNameExists(i_Name, jedis))
        {
            String uid = jedis.hget("users_uname_uid", i_Name);
            
            String passEntered = i_Pass;
            
            if (passEntered.equals(jedis.hget("users_uid_pass", uid)))
            {
                result= true;
            }
            else
            {
                result= false;
            }
        }
        else
        {
            result= false;
        }     
        return result;
    }
}
