/*-  Finalized and working 27/11/13 */
package ValidalityCheckAddRemoveFriend;

import redis.clients.jedis.Jedis;

public class Chacker 
{
    public static boolean isFieldEmpty(String field)
    {
        if(field.trim().isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean isFriendAskedForIsMyself(String friendName, Jedis jedis, String myId)
    {
        String fid = jedis.hget("users_uname_uid", friendName);
        if(fid == null)
        {
            return false;
        }
        else if (fid.compareTo(myId)==0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean isFriendExists(String friendName, Jedis jedis)
    {
        if(jedis.hexists("users_uname_uid", friendName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean isUserFriends (String friendName, Jedis jedis, String userId)
    {
        String friend_uid = jedis.hget("users_uname_uid",friendName);
        
        if (jedis.hexists("friends_of_"+userId, friend_uid))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
