package cn.edu.scujcc.api;

import java.util.List;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.scujcc.model.Channel;
import cn.edu.scujcc.model.Comment;
import cn.edu.scujcc.model.Result;
import cn.edu.scujcc.service.ChannelService;
import cn.edu.scujcc.service.UserService;

@RestController
@RequestMapping("/channel")
public class ChannelController {
	private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

	@Autowired
	private UserService userService;
	@Autowired
	private ChannelService service;
	@Autowired
	private CacheManager cacheManager;
	
	
	@GetMapping
	/*
	public List<Channel> getAllChannels(@RequestHeader("token") String token){
		logger.info("正在查找所有频道信息,token="+token);
		String user = userService.currentUser(token);
		logger.info("当前用户是："+ user);
		List<Channel> results = service.getAllChannels();
		logger.debug("所有频道的数量是"+results.size());
		return results;		
	}
	*/
	public Result<List<Channel>> getAllChannels() {
		logger.info("正在读取所有频道信息!!!");
		Result<List<Channel>> result = new Result<List<Channel>>();
		List<Channel> channels = service.getAllChannels();
		result = result.ok();
		result.setData(channels);
		logger.debug("所有频道的数量是"+channels.size());
		return result;
	}
	
	/**
	 * 读取频道前必须先登录
	 * @param id
	 * @param token
	 * @return
	 */
	@GetMapping("/{id}")
	/*
	public Channel getChannel(@PathVariable String id,@RequestHeader("token") String token) {
		logger.info("正在读取频道"+token);
		String user = userService.currentUser(token);
		logger.debug("当前已登录用户是："+ user);
		Channel c = service.getChannel(id);
		if (c != null) {
			return c;
		} else {
			logger.error("找不到指定的频道");
			return null;
		}	
	}
	*/
	public Result<Channel> getChannel(@PathVariable String id) {
		logger.info("正在读取频道："+id);
		Result<Channel> result = new Result<>();
		Channel c = service.getChannel(id);
		if (c != null) {
			result = result.ok();
			result.setData(c);
		} else {
			logger.error("找不到指定的频道。");
			result = result.error();
			result.setMessage("找不到指定的频道。");
		}
		return result;
	}
	
	
	
	
	/**
	 * 删除频道
	 * @param id
	 * @return
	 */
	@DeleteMapping("/{id}")
	/*public ResponseEntity<String> deleteChannel(@PathVariable String id){
		logger.debug("即将删除频道, id=" +id);
		boolean result = service.deleteChannel(id);
		if(result) {
			return ResponseEntity.ok().body("删除成功");
		}else {
			return ResponseEntity.ok().body("删除失败");
		}	
	}
	*/
	public Result<Channel> deleteChannel(@PathVariable String id) {
		logger.info("即将删除频道，id="+id);
		Result<Channel> result = new Result<>();
		boolean del = service.deleteChannel(id);
		if (del) {
			result = result.ok();
		} else {
			result.setStatus(Result.STATUS_ERROR);
			result.setMessage("删除失败");
		}
		return result;
	}
	
	
	@PostMapping
	/*public Channel createChannel(@RequestBody Channel c) {
		logger.debug("即将新建频道，频道数据："+ c);
		Channel saved = service.createChannel(c);
		return saved;
	}
	*/
	public Result<Channel> createChannel(@RequestBody Channel c) {
		logger.info("即将新建频道，频道数据：" + c);
		Result<Channel> result = new Result<>();
		Channel saved= service.createChannel(c);
		//result = result.ok();
		result.setData(saved);
		return result;
	}
	
	@PutMapping
	/*
	public Channel updateChannel(@RequestBody Channel c) {
		logger.debug("即将更新频道，频道数据："+ c);
		Channel updated = service.updateChannel(c);
		return updated;
	}
	*/
	public Result<Channel> updateChannel(@RequestBody Channel c) {
		logger.debug("即将更新频道：频道数据：" + c);
		Result<Channel> result = new Result<>();
		Channel updated = service.updateChannel(c);
		result = result.ok();
		result.setData(updated);
		return result;
	}
	
	@GetMapping("/t/{title}")
	public List<Channel> searchTitle(@PathVariable String title){
		return service.searchTitle(title);
	}

	
	
	@GetMapping("/q/{quality}")
	public List<Channel> searchQuality(@PathVariable String quality){
		return service.searchQuality(quality);
	}
	
	@GetMapping("/hot")
	public List<Channel> getHotChannels(){
		return service.getLatesCommentsChannel();
	}
	/**
	 * 新增评论
	 * chanelId 被评论的频道编号
	 * comment 将要新增的评论对象	
	 */
	@PostMapping("/{channelId}/comment")
	public Channel addComment(@RequestHeader("token") String token, @PathVariable String channelId,@RequestBody Comment comment) {
		Channel result = null;
		String username = userService.currentUser(token);
		comment.setAuthor(username);
		logger.debug(username + "即将评论频道" + channelId+ "评论对象：" + comment);
		//把评论保存到数据库
		result = service.addComment(channelId, comment);
		return result;
	}
	
	/**
	 *  获取指定评论的热门评论（前3条）
	 * @param channelId 制定的频道编号
	 * @return 3条热门评论的列表（数组）
	 */
	@GetMapping("/{channelId}/hotcomments")
	/*
	public List<Comment> hotComments(@PathVariable String channelId ){
		logger.debug("将获取频道"+channelId+"的热门评论...");
		//TODO 从数据库
		return service.hotComments(channelId);
	}
	*/
	public Result<List<Comment>> hotComments(@PathVariable String channelId) {
		Result<List<Comment>> result = new Result<List<Comment>>();
		logger.debug("获取频道"+channelId+"的热门评论...");
		result = result.ok();
		result.setData(service.hotComments(channelId));
		return result;
	}
	
}