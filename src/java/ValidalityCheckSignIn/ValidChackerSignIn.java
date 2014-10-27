/*-  Finalized and working 27/11/13 */

package ValidalityCheckSignIn;

import javax.servlet.http.HttpServletRequest;
import redis.clients.jedis.Jedis;

public class ValidChackerSignIn 
{
    Jedis jedis;
    String m_Name;
    String m_Pass;
    
    public enum e_msgType
    {
        e_errorNotAllSignInFieldsFilled,
        e_userNotExists,
        e_errorPasswordDontMatchUserName,
        e_success;
    }
    
    public ValidChackerSignIn (String i_Name, String i_Pass, Jedis jedis)
    {
        this.jedis = jedis;
        m_Name = i_Name;
        m_Pass = i_Pass;
    }

     public ValidChackerSignIn.e_msgType doPost()
     {
         if(!Chacker.isAllSignInFieldsFilled(m_Name,m_Pass, jedis))
         {
             return ValidChackerSignIn.e_msgType.e_errorNotAllSignInFieldsFilled;
         }
         else if(!Chacker.isUserNameExists(m_Name, jedis))
         {
             return ValidChackerSignIn.e_msgType.e_userNotExists;
         }
         else if(!Chacker.isPassswordMatchUserName(m_Name, m_Pass, jedis))
         {
             return ValidChackerSignIn.e_msgType.e_errorPasswordDontMatchUserName;
         }
         else
         {
             return ValidChackerSignIn.e_msgType.e_success;
         }
     }
}