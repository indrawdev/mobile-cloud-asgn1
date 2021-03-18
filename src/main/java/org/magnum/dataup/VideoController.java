package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Response;
import retrofit.mime.TypedFile;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.magnum.dataup.VideoFileManager;

@Controller
public class VideoController {

	public static final String VIDEO_SVC_PATH = "/video";
	public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";

	private static final AtomicLong currentId = new AtomicLong(0L);

	private Map<Long, Video> videos = new HashMap<Long, Video>();
	
	private VideoFileManager videoDataMgr;

	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos.values();
	}

//	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
//	public Video addVideo(Video v) {
//		checkAndSetId(v);
//		v.setDataUrl(getDataUrl(v.getId()));
//		videos.put(v.getId(), v);
//		return v;
//	}
	
	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
	@ResponseBody
	public VideoStatus setVideoData(@PathVariable("id") Long id, @RequestParam MultipartFile data) throws IOException {
		try {
			videoDataMgr.saveVideoData(videos.get(id), data.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new VideoStatus(VideoState.READY);
	}

	@RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.GET)
	public void getData(@PathVariable("id") Long id, HttpServletResponse response) throws IOException {

		try {
			videoDataMgr.copyVideoData(videos.get(id), response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getUrlBaseForLocalServer(HttpServletRequest request) {
		String baseUrl = "http://" + request.getServerName()
				+ ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
		return baseUrl;
	}

	private String getDataUrl(long videoId) {
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}

	private String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String base = "http://" + request.getServerName()
				+ ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
		return base;
	}

	private void checkAndSetId(Video entity) {
		if (entity.getId() == 0) {
			entity.setId(currentId.incrementAndGet());
		}
	}


}
