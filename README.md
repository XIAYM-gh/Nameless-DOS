## Nameless DOS (Java)
这个项目参考并扩展了: [NDOS](https://cmd.xinv.ink/).<br>
这是一个扩展性高的控制台应用，允许你自由添加插件，执行命令，使用 Java 和内置脚本语言进行批量操作

#### 编译
请使用 jdk 17+<br>
依赖在根目录的 `Libs` 文件夹中
<br>
编译时，请一并编译 builtin-plugins 文件夹内的main.java<br>
编译脚本(Linux)参考此处: <br>

[Github Actions main.yml](https://github.com/XIAYM-gh/Nameless-DOS/blob/main/.github/workflows/main.yml)

#### 运行
下载链接: [Releases](https://github.com/XIAYM-gh/Nameless-DOS/releases)<br>
运行时请使用java 17，运行命令为:<br>
> java -jar ndos.jar

#### 特性
 - 支持使用插件扩展功能
 - 支持[使用MC格式化字符](https://github.com/XIAYM-gh/Nameless-DOS/blob/main/src/utils/McColorFormatter.java)(§)
 - 自动格式化配置文件
 - 支持运行外部脚本
 - 支持直接运行 Java 代码
 - 一键更新
