package cn.xiaym.utils;

import java.util.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.*;
import java.nio.file.*;

import cn.xiaym.ndos.*;

import org.json.*;

public class UpdateUtil { 
  private static long local_version = -1L;
  private static xconfig x;

  private static void init() {
    x = ConfigUtil.getSection("updateutil");

    if(!ConfigUtil.hasSection("updateutil")) {
      x.newCommentLine("NDOS Update Util - Configuration File");
      x.newLine("");

      x.newCommentLine("是否使用镜像源检查更新 (默认: false)");
      x.newCommentLine("镜像源由 Vercel 提供 (链接: https://proxy-a.vercel.app/api/ )");
      x.put("use-api-mirror", "false");

      x.newLine("");

      x.newCommentLine("是否使用 GHProxy 进行更新文件下载 (默认: false)");
      x.newCommentLine("在国内建议启用，但有时可能无法连接，需要多试几次 (链接: https://ghproxy.com/ )");
      x.put("use-ghproxy", "false");

      x.save();
    }
  }

  public static void checkUpdate() {
    checkUpdate(false);
  }

  public static void checkUpdate(boolean down) {
    init();
    String apiAddr = x.get("use-api-mirror", "false").equals("true") ? "https://proxy-a.vercel.app/api" : "https://api.github.com" ;
    String ghproxyAddr = x.get("use-ghproxy", "false").equals("true") ? "https://ghproxy.com/" : "" ;

    String mainPath = new NullClass().getClass().getProtectionDomain().getCodeSource().getLocation().getPath().split("!")[0];
    if(mainPath.charAt(2) == ':') mainPath = mainPath.substring(1);
    if (down) Logger.debug("NDOS 主包路径: " + mainPath);

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
      HttpResponse<String> response = client.send(
          getSimpleRequest(apiAddr + "/repos/XIAYM-gh/Nameless-DOS/releases?per_page=1"),
          HttpResponse.BodyHandlers.ofString()
          );

      if(response.statusCode() != 200) throw new IOException("状态码错误");

      JSONObject jo = (JSONObject) new JSONArray(response.body()).get(0);
      long remote_version = Long.parseLong(jo.getString("tag_name"));

      if(remote_version > local_version) {
        if(down) {
          Logger.info("检查到更新! 正在下载...");
          Logger.info("此过程可能较慢，请耐心等待.");

          new File("download_cache").delete();
          Thread ct = new Thread(new cacheCounter());

          ct.start();

          if(download(ghproxyAddr + "https://github.com/XIAYM-gh/Nameless-DOS/releases/download/" + remote_version + "/ndos.jar")){
            Logger.success("下载成功，正在覆盖NDOS主文件..");
            new File("download_cache").renameTo(new File(mainPath));
            Logger.info("请重启 NDOS 以应用更新.");
          } else {
            Logger.err("下载失败，请稍后重试.");
          }

          ct.interrupt();

        } else {
          String updateTitle = "";
          Boolean commitRequestSucceed = false;
          try{
            response = client.send(
                getSimpleRequest(apiAddr + "/repos/XIAYM-gh/Nameless-DOS/commits/" + jo.getString("target_commitish")),
                HttpResponse.BodyHandlers.ofString()
                );

            if(response.statusCode() != 200) throw new Exception("状态码错误");

            updateTitle = new JSONObject(response.body()).getJSONObject("commit").getString("message");
            commitRequestSucceed = true;
          } catch(Exception e) {
            Logger.err("获取详细信息时发生错误.");
            ErrorUtil.trace(e);
          }
          Logger.info("检查到更新! 请使用 checkupdate download 命令下载或通过以下地址下载:");
          if(commitRequestSucceed) Logger.info("更新信息: " + updateTitle);
          Logger.info(ghproxyAddr + "https://github.com/XIAYM-gh/Nameless-DOS/releases/download/" + remote_version + "/ndos.jar");
        }
        return;
      } else {
        Logger.info("没有更新版本.");
        return;
      }

    } catch(JSONException e) {
      Logger.err("检查更新失败: JSON解析错误，请检查您的网络。");
    } catch(IOException e) {
      Logger.err("检查更新失败: 无法连接到 Github API 服务器.");
      Logger.err("错误详情: " + e.getMessage());
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

  private static boolean download(String url) {
    Logger.debug("任务开始下载: " + url);
    
    try {
      URLConnection uc = new URL(url).openConnection();
      Logger.info("资源大小: " + (uc.getContentLengthLong() > 0L ? uc.getContentLengthLong() / 1024 : "未知") + " KB");
      InputStream in = uc.getInputStream();
      Files.copy(in, Paths.get("download_cache"), StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch(Exception e) {
      Logger.err("下载失败: " + e.getMessage());
    }
    return false;
  }

  private static class cacheCounter implements Runnable {
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        File f = new File("download_cache");
        if(f.exists() && f.length() > 0) {

          Logger.info("更新已下载: " + f.length() / 1024 + " KB");

          try {
            Thread.sleep(500);
          } catch(Exception e) {}

        }
      }
    }
  }
}
