import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService
{
    @Autowired
    RedisTemplate<String,User> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    String addUser(UserRequest userRequest)
    {
        //
        User user = User.builder().userName(userRequest.getUserName()).age(userRequest.getAge()).mobNo(userRequest.getMobNo()).build();

        //Save it to the db
        userRepository.save(user);
        //Save it in the cache
        saveInCache(user);
        return "User Added successfully";

    }

    public void saveInCache(User user){

        Map map = objectMapper.convertValue(user,Map.class);
        redisTemplate.opsForHash().putAll(user.getUserName(),map);
        redisTemplate.expire(user.getUserName(), Duration.ofHours(12));

    }

    public User findUserByUserName(String userName){

        //logic
        //1. find in the redis cache
        Map map = redisTemplate.opsForHash().entries(userName);

        User user = null;
        //If not found in the redis/map
        if(map==null){

            //Find the userObject from the userRepo
            user = userRepository.findByUserName(userName);
            //Save that found user in the cache
            saveInCache(user);
            return user;
        }else{
            //We found out the User object
            user = objectMapper.convertValue(map,User.class);
            return user;

        }
    }





}
