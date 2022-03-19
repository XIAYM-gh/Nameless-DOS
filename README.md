## Nameless DOS (Java)
这个项目是从 [NDOS](https://cmd.xinv.ink/) 还原来的.

#### 编译
请使用 jdk 17+<br>
依赖分别为:
 - /json.jar
 - /jline3.jar
 - /jansi.jar
<br>
编译时，请一并编译 builtin-plugins 文件夹内的main.java<br>
JAR 目录结构:<br>

 - META-INF
 - cn
 - org
 - ndosplugin

<br>
ndosplugin文件夹内内容:<br>

 - main.class (编译后)
 - plugin_meta

#### 运行
下载链接: [ndos.jar](https://asset.xiaym.ml/ndos.jar)<br>
运行时请使用java 17，运行命令为:<br>
> java -jar ndos.jar

#### 特性
 - 支持使用插件扩展功能
 - 支持[使用MC格式化字符](https://github.com/XIAYM-gh/Nameless-DOS/blob/main/src/utils/McColorFormatter.java)(§)
 - 自动格式化配置文件
