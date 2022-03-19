package cn.xiaym.utils;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;

public class xconfig {

        private HashMap<String, String> props = new HashMap<String, String>();
        private HashMap<Integer, String> lines = new HashMap<Integer, String>();
        private HashMap<String, Integer> lineTrace = new HashMap<String, Integer>();
        private Long createTime = System.currentTimeMillis();
        private String fileDir = "";

        public xconfig(String... fileDir){
                if(fileDir.length > 0){
                  this.fileDir = fileDir[0];
                }

                makeProps();
        }

        public void makeProps(){
                try{
                  if(!fileDir.equals("")){
                    int LinesCount = 1;
                    List<String> props = Files.readAllLines(Paths.get(this.fileDir));
                    for(String item:props){
                        lines.put(LinesCount, item);
                        if(!item.startsWith("#") && item.contains("=")){
                              String key=item.substring(0,item.indexOf("="));
                              String value=item.substring(key.length()+1);
                              this.props.put(key,value);
                              this.lineTrace.put(key,LinesCount);
                        }

                        LinesCount++;
                    }
                  }
                }catch (NoSuchFileException e){
                        // do nothing
                }catch (IOException e){
                        e.printStackTrace();
                }
        }

        public String get(String key){
                return this.props.get(key);
        }

        public String get(String key, String default_value){
                return this.props.getOrDefault(key, default_value);
        }

        public ArrayList<String> getLines_(){
                ArrayList<String> returnArr = new ArrayList<String>();
                for(int i:this.lines.keySet()){
                  returnArr.add(this.lines.get(i));
                }
                return returnArr;
        }

        public ArrayList<String> getLines(){
                ArrayList<String> returnArr = new ArrayList<String>();
                for(int i=1;i<=this.lines.size();i++){
                  returnArr.add(this.lines.get(i));
                }
                return returnArr;
        }

        public void put(String key, String value){
                this.set(key, value);
        }

        public void putIfAbsent(String key, String value){
                if(this.has(key)){
                  return;
                }
                this.set(key, value);
        }

        public void set(String key, String value){
                this.props.put(key, value);
                if(this.lineTrace.getOrDefault(key,-1)==-1){
                  this.lines.put(this.lines.size()+1, key+"="+value);
                }else{
                  this.lines.put(this.lineTrace.get(key), key+"="+value);
                }
        }

        public boolean has(String key){
                return this.props.containsKey(key);
        }

        public void remove(String key){
                if(this.has(key)){
                  this.lines.remove(this.lineTrace.get(key));
                  this.props.remove(key);
                }
        }

        public void newLine(String str){
                this.lines.put(this.lines.size()+1, str);
        }

        public void newCommentLine(String comment){
                this.newLine("# "+comment);
        }

        public void save(){
                if(!this.fileDir.equals("")){
                    this.save(this.fileDir);
                } else {
                    this.save("xconfig-save-" + this.createTime + ".properties");
                }
        }

        public void save(String path){
                try{
                  Path p = Paths.get(path);
                  if(Files.exists(p)){
                    Files.delete(p);
                  }
                  for(String i:getLines()){
                    Files.write(p, new String(i+"\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                  }
                }catch(Exception e){
                  //do nothing
                }
        }

        public Set<String> keySet(){
                return this.props.keySet();
        }

}
