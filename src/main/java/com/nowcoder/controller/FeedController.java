package com.nowcoder.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nowcoder.model.EntityType;
import com.nowcoder.model.Feed;
import com.nowcoder.model.HostHolder;
import com.nowcoder.service.FeedService;
import com.nowcoder.service.FollowService;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;

@Controller
public class FeedController {
	@Autowired
	HostHolder hostHolder;
	@Autowired
	FollowService followService;
	@Autowired
	FeedService feedService;
	@Autowired
	JedisAdapter jedisAdapter;
	//拉模式
	@RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET})
	 private String getPullFeeds(Model model) {
		int localUserId = hostHolder.getUser() ==null? 0: hostHolder.getUser().getId();
		List<Integer> followees =  new ArrayList<>();
		//当前用户已经登录
		if(localUserId!= 0){
			//获取当前用户关注的人
			 followees =  followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
		   System.out.println(followees.size());
		}
		List<Feed> feeds = feedService.selectUserFeeds(Integer.MAX_VALUE, followees, Integer.MAX_VALUE);
		model.addAttribute("feeds", feeds);
		return "feeds";
		
	}
	
	//推模式
		@RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET})
		 private String getPushFeeds(Model model) {
			int localUserId = hostHolder.getUser() ==null? 0: hostHolder.getUser().getId();
			//当前用户已经登录
			List<Feed> feeds = new ArrayList<>();
			List<String> feedIds = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId), 0, 100);
			//拿到当前登录用户timeline中的所有的新鲜事
			for(String feedId:feedIds){
				 Feed feed =feedService.getFeedById(Integer.parseInt(feedId));
				 if(feed != null){
					 feeds.add(feed);
				 }
			}
			
			model.addAttribute("feeds", feeds);
			return "feeds";
			
		}
}
