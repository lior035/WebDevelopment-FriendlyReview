/*-  Finalized and working 27/11/13 */

package ValidalityCheckSignUp;

import java.util.List;
import redis.clients.jedis.Jedis;

public class Chacker 
{
    public static boolean isAllSignUpFieldsFilled(String i_name, String i_email,String i_pass,String i_rePass, Jedis jedis)
    {
        boolean result;
        if ((i_name.trim().isEmpty()) || (i_email.trim().isEmpty()) 
            || (i_pass.trim().isEmpty()) || (i_rePass.trim().isEmpty()))
        {
            result= false;
        }
        
        else
        {
            result = true;
        }
        return result;
    }
    
       
    public static boolean isPasswordsMatch(String i_Pass, String i_RePass)
    {
        boolean result;
        
        if (!(i_Pass.equals(i_RePass)))
        {
            result =  false;
        }
        
        else
        {
            result = true;
        }
        return result;
    }
    
    public static boolean isUserNameAlreadyTaken(String i_UserName, Jedis jedis)
    {
        boolean result;
        if (jedis.hexists("users_uname_uid",i_UserName))
        {
            result = true;
        }
        
        else
        {
            result= false;
        }
        return result;
    }
    
     
    public static boolean isEmailAlreadyTaken(String i_Email, Jedis jedis)
    {
        List <String> email_List = jedis.hvals("users_uid_uemail");
        
        for (String str : email_List) 
        {  
            if (str.equals(i_Email))
            {
                return true;
            }
        }
        
        return false;
    }
    
}
