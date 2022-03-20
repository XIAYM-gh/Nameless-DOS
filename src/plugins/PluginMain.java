package cn.xiaym.ndos.plugins;

import java.io.*;
import java.util.*;
import java.net.*;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.command.*;

public class PluginMain {
  private static ArrayList<JavaPlugin> Plugins = new ArrayList<>();
  private static ClassLoader cl = new NullClass().getClass().getClassLoader();
  private static boolean inited = false;

  public static void init(){
    if(inited) return;
    //加载内置组件包
    try{
      InputStream in = cl.getResourceAsStream("ndosplugin/plugin_meta");
      if(in != null){
        JavaPlugin pl = initPlugin(in, cl, "Built-In");
        Plugins.add(pl);
      } else {
        Logger.warn("无法加载内置组件，请重新下载NDOS");
      }
    } catch(Exception e) {}

    File plugindir = new File("plugins/");
    if(!plugindir.exists()){
      plugindir.mkdir();
    }

    File[] pluginsFile = plugindir.listFiles();

    List<File> pluginJars = Collections.synchronizedList(new ArrayList<File>());

    for (File f : pluginsFile) {
      if(f.getName().endsWith(".jar")){
        pluginJars.add(f);
      }
    }

    int fl_size = pluginJars.size();

    for (int i=1;i<=fl_size;i++) {
      try{
        URLClassLoader u = new URLClassLoader(new URL[]{ pluginJars.get(i-1).toURI().toURL() });
        InputStream is = u.getResourceAsStream("plugin_meta");
        if(is != null){
          JavaPlugin plugin = initPlugin(is,u,pluginJars.get(i-1).getName());
          if(plugin != null){
            Plugins.add(plugin);
            try{
              Logger.info("("+i+"/"+fl_size+") " + plugin.getName());
              plugin.onEnable();
            } catch(Exception e_PluginOnEnable) {
              Logger.err("无法执行插件的onEnable方法.");
              ErrorUtil.trace(e_PluginOnEnable);
            }
          }
        } else {
          Logger.info("("+i+"/"+fl_size+") " + pluginJars.get(i-1).getName());
          Logger.err("无法在 "+pluginJars.get(i-1).getName()+" 中找到 /plugin_meta 文件!");
        }
      } catch(Exception e) {
        Logger.err("插件加载时出现错误!");
        ErrorUtil.trace(e);
      }
    }

    inited = true;
  }

  private static JavaPlugin initPlugin(InputStream is, ClassLoader u, String fileName) {
    try{
      String randId = RandomIDGenerator.generate(16);
      Properties pc = new Properties();
      pc.load(new InputStreamReader(is, "UTF-8"));
      Class<?> clazz = u.loadClass(pc.getProperty("plugin.main-class"));
      JavaPlugin p = (JavaPlugin) clazz.getDeclaredConstructor().newInstance();
      p.setName(pc.getProperty("plugin.name", fileName));
      p.setVersion(pc.getProperty("plugin.version", "1.0.0"));
      p.setAuthor(pc.getProperty("plugin.author", "Nameless"));
      p.setDesc(pc.getProperty("plugin.desc", "无描述"));
      if (pc.getProperty("plugin.id") == null) Logger.warn("插件 \"" + p.getName() + "\" 的ID为空，正在使用随机id: " + randId);
      p.setID(pc.getProperty("plugin.id", randId));

      NDOSCommand.processPlugin(pc, p.getID());
      return p;
    } catch(ClassNotFoundException|NoSuchMethodException e) {
      Logger.err("无法找到插件主类，请检查插件的配置文件!");
      return null;
    } catch(Exception e) {
      Logger.err("构造插件失败: "+e.toString());
      return null;
    }
  }

  public static ArrayList<JavaPlugin> getPlugins(){
    return Plugins;
  }

  public static JavaPlugin getPlugin(String id){
    for(JavaPlugin p:Plugins){
      if(p.getID().equals(id)) return p;
    }

    return null;
  }

}
