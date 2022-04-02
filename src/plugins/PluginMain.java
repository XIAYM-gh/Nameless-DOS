package cn.xiaym.ndos.plugins;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.net.*;
import java.lang.reflect.*;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.command.*;

import org.json.*;

public class PluginMain {
  private static ArrayList<JavaPlugin> Plugins = new ArrayList<>();
  private static ArrayList<SimpleClassLoader> Loaders = new ArrayList<>();
  private static HashMap<String, Class<?>> cachedClasses = new HashMap<>();
  private static HashMap<String, Integer> undefinedClassFindCount = new HashMap<>();
  private static ClassLoader cl = new NullClass().getClass().getClassLoader();
  private static boolean inited = false;

  public static void init(Boolean BeSilent){
    if(inited) return;

    long bef = System.currentTimeMillis();

    //加载内置组件包
    try{
      InputStream in = cl.getResourceAsStream("ndosplugin/plugin_meta");
      if(in != null){
        JavaPlugin pl = initPlugin(in, new File("Built-In"));
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

    //先加载所有插件类到内存(解决 API插件 问题)
    for (File f : pluginJars) {
      try{
        JarFile jar = new JarFile(f);
        JarEntry entry = jar.getJarEntry("plugin_meta");
        if(entry != null){
          InputStream is = jar.getInputStream(entry);
          JavaPlugin p = initPlugin(is, f);
          Plugins.add(p);
        } else {
          Logger.err("分析失败: 插件文件 " + f.getName() + " 中不含有 plugin_meta 文件!");
        }
      } catch(Exception e) {
        Logger.err("加载插件文件 " + f.getName() + " 时出现问题!");
        ErrorUtil.trace(e);
      }
    }

    Logger.debug("插件初始化完成!");

    //再逐个执行 onEnable 方法
    //并检查依赖
    int co = 0;
    ArrayList<JavaPlugin> rl = new ArrayList<>();
    ArrayList<JavaPlugin> pBlackList = new ArrayList<>();

    for (JavaPlugin plugin : Plugins) {
      co++;
      Logger.info("(" + co + "/" + Plugins.size() + ") " + plugin.getName() + " v" + plugin.getVersion());

      for (int i=0; i<plugin.getDepends().length(); i++) {
        if(!Plugins.contains(plugin) || pBlackList.contains(plugin)) break;
        String did = String.valueOf(plugin.getDepends().get(i));
        if(!did.equals("")) {
          if(getPlugin(did) == null) {
            Logger.warn("插件 " + plugin.getName() + " 需要的依赖插件不存在: " + did + " ，正在禁用.");
            try {
              ArrayList<SimpleClassLoader> removingList = new ArrayList<>();

              for(SimpleClassLoader l:Loaders) {
                if(l.getDeclaredPlugin().equals(plugin)) {
                  //Loaders.remove(l);
                  removingList.add(l);
                }
              }

              for(SimpleClassLoader l:removingList) {
                Loaders.remove(l);
              }

              rl.add(plugin);
              pBlackList.add(plugin);
              NDOSCommand.removeByPlugin(plugin);

              plugin.onDisable();
            } catch(Exception e) {
              ErrorUtil.trace(e);
            }
          }
        }
      }

      try{
        if(!pBlackList.contains(plugin)) plugin.onEnable();
      } catch(Exception e) {
        Logger.err("无法执行插件" + plugin.getName() + "的onEnable方法!");
        ErrorUtil.trace(e);
      }
    }

    for(JavaPlugin p:rl){
      Plugins.remove(p);
    }

    inited = true;
    Logger.info("插件加载完成，耗时 " + (System.currentTimeMillis() - bef) + " ms");
  }

  private static JavaPlugin initPlugin(InputStream is, File file) {
    try{
      String randId = RandomIDGenerator.generate(16);

      Properties pc = new Properties();
      pc.load(new InputStreamReader(is, "UTF-8"));

      SimpleClassLoader scl = new SimpleClassLoader(file, pc.getProperty("plugin.main-class"), cl);
      JavaPlugin p = scl.getDeclaredPlugin();

      p.setName(pc.getProperty("plugin.name", file.getName()));
      p.setVersion(pc.getProperty("plugin.version", "1.0.0"));
      p.setAuthor(pc.getProperty("plugin.author", "Nameless"));
      p.setDesc(pc.getProperty("plugin.desc", "无描述"));
      p.setDepends(new JSONArray(pc.getProperty("plugin.depends", "[]")));
      if (pc.getProperty("plugin.id") == null) Logger.warn("插件 \"" + p.getName() + "\" 的ID为空，正在使用随机id: " + randId);
      p.setID(pc.getProperty("plugin.id", randId));

      NDOSCommand.processPlugin(pc, p.getID());
      return p;
    } catch(ClassNotFoundException e) {
      Logger.err(e.getMessage());
      return null;
    } catch(Exception e) {
      Logger.err("构造插件失败: "+e.toString());
      ErrorUtil.trace(e);
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

  public static void addLoader(SimpleClassLoader lo){
    Loaders.add(lo);
  }

  public static void clearCache(){
    cachedClasses.clear();
    undefinedClassFindCount.clear();
  }

  /* 解释:
   * 当调用时，自动缓存这个class
   * 如果class不存在，则增加一次寻找次数
   * 为提高寻找效率，如果寻找次数到达30, 那将会直接返回null
   */
  public static Class<?> getClass(String name){
    if(cachedClasses.containsKey(name) && cachedClasses.get(name) != null) return cachedClasses.get(name);
    if(undefinedClassFindCount.containsKey(name) && undefinedClassFindCount.getOrDefault(name, 0) >= 30) return null;

    for(SimpleClassLoader l:Loaders){
      try{
        Class<?> result = l.findClass(name, false);
        cachedClasses.put(name, result);
        return result;
      } catch(Exception e) {}
    }

    cachedClasses.put(name, null);
    undefinedClassFindCount.put(name, undefinedClassFindCount.getOrDefault(name, 0) + 1);
    return null;
  }

  private static class SimpleClassLoader extends URLClassLoader {
    private File file;
    private JavaPlugin plugin;
    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    
    public SimpleClassLoader(File file, String main, ClassLoader parent) throws ClassNotFoundException, MalformedURLException, InvocationTargetException, NoSuchMethodException{
      super(new URL[]{ file.toURI().toURL() }, parent);

      this.file = file;

      try {
        Class<?> jarClass;
        try {
          jarClass = super.loadClass(main);
        } catch(ClassNotFoundException|NullPointerException e) {
          throw new ClassNotFoundException("无法找到插件主类: "+main);
        }

        Class<? extends JavaPlugin> pluginClass;
        try {
          pluginClass = jarClass.asSubclass(JavaPlugin.class);
        } catch(ClassCastException e) {
          throw new ClassNotFoundException("插件主类未继承 JavaPlugin");
        }

        plugin = pluginClass.getDeclaredConstructor().newInstance();
      } catch(IllegalAccessException e) {
        throw new ClassNotFoundException("插件没有公共构造器.");
      } catch(InstantiationException e) {
        throw new ClassNotFoundException("无法实例化插件.");
      }

      PluginMain.addLoader(this);
    }

    public JavaPlugin getDeclaredPlugin() {
      return plugin;
    }

    @Override protected Class<?> findClass(String n) throws ClassNotFoundException {
      return findClass(n, true);
    }

    Class<?> findClass(String name, Boolean checkGlobal) throws ClassNotFoundException {
      Class<?> result = classes.get(name);

      if(checkGlobal && result == null) {
        result = PluginMain.getClass(name);
        classes.put(name, result);
      }

      if(result == null) {
        result = super.findClass(name);
        classes.put(name, result);
      }

      return result;
    }

  }

}
