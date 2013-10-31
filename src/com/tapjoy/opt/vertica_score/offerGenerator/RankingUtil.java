package com.tapjoy.opt.vertica_score.offerGenerator;

import java.util.Date;

import com.tapjoy.opt.util.DateUtil;
import com.tapjoy.opt.vertica_score.config.Configuration;

public class RankingUtil {
	public static String dateReplace(String out_dir, Date current) {
		out_dir = out_dir.replace(":DAY", DateUtil.getTodayDateString(current));
		out_dir = out_dir.replace(":HH", DateUtil.getHH(current));
		out_dir = out_dir.replace(":mm", DateUtil.get_mm(current));

		return out_dir;
	}

	public static String getOptOutputDir(Date current) {
		String out_dir = Configuration.Ranking.getOptOutputDir();
		
		return dateReplace(out_dir, current);
	}

	public static String getTargetOutputDir(Date current) {
		String out_dir = Configuration.Ranking.getTargetOutputDir();
		
		return dateReplace(out_dir, current);
	}

	public static String getAudtionOutputDir(Date current) {
		String out_dir = Configuration.Ranking.getAuditionOutputDir();

		return dateReplace(out_dir, current);
	}
}
