/*-  Finalized and working 27/11/13 */
package ValidalityCheckAddRemoveFriend;

import redis.clients.jedis.Jedis;

public class ValidChackerAddRemoveFriend 
{
    Jedis jedis;
    String friendName;
    String userId;
    
    public ValidChackerAddRemoveFriend (Jedis jedis, String friendName, String uid)
    {
        this.jedis = jedis;
        this.friendName = friendName;
        this.userId = uid;
    }
    
    public static enum e_msgType
    {
        e_FieldIsEmpty,
        e_FriendNotExist,
        e_UserIsYourFriend,
        e_canNotAddOrRemoveYourselveAsFriend,
        e_success;
    }
    
    public ValidChackerAddRemoveFriend.e_msgType doPost()
    {
        if (Chacker.isFieldEmpty(friendName))
        {
            return ValidChackerAddRemoveFriend.e_msgType.e_FieldIsEmpty;
        }
        else if (Chacker.isFriendAskedForIsMyself(friendName, jedis, userId))
        {
            return ValidChackerAddRemoveFriend.e_msgType.e_canNotAddOrRemoveYourselveAsFriend;
        }
        else if (!Chacker.isFriendExists(friendName, jedis))
        {
            return ValidChackerAddRemoveFriend.e_msgType.e_FriendNotExist;
        }
        else if (Chacker.isUserFriends(friendName, jedis, userId))
        {
            return ValidChackerAddRemoveFriend.e_msgType.e_UserIsYourFriend;
        }
        else
        {
            return ValidChackerAddRemoveFriend.e_msgType.e_success;
        }
    }
}
