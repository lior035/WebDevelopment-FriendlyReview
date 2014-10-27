/*-  Finalized and working 27/11/13 */

package ValidalityCheckSignUp;

import redis.clients.jedis.Jedis;

public class ValidChackerSignUp 
{
    Jedis jedis;  
    String m_name;
    String m_pass;
    String m_rePass;
    String m_email;
    
      
    public enum e_msgType
    {
        e_errorPasswordsNotMatch,
        e_errorEmailAlreadyExists,
        e_errorUserNameAlreadyExists,
        e_errorNotAllSignUpFieldsFilled,
        e_success,
    }
      
    public ValidChackerSignUp (String i_name, String i_pass, String i_email, String i_rePass, Jedis jedis)
    {
        m_email= i_email;
        m_name = i_name;
        m_pass = i_pass;
        m_rePass= i_rePass;
        this.jedis = jedis;
    }
    
    public ValidChackerSignUp.e_msgType doPost()
    {
        if (!Chacker.isAllSignUpFieldsFilled(m_name, m_email,m_pass,m_rePass, jedis))
        {
            return ValidChackerSignUp.e_msgType.e_errorNotAllSignUpFieldsFilled;
        }
        else if (!Chacker.isPasswordsMatch(m_pass,m_rePass))
        {
            return ValidChackerSignUp.e_msgType.e_errorPasswordsNotMatch;
        }
        else if (Chacker.isUserNameAlreadyTaken(m_name, jedis))
        {
            return ValidChackerSignUp.e_msgType.e_errorUserNameAlreadyExists;
        }
        else if (Chacker.isEmailAlreadyTaken(m_email, jedis))
        {
            return ValidChackerSignUp.e_msgType.e_errorEmailAlreadyExists;
        }
        else
        {
            return ValidChackerSignUp.e_msgType.e_success;
        }
    } 
}
