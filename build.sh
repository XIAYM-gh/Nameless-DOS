rm -rf mf.txt tmp cn org META-INF ndosplugin

unzip jline3.jar > tmp
rm -rf META-INF
unzip jansi.jar > tmp
rm -rf META-INF
unzip json.jar > tmp

javac -d . -cp jline3.jar:jansi.jar:json.jar src/*.java src/console/*.java src/utils/*.java src/command/*.java src/plugins/*.java builtin-plugin/*.java

echo "Manifest-Version: 1.0">mf.txt
echo "Main-Class: cn.xiaym.ndos.NDOSMain">>mf.txt

cp builtin-plugin/plugin_meta ndosplugin/

jar -cvmf mf.txt ndos.jar ./cn ./org ./ndosplugin > tmp

rm mf.txt tmp
rm -rf cn org META-INF ndosplugin
