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

    public xconfig() {}

    public xconfig(String fileDir) {
        this.fileDir = fileDir;
        makeProps();
    }

    public void makeProps() {
        try {
          props.clear();
          lines.clear();
          lineTrace.clear();

          if(!fileDir.equals("")) {
            int LinesCount = 1;
            List<String> Props = Files.readAllLines(Paths.get(fileDir));
            for(String item : Props) {
              lines.put(LinesCount, item);
              if(!item.startsWith("#") && item.contains("=")) {
                String key=item.substring(0, item.indexOf("=")).trim();
                String value=item.substring(key.length() + 1).trim();

                props.put(key, value);
                lineTrace.put(key, LinesCount);
              }

              LinesCount++;
            }
          }
        } catch (NoSuchFileException e) {
            // do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return props.get(key);
    }

    public String get(String key, String default_value) {
        return props.getOrDefault(key, default_value);
    }

    public ArrayList<String> getLines() {
        //SORT
        ArrayList<String> returnArr = new ArrayList<String>();
        for(int i=1; i <= lines.size(); i++) {
          returnArr.add(lines.get(i));
        }
        return returnArr;
    }

    public void put(String key, String value) {
        set(key, value);
    }

    public void putIfAbsent(String key, String value) {
        if(has(key)) {
          return;
        }

        set(key, value);
    }

    public void set(String key, String value) {
        props.put(key, value);

        if(lineTrace.getOrDefault(key, -1) == -1) {
          lines.put(lines.size()+  1, key + " = " + value);
        }else{
          lines.put(lineTrace.get(key), key + " = " + value);
        }
    }

    public boolean has(String key) {
        return props.containsKey(key);
    }

    public void remove(String key) {
        if(has(key)) {
          lines.remove(lineTrace.get(key));
          props.remove(key);
        }
    }

    public void newLine() {
        newLine("");
    }

    public void newLine(String str) {
        lines.put(lines.size() + 1, str);
    }

    public void newCommentLine(String comment) {
        newLine("# "+comment);
    }

    public void save() {
        if(!fileDir.equals("")) {
            save(fileDir);
        } else {
            save("xconfig-save-" + createTime + ".properties");
        }
    }

    public void save(String path) {
        try {
          Path p = Paths.get(path);
          if(Files.exists(p)) {
            Files.delete(p);
          }

          for(String i : getLines()) {
            Files.write(p, new String(i + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
          }
        } catch(Exception e) {
          //do nothing
        }
    }

    public Set<String> keySet() {
        return props.keySet();
    }

}
