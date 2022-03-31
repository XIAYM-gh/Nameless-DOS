package cn.xiaym.ndos.plugins;

import java.io.*;
import java.net.*;

import cn.xiaym.utils.*;

import org.json.*;

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
  private JSONArray depends;

  public final void info(Object obj){
    Logger.info("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public final void warn(Object obj){
    Logger.warn("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public final void err(Object obj){
    Logger.err("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public final void success(Object obj){
    Logger.success("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public final void setName(String name){
    this.name = name;
  }

  public final void setVersion(String version){
    this.version = version;
  }

  public final void setAuthor(String author){
    this.author = author;
  }

  public final void setDesc(String desc){
    this.desc = desc;
  }

  public final void setID(String id){
    this.id = id;
  }

  public final void setDepends(JSONArray depends){
    this.depends = depends;
  }

  public final String getName(){
    return this.name;
  }

  public final String getVersion(){
    return this.version;
  }

  public final String getAuthor(){
    return this.author;
  }

  public final String getDesc(){
    return this.desc;
  }

  public final String getID(){
    return this.id;
  }

  public final JSONArray getDepends(){
    return this.depends;
  }
}
