package com.nowcoder.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.Feed;
import com.nowcoder.model.Message;
import com.nowcoder.model.Question;
import com.nowcoder.model.User;
import com.nowcoder.service.FeedService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import com.nowcoder.util.WendaUtil;

import org.apache.tomcat.util.buf.UEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by nowcoder on 2016/7/30.
 */
@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FeedService feedService;

    @Autowired
    UserService userService;
    
    @Autowired
    QuestionService questionService;
    
    @Autowired
    JedisAdapter jedisAdapter;
    
    @Autowired
    FollowService followService;
    //把新鲜事主要数据存起来data
    private String buildFeedData(EventModel model) {
    	Map<String, String> map = new HashMap<>();
    	User user = userService.getUser(model.getActorId());
    	if(user==null)
    		return null;
    	map.put("userId", String.valueOf(user.getId()));
    	map.put("userName", user.getName());
    	map.put("userHead", user.getHeadUrl());
    	//user对某个问题评论或者关注了会触发一个feed流
    	if(model.getType() == EventType.COMMENT ||
    			(model.getType() == EventType.FOLLOW && model.getEntityType() == EntityType.ENTITY_QUESTION)){
    		Question question = questionService.getById(model.getEntityId());
    		if(question == null){
    			return null; 
    		}
    		map.put("questionId", String.valueOf(question.getId()));
    		map.put("questionTitle", question.getTitle());
    		return JSONObject.toJSONString(map);
    	}
    	
    	
    	
    	
    	return null;
    }
    @Override
    public void doHandle(EventModel model) {
      Feed feed = new Feed();
      //方便测试，actor写成随机的
//      Random random = new Random();
//      model.setActorId(2+random.nextInt(10));
      
      feed.setCreatedDate(new Date());
      feed.setUserId(model.getActorId());
      feed.setType(model.getType().getValue());
      feed.setData(buildFeedData(model));
      if(feed.getData() == null){
    	  return;
      }
      //添加新鲜事
     // System.out.println(feed.getData());
      //System.out.println(feed.getUserId());
      feedService.addFeed(feed);
      
      //推模式 
      //得到事件触发者的全部的粉丝
      List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, model.getActorId(), Integer.MAX_VALUE);
      followers.add(0);
      //在所有粉丝的timeline中加上feed事件
      for(int follower:followers){
          String key = RedisKeyUtil.getTimelineKey(follower);
          jedisAdapter.lpush(key, String.valueOf(feed.getId()));
      }
     
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.COMMENT,EventType.FOLLOW});
    }
}
