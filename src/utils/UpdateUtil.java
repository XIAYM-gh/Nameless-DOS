package cn.xiaym.utils;

import java.util.*;
import java.io.*;
import java.net.*;
import java.net.http.*;

import cn.xiaym.ndos.*;

import org.json.*;

public class UpdateUtil { 
  private static long local_version = -1L;

  public static void checkUpdate() {
    try {
      ClassLoader mainPackCL = new NullClass().getClass().getClassLoader();
      InputStream in = mainPackCL.getResourceAsStream("version.properties");
      if(in != null) {
        Properties prop = new Properties();
        prop.load(in);
        local_version = Long.parseLong(prop.getProperty("version", "-1"));
      } else {
        Logger.warn("由于您的版本未设定，所以无法进行更新检查。");
        Logger.info("请前往 https://github.com/XIAYM-gh/Nameless-DOS/releases 手动更新.");
        return;
      }

      if(local_version == -1L) {
        Logger.warn("检查更新已禁用!");
        Logger.info("请前往 https://github.com/XIAYM-gh/Nameless-DOS/releases 手动更新.");
        return;
      }
    } catch(Exception e) {
      Logger.err("更新时遇到错误.");
      ErrorUtil.trace(e);
      return;
    }

    //初始化 HttpClient
    HttpClient client = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();

    Logger.info("正在检查更新...");

    try {
      HttpResponse<String> response = client.send(getSimpleRequest("https://api.github.com/repos/XIAYM-gh/Nameless-DOS/releases?per_page=1"), HttpResponse.BodyHandlers.ofString());
      if(response.statusCode() != 200) throw new IOException();
      JSONObject jo = (JSONObject) new JSONArray(response.body()).get(0);
      long remote_version = Long.parseLong(jo.getString("tag_name"));
      if(remote_version > local_version) {
        Logger.info("检查到更新! 请通过以下地址下载:");
        Logger.info("https://github.com/XIAYM-gh/Nameless-DOS/releases/download/" + remote_version + "/ndos.jar");
        return;
      } else {
        Logger.info("没有更新版本.");
        return;
      }
    } catch(JSONException e) {
      Logger.err("检查更新失败: JSON解析错误，请检查您的网络。");
    } catch(IOException e) {
      Logger.err("检查更新失败: 无法连接到 Github API 服务器.");
    } catch(Exception e) {
      Logger.err("检查更新失败: 未知错误: " + e.toString());
      Logger.err("错误详情: " + e.getMessage());
    }
  }

  private static HttpRequest getSimpleRequest(String URL) {
    HttpRequest req = HttpRequest.newBuilder()
      .uri(URI.create(URL))
      .build();

    return req;
  }
}
