package com.raidomatic.bnet.wow;

import java.util.List;
import lombok.Data;

public @Data class SocketInfo implements BnetObject {
	private static final long serialVersionUID = 3650839411635114153L;

	private API api;
	private String socketBonus;
	private List<Socket> sockets;

}
