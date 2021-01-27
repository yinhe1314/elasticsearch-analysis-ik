/**
 * 
 */
package org.wltea.analyzer.cfg;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugin.analysis.ik.AnalysisIkPlugin;
import org.wltea.analyzer.dic.Dictionary;

import java.io.File;
import java.nio.file.Path;

public class Configuration {

	private Environment environment;
	private Settings settings;

	//是否启用智能分词
	private  boolean useSmart;

	//是否启用远程词典加载
	private boolean enableRemoteDict=false;

	//是否启用小写处理
	private boolean enableLowercase=true;

	// TODO 词典url
	private String dictUrl;

	// TODO 词典key
	private String dictKey;

	@Inject
	public Configuration(Environment env,Settings settings) {
		this.environment = env;
		this.settings=settings;

		this.useSmart = settings.get("use_smart", "false").equals("true");
		this.enableLowercase = settings.get("enable_lowercase", "true").equals("true");
		this.enableRemoteDict = settings.get("enable_remote_dict", "true").equals("true");

		Dictionary.initial(this);

		// TODO 获取词典url，初始化词典
		this.dictUrl = settings.get("dict_url");
		if (this.dictUrl != null && !"".equals(this.dictUrl)) {
			this.dictKey = Dictionary.getSingleton().getMD5(this.dictUrl);
			Dictionary.getSingleton().initDict(this);
		}
	}

	public Path getConfigInPluginDir() {
		return PathUtils
				.get(new File(AnalysisIkPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
						.getParent(), "config")
				.toAbsolutePath();
	}

	public boolean isUseSmart() {
		return useSmart;
	}

	public Configuration setUseSmart(boolean useSmart) {
		this.useSmart = useSmart;
		return this;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Settings getSettings() {
		return settings;
	}

	public boolean isEnableRemoteDict() {
		return enableRemoteDict;
	}

	public boolean isEnableLowercase() {
		return enableLowercase;
	}

	/**
	 * TODO 获取词典url
	 */
	public String getDictUrl() {
		return dictUrl;
	}

	/**
	 * TODO 获取词典key
	 */
	public String getDictKey() {
		return dictKey;
	}
}
