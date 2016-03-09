package com.raidomatic.bnet.wow;

public class ItemNotFoundException extends Exception {
	private static final long serialVersionUID = 754269767637140441L;

	public ItemNotFoundException() { super(); }
	public ItemNotFoundException(String message) {
		super(message);
	}
}
