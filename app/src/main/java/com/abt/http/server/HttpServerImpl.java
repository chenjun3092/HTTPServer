package com.abt.http.server;

import android.text.TextUtils;

import com.abt.http.util.FileUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
http://192.168.0.100:8080/getFileList?dirPath=/sdcard
http://192.168.0.100:8080/getFile?fileName=/sdcard/xxx.png
*/
public class HttpServerImpl extends NanoHTTPD {

    public static final int DEFAULT_SERVER_PORT = 8080;

    private static final String REQUEST_ROOT = "/";
    private static final String REQUEST_TEST = "/test";
    private static final String REQUEST_ACTION_GET_FILE = "/getFile";
    private static final String REQUEST_ACTION_GET_FILE_LIST = "/getFileList";
    private static final String FILE_NAME = "fileName";
    private static final String DIR_PATH = "dirPath";

    public HttpServerImpl() {
        super(DEFAULT_SERVER_PORT);
    }

    @Override
    public Response serve(IHTTPSession session) {
    	String strUri = session.getUri();
    	String method = session.getMethod().name();
        Logger.d("Response serve uri = " + strUri + ", method = " + method);

        if (REQUEST_ROOT.equals(strUri)) {                          // 根目录
            return responseRootPage(session);
        } else if(REQUEST_TEST.equals(strUri)) {                    // 返回给调用端json串
        	return responseJson();
        } else if(REQUEST_ACTION_GET_FILE_LIST.equals(strUri)) {    // 获取文件列表
        	Map<String,String> params = session.getParms();
        	String dirPath = params.get(DIR_PATH);
        	if (!TextUtils.isEmpty(dirPath)) {
        		return responseFileList(session,dirPath);
        	}        	
        } else if(REQUEST_ACTION_GET_FILE.equals(strUri)) {         // 下载文件
        	Map<String,String> params = session.getParms();
        	String fileName = params.get(FILE_NAME);
        	File file = new File(fileName);
        	if (file.exists()) {
        		if (file.isDirectory()) {
        			return responseFileList(session,fileName);
        		} else {
        			return responseFileStream(session,fileName);
        		}
        	}        	
        }
        return response404(session);
    }

    private Response responseRootPage(IHTTPSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("This is for testing from http server.\n");
        builder.append("</body></html>\n");
        return NanoHTTPD.newFixedLengthResponse(builder.toString());
    }

    /**
     * 返回给调用端LOG日志文件
     */
    private Response responseFileStream(IHTTPSession session, String filePath) {
    	Logger.d("responseFileStream() ,fileName = " + filePath);
        try {
            FileInputStream fis = new FileInputStream(filePath);
            return NanoHTTPD.newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
        } catch (FileNotFoundException e) {
            Logger.d("responseFileStream FileNotFoundException :" ,e);
            return response404(session);
        }
    }

    /**
     * @param session http请求
     * @param dirPath 文件夹路径名称
     */
    private Response responseFileList(IHTTPSession session, String dirPath) {
    	Logger.d("responseFileList() , dirPath = " + dirPath);
    	List <String> fileList = FileUtils.getFilePaths(dirPath, false);
    	StringBuilder sb = new StringBuilder();
    	for(String filePath : fileList){
    		sb.append("<a href=" + REQUEST_ACTION_GET_FILE + "?fileName=" + filePath + ">" + filePath + "</a>" + "<br>");
    	}
    	return NanoHTTPD.newFixedLengthResponse(sb.toString());
    }

    /**
     * 调用的路径出错
     */
    private Response response404(IHTTPSession session) {
    	String url = session.getUri();
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");        
        builder.append("Sorry, Can't Found "+url + " !");        
        builder.append("</body></html>\n");
        return NanoHTTPD.newFixedLengthResponse(builder.toString());
    }

    /**
     * 返回给调用端json字符串
     */
    private Response responseJson() {
    	return NanoHTTPD.newFixedLengthResponse("Call success!!");
    }

}
