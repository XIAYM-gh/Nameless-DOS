package cn.xiaym.ndos.bootstrap;

import cn.xiaym.utils.*;
import java.util.*;

public class Boot {
	public static void main(String[] args) {
		//Boot up NDOS
		try {
			cn.xiaym.ndos.NDOSMain.main(args);
		} catch(Exception e) {
			Logger.err("NDOS ran into a problem.");
			Logger.err("Error: " + e);
			Logger.err("This problem is uncaughted, please contact the developer.");
			Logger.err("Stacktrace shown below:");
			ErrorUtil.trace(e);
			System.exit(-1);
		}
	}
}