package com.project.majorproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.majorproject.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService
{
    @Autowired
    RedisTemplate<String, User> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;


    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    String addUser(UserRequest userRequest)
    {
        //
        User user = User.builder().userName(userRequest.getUserName()).age(userRequest.getAge()).mobNo(userRequest.getMobNo()).build();


        //Save it to the db
        userRepository.save(user);
        //Save it in the cache
        saveInCache(user);


        //Send an update to the wallet module/ wallet service ---> that create a new wallet from the userName sent as a string.
        kafkaTemplate.send("create_wallet",user.getUserName());



        return "User Added successfully";

    }

    public void saveInCache(User user){

        Map map = objectMapper.convertValue(user,Map.class);

        String key = "USER_KEY"+user.getUserName();
        System.out.println("The user key is "+key);
        redisTemplate.opsForHash().putAll(key,map);
        redisTemplate.expire(key, Duration.ofHours(12));
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
            user = objectMapper.convertValue(map, User.class);
            return user;

        }
    }





}
