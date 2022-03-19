package cn.xiaym.ndos.plugins;

import java.io.*;
import java.net.*;

import cn.xiaym.utils.*;

abstract class PluginCore {

  public void onEnable(){}

  public void onDisable(){}

  public void onCommand(String cmd){}
}

public class JavaPlugin extends PluginCore {
  private String name;
  private String version;
  private String author;
  private String desc;
  private String id;

  public void info(Object obj){
    Logger.info("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void warn(Object obj){
    Logger.warn("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void err(Object obj){
    Logger.err("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void success(Object obj){
    Logger.success("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void setName(String name){
    this.name = name;
  }

  public void setVersion(String version){
    this.version = version;
  }

  public void setAuthor(String author){
    this.author = author;
  }

  public void setDesc(String desc){
    this.desc = desc;
  }

  public void setID(String id){
    this.id = id;
  }

  public String getName(){
    return this.name;
  }

  public String getVersion(){
    return this.version;
  }

  public String getAuthor(){
    return this.author;
  }

  public String getDesc(){
    return this.desc;
  }

  public String getID(){
    return this.id;
  }
}
