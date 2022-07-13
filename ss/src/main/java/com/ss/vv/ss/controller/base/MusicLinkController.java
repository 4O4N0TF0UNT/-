package com.ss.vv.ss.controller.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ss.vv.common.WebResponse;
import com.ss.vv.ss.domain.MusicLink;
import com.ss.vv.ss.domain.User;
import com.ss.vv.ss.service.IMusicLinkService;
import com.ss.vv.ss.service.impl.MusicLinkService;



@Controller 
@RequestMapping("/musicLink")
public class MusicLinkController {

	@Autowired
	protected WebResponse webResponse;

	@Resource
	protected IMusicLinkService musicLinkService;

	@Resource
	protected MusicLinkService musicLinkService2;

	

	// 从数据库中获取歌曲数据，在榜单中显示
	@RequestMapping(value = "/getMusicLinkList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WebResponse getMusicLinkList(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			@RequestParam(defaultValue = "1", required = false) Integer pageNo,
			@RequestParam(defaultValue = "30", required = false) Integer pageSize,
			@RequestParam(defaultValue = "正常", required = false) String tbStatus,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "ml_id", required = false) String order,
			@RequestParam(defaultValue = "desc", required = false) String desc) {
		Object data = null;
		String statusMsg = "";
		int statusCode = 200;
		LinkedHashMap<String, String> condition = new LinkedHashMap<String, String>();

		if (keyword != null && keyword.length() > 0) {
			StringBuffer buf = new StringBuffer();
			buf.append("(");
			buf.append("test_name like '%").append(keyword).append("%'");
			buf.append(" or ");
			buf.append("info like '%").append(keyword).append("%'");
			buf.append(" or ");
			buf.append("other like '%").append(keyword).append("%'");
			buf.append(")");
			condition.put(buf.toString(), "and");
		}
		String field = null;
		if (condition.size() > 0) {
			condition.put(condition.entrySet().iterator().next().getKey(), "");
		}
		int count = this.musicLinkService.getCount(condition, field);
		if (order != null && order.length() > 0 & "desc".equals(desc)) {
			order = order + " desc";
		}
		// 从数据库中获取数据，并把对象的结果集存到list列表中
		List<MusicLink> list = this.musicLinkService.getList(condition, pageNo, pageSize, order, field);
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("total", count);

		int size = list.size();
		if (size > 0) {
			List<MusicLink> listFont = new ArrayList<MusicLink>();
			MusicLink vo;
			MusicLink voFont = new MusicLink();

			// 循环将获取到的对象及结构属性克隆到一个新的voFont对象中，并保存到动态数组中
			for (int i = 0; i < size; i++) {
				vo = list.get(i);

				// 通过java反射将类中当前属性字段对应的内容复制到另外一个类中
				BeanUtils.copyProperties(vo, voFont);
				listFont.add(voFont);
				voFont = new MusicLink();
			}
			map.put("list", listFont);

			// data是一个map对象
			data = map;
			statusMsg = "根据条件获取分页数据成功！！！";
		} else {
			map.put("list", list);
			data = map;
			statusCode = 202;
			statusMsg = "no record!!!";
			return webResponse.getWebResponse(statusCode, statusMsg, data);
		}
		return webResponse.getWebResponse(statusCode, statusMsg, data);
	}

	// 歌曲搜索功能
	@RequestMapping(value = "/getSongRearch", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WebResponse getSongRearch(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			@RequestParam(required = false) String songName) {

		Object data = null;
		String statusMsg = "";
		int statusCode = 200;

		// 调用Mapper层的songRearch方法，进行数据库的操作
		List<MusicLink> list = this.musicLinkService2.songRearch(songName);

		int count = list.size();
		System.out.println();
		System.out.println("搜索到的歌曲数：" + count);
		System.out.println("结束");
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("total", count);
		int size = list.size();
		if (size > 0) {
			List<MusicLink> listFont = new ArrayList<MusicLink>();
			MusicLink vo;
			MusicLink voFont = new MusicLink();
			for (int i = 0; i < size; i++) {
				vo = list.get(i);
				BeanUtils.copyProperties(vo, voFont);
				listFont.add(voFont);
				voFont = new MusicLink();
			}
			map.put("list", listFont);
			data = map;
//
			statusMsg = "根据条件获取分页数据成功！！！";
		} else {
			map.put("list", list);
			data = map;
			statusCode = 202;
			statusMsg = "no record!!!";
			return webResponse.getWebResponse(statusCode, statusMsg, data);
		}

		return webResponse.getWebResponse(statusCode, statusMsg, data);
	}

	// 歌曲收藏
	@RequestMapping(value = "/addMusicCollect", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WebResponse addMusicCollect(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			@RequestParam(required = false) int song_id, @RequestParam(required = false) String user_name,
			@RequestParam(required = false) String user_password, @RequestParam(required = false) String songName) {
		WebResponse webResponse = new WebResponse();
		MusicLink musicLink = new MusicLink();
		User user = new User();
		System.out.println("歌曲id：" + song_id + " 用户名：" + user_name + "  户用密码：" + user_password);
		Object data = null;
		String statusMsg = "";
		int statusCode = 200;

		String user_Id = null;
		try {
			user_Id = musicLinkService2.getUserId(user_name, user_password);
		} catch (Exception e) {

		}

		int userId = 0;
		try {

			boolean NotisNum = (user_Id.equals("null"));

			if (!NotisNum) {
				userId = Integer.parseInt(user_Id);
			}
		} catch (Exception e) {

		}

		System.out.println("歌曲名：" + songName);

		String jSong = this.musicLinkService.judgeSong(songName, userId);

		int my_Id = 0;
		try {

			boolean NotisNum = (jSong.equals("null"));

			if (!NotisNum) {
				my_Id = Integer.parseInt(jSong);
			}
		} catch (Exception e) {

		}

		if (my_Id > 0) {
			statusCode = 201;
			statusMsg = "已收藏,请不要重复收藏！";
		} else {
			this.musicLinkService.insertSongRearch(song_id, userId);
		}

		System.out.println("收藏歌曲的用户id：" + userId);

		return webResponse.getWebResponse(statusCode, statusMsg, data);

	}

}
