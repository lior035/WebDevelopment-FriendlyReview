/*This version is up to date by lior asulin 25/11/13*/
package UtitlityClasses;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import redis.clients.jedis.Jedis;

public class RateCalculator
{   
    final int FavWeightVsRegWeight = 2;
    public class NodeInformation
    {
        int score;
        boolean favoriteOrRegular;
        
        public NodeInformation(int score, boolean favFriendStatus)
        {
            this.score = score;
            this.favoriteOrRegular = favFriendStatus;
        }
    }
    
    public double calculate (String userId, Jedis jedis, String rid)
    {
        int countFav = 0;
        
        Set<String> favorite = new HashSet<String>();
        Set<String> friendId = new HashSet<String>();
        List<RateCalculator.NodeInformation> info = new LinkedList<RateCalculator.NodeInformation>();
        
        friendId.addAll(jedis.hkeys("friends_of_"+userId));
        favorite.addAll(jedis.hvals("friends_of_"+userId));
        
       Iterator iterId = friendId.iterator();
       Iterator iterFav = favorite.iterator();
     
       try
       {
            while (iterId.hasNext() && iterFav.hasNext())
            {
                String fId = iterId.next().toString();

                 if(jedis.hexists("ranked_restaurants_of_"+fId+"_score", rid))
                 {
                     int currScore =  Integer.parseInt(jedis.hget("ranked_restaurants_of_"+fId+"_score", rid));
                     int isFavorite = Integer.parseInt(jedis.hget("friends_of_"+userId ,fId));
                     boolean favotire;

                    if (isFavorite == 0)
                    {
                        favotire =  false;
                    }
                    else
                    {
                        countFav++;
                        favotire = true;
                    }

                    info.add(new RateCalculator.NodeInformation(currScore,favotire));
                 }
                 
            }

         return calculateUsingInformation(info, countFav);
       }
    
        catch(NumberFormatException nfe)
        {
            return 0;
        }
   }
    
    private double calculateUsingInformation(List<RateCalculator.NodeInformation> info, int numOfFav)
    {
        double score = 0;
        
        if(info == null)
        {
            return score;
        }
        else
        {
            int numOfAll = info.size();
            double weightOfRegularScore;
            double weightOfFavScore;
        
            double total = (numOfAll-numOfFav)+(numOfFav*FavWeightVsRegWeight);
            weightOfRegularScore = (double)100/total;
            weightOfFavScore = weightOfRegularScore*FavWeightVsRegWeight;

            for(RateCalculator.NodeInformation information:info)
            {
                double currScore = information.score;
                if (information.favoriteOrRegular)
                {
                    currScore =(currScore*weightOfFavScore)/100;
                }
                else
                {
                    currScore =(currScore*weightOfRegularScore)/100;
                }
                score+=currScore;
            }
        
            return score;
        }
    }
}
