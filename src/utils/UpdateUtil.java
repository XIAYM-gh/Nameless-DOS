package cn.xiaym.utils;

import java.util.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.*;
import java.nio.file.*;

import cn.xiaym.ndos.*;

import static cn.xiaym.utils.LanguageUtil.Lang;

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
    if (down) Logger.debug(Lang("uu.debug.main_pack", mainPath));

    try {
      ClassLoader mainPackCL = new NullClass().getClass().getClassLoader();
      InputStream in = mainPackCL.getResourceAsStream("version.properties");
      if(in != null) {
        Properties prop = new Properties();
        prop.load(in);
        local_version = Long.parseLong(prop.getProperty("version", "-1"));
      } else {
        Logger.warn(Lang("uu.not_set"));
        Logger.info(Lang("uu.manual", "https://github.com/XIAYM-gh/Nameless-DOS/releases"));
        return;
      }

      if(local_version == -1L) {
        Logger.warn(Lang("uu.disabled"));
        Logger.info(Lang("uu.manual", "https://github.com/XIAYM-gh/Nameless-DOS/releases"));
        return;
      }
    } catch(Exception e) {
      Logger.err(Lang("uu.failed"));
      ErrorUtil.trace(e);
      return;
    }

    //初始化 HttpClient
    HttpClient client = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();

    Logger.info(Lang("uu.checking"));

    try {
      HttpResponse<String> response = client.send(
          getSimpleRequest(apiAddr + "/repos/XIAYM-gh/Nameless-DOS/releases?per_page=1"),
          HttpResponse.BodyHandlers.ofString()
          );

      if(response.statusCode() != 200) throw new IOException(Lang("uu.status_code"));

      JSONObject jo = (JSONObject) new JSONArray(response.body()).get(0);
      long remote_version = Long.parseLong(jo.getString("tag_name"));

      if(remote_version > local_version) {
        if(down) {
          Logger.info(Lang("uu.update_found_down"));
          Logger.info(Lang("uu.update_await"));

          new File("download_cache").delete();
          Thread ct = new Thread(new cacheCounter());

          ct.start();

          if(download(ghproxyAddr + "https://github.com/XIAYM-gh/Nameless-DOS/releases/download/" + remote_version + "/ndos.jar")){
            Logger.success(Lang("uu.success"));
            new File("download_cache").renameTo(new File(mainPath));
            Logger.info(Lang("uu.restart"));
          } else {
            Logger.err(Lang("uu.down_failed"));
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
            Logger.err(Lang("uu.get_failed"));
            ErrorUtil.trace(e);
          }
          Logger.info(Lang("uu.update_found"));
          if(commitRequestSucceed) Logger.info(Lang("uu.info", updateTitle));
          Logger.info(ghproxyAddr + "https://github.com/XIAYM-gh/Nameless-DOS/releases/download/" + remote_version + "/ndos.jar");
        }
        return;
      } else {
        Logger.info(Lang("uu.no_update"));
        return;
      }

    } catch(JSONException e) {
      Logger.err(Lang("uu.check_failed", Lang("uu.exception.json")));
    } catch(IOException e) {
      Logger.err(Lang("uu.check_failed", Lang("uu.exception.api")));
      Logger.err(Lang("uu.failed_detail", e.getMessage()));
    } catch(Exception e) {
      Logger.err(Lang("uu.check_failed", Lang("uu.exception.unknown", e.toString())));
      Logger.err(Lang("uu.failed_detail", e.getMessage()));
    }
  }

  private static HttpRequest getSimpleRequest(String URL) {
    HttpRequest req = HttpRequest.newBuilder()
      .uri(URI.create(URL))
      .build();

    return req;
  }

  private static boolean download(String url) {
    Logger.debug(Lang("uu.download.start", url));
    
    try {
      URLConnection uc = new URL(url).openConnection();
      Logger.info(Lang("uu.download.size", (uc.getContentLengthLong() > 0L ? uc.getContentLengthLong() / 1024 : -1)));
      InputStream in = uc.getInputStream();
      Files.copy(in, Paths.get("download_cache"), StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch(Exception e) {
      Logger.err(Lang("uu.download.failed", e.getMessage()));
    }
    return false;
  }

  private static class cacheCounter implements Runnable {
    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        File f = new File("download_cache");
        if(f.exists() && f.length() > 0) {

          Logger.info(Lang("uu.download.downloaded", f.length() / 1024));

          try {
            Thread.sleep(500);
          } catch(Exception e) {}

        }
      }
    }
  }
}
