package cn.xiaym.ndos.plugins;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.net.*;
import java.lang.reflect.*;

import cn.xiaym.utils.*;
import cn.xiaym.ndos.command.*;

import static cn.xiaym.utils.LanguageUtil.Lang;

import org.json.*;

public class PluginMain {
  private static ArrayList<JavaPlugin> Plugins = new ArrayList<>();
  private static ArrayList<SimpleClassLoader> Loaders = new ArrayList<>();
  private static ArrayList<JavaPlugin> rl = new ArrayList<>();
  private static ArrayList<JavaPlugin> pBlackList = new ArrayList<>();
  private static HashMap<String, Class<?>> cachedClasses = new HashMap<>();
  private static HashMap<String, Integer> undefinedClassFindCount = new HashMap<>();
  private static ClassLoader cl = new NullClass().getClass().getClassLoader();
  private static boolean inited = false;

  public static synchronized void init(Boolean BeSilent){
    if(inited) return;

    long bef = System.currentTimeMillis();

    //加载内置组件包
    try{
      InputStream in = cl.getResourceAsStream("ndosplugin/plugin_meta");
      if(in != null){
        JavaPlugin pl = initPlugin(in, new File("Built-In"));
        Plugins.add(pl);
      } else {
        Logger.warn(Lang("pluginmanager.failed_builtin"));
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
      loadPlugin(f);
    }

    Logger.debug(Lang("pluginmanager.debug.init_ok"));

    //再逐个执行 onEnable 方法
    //并检查依赖
    int co = 0;
    rl = new ArrayList<>();
    pBlackList = new ArrayList<>();

    for (JavaPlugin plugin : Plugins) {
      co++;
      Logger.info("(" + co + "/" + Plugins.size() + ") " + plugin.getName() + " v" + plugin.getVersion());

      executePlugin(plugin);
    }

    for(JavaPlugin p:rl){
      Plugins.remove(p);
    }

    inited = true;
    Logger.info(Lang("pluginmanager.loaded_duration", System.currentTimeMillis() - bef));
  }

  private static synchronized JavaPlugin initPlugin(InputStream is, File file) {
    try{
      String randId = RandomIDGenerator.generate(16);

      Properties pc = new Properties();
      pc.load(new InputStreamReader(is, "UTF-8"));

      SimpleClassLoader scl = new SimpleClassLoader(file, pc.getProperty("plugin.main-class"), cl);
      JavaPlugin p = scl.getDeclaredPlugin();

      p.setName(pc.getProperty("plugin.name", file.getName()));
      p.setVersion(pc.getProperty("plugin.version", "1.0.0"));
      p.setAuthor(pc.getProperty("plugin.author", "Nameless"));
      p.setDesc(pc.getProperty("plugin.desc", Lang("pluginmanager.no_desc")));
      p.setDepends(new JSONArray(pc.getProperty("plugin.depends", "[]")));
      if (pc.getProperty("plugin.id") == null) Logger.warn(Lang("pluginmanager.using_random_id", p.getName(), randId));
      p.setID(pc.getProperty("plugin.id", randId));

      NDOSCommand.processPlugin(pc, p.getID());
      return p;
    } catch(ClassNotFoundException e) {
      Logger.err(e.getMessage());
      return null;
    } catch(Exception e) {
      Logger.err(Lang("pluginmanager.failed_2construct") + ": "+e.toString());
      ErrorUtil.trace(e);
      return null;
    }
  }

  public static synchronized JavaPlugin loadPlugin(File f) {
    try {
      JarFile jar = new JarFile(f);
      JarEntry entry = jar.getJarEntry("plugin_meta");
      if(entry != null){
        InputStream is = jar.getInputStream(entry);
        JavaPlugin p = initPlugin(is, f);
        Plugins.add(p);

        return p;
      } else {
        Logger.err(Lang("pluginmanager.no_meta", f.getName()));
      }
    } catch(NoSuchFileException e) {
      Logger.err(Lang("pluginmanager.not_found"));
    } catch(Exception e) {
      Logger.err(Lang("pluginmanager.exception_occurs", f.getName()));
      ErrorUtil.trace(e);
    }

    return null;
  }

  public static synchronized void executePlugin(JavaPlugin plugin) {
    for (int i=0; i<plugin.getDepends().length(); i++) {
      if(!Plugins.contains(plugin) || pBlackList.contains(plugin)) break;
      String did = String.valueOf(plugin.getDepends().get(i));
      if(!did.equals("")) {
        if(getPlugin(did) == null) {
          Logger.warn(Lang("pluginmanager.no_dependence", plugin.getName(), did));
          try {
            ArrayList<SimpleClassLoader> removingList = new ArrayList<>();

            for(SimpleClassLoader l:Loaders) {
              if(l.getDeclaredPlugin().equals(plugin)) {
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
      Logger.err(Lang("pluginmanager.disable_failed", plugin.getName()));
      ErrorUtil.trace(e);
    }
  }
 

  public static synchronized void reloadPlugins() {
    for(JavaPlugin p: Plugins) {
      try {
        p.onDisable();
      } catch(Exception e) {}
    }

    clearCache();
    Loaders.clear();
    Plugins.clear();
    NDOSCommand.clearAllCommands();
    inited = false;

    System.gc();

    init(false);

    Logger.info(Lang("pluginmanager.reload_ok"));
  }

  public static synchronized void unloadPlugin(String pid) {
    if(pid.equals("builtinPlugin")) return;

    JavaPlugin p = getPlugin(pid);

    if (p == null) {
      Logger.warn("pluginmanager.unload_failed");
      return;
    }

    NDOSCommand.removeByPlugin(p);

    clearCache();

    SimpleClassLoader lo = null;

    for (SimpleClassLoader l : Loaders) {
      if(l.getDeclaredPlugin() == p) {
        lo = l;
        break;
      }
    }

    Loaders.remove(lo);

    Plugins.remove(p);

    System.gc();

    Logger.info(Lang("pluginmanager.unload_ok"));
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

  public static boolean isPluginClass(String forName) {
    return !(getClass(forName) == null);
  }

  public static synchronized void addLoader(SimpleClassLoader lo){
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
        undefinedClassFindCount.remove(name);
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
          throw new ClassNotFoundException(Lang("scl.find_failed", main));
        }

        Class<? extends JavaPlugin> pluginClass;
        try {
          pluginClass = jarClass.asSubclass(JavaPlugin.class);
        } catch(ClassCastException e) {
          throw new ClassNotFoundException(Lang("scl.not_extends"));
        }

        plugin = pluginClass.getDeclaredConstructor().newInstance();
      } catch(IllegalAccessException e) {
        throw new ClassNotFoundException(Lang("scl.no_public_constructor"));
      } catch(InstantiationException e) {
        throw new ClassNotFoundException(Lang("scl.instant_failed"));
      }

      PluginMain.addLoader(this);
    }

    public JavaPlugin getDeclaredPlugin() {
      return plugin;
    }

    public File getPluginFile() {
      return file;
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
