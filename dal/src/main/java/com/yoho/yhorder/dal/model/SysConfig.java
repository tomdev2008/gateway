package com.yoho.yhorder.dal.model;

public class SysConfig {
	private Integer id;

	private String configName;

	private String configKey;

	private String configValue;

	private Byte type;

	private Integer lastModifyPid;

	private Integer lastModifyTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName == null ? null : configName.trim();
	}

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey == null ? null : configKey.trim();
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue == null ? null : configValue.trim();
	}

	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}

	public Integer getLastModifyPid() {
		return lastModifyPid;
	}

	public void setLastModifyPid(Integer lastModifyPid) {
		this.lastModifyPid = lastModifyPid;
	}

	public Integer getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Integer lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}
}