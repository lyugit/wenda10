package com.nowcoder.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.nowcoder.model.Comment;
import com.nowcoder.model.Feed;

@Mapper
public interface FeedDAO {
	String TABLE_NAME = " feed ";
    String INSERT_FIELDS = " type, user_id, created_date, data ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;
    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
    ") values (#{type},#{userId},#{createdDate},#{data})"})
    int addFeed(Feed feed);
    //推模式下
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Feed getFeedById(int id);
    
    //增量拉模式下从关注的所有人中获取feed流 动态的sql语句，count是分页用的
    //没有登录的状态是不需要userids
    List<Feed> selectUserFeeds(@Param("maxId") int maxId,
    		 @Param("userIds") List<Integer> userIds,
             @Param("count") int count);
    
}
