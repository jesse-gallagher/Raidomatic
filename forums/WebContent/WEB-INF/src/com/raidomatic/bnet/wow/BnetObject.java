package com.raidomatic.bnet.wow;

public interface BnetObject extends java.io.Serializable {
	public void setApi(API api);
	public API getApi();
}