package com.raidomatic.servlet;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.*;

public class ServletFactory implements IServletFactory {
	private ComponentModule module;

	//@Override
	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		if(path.startsWith("/xsp/calendar")) {
			return new ServletMatch(module.createServlet("com.raidomatic.servlet.CalendarServlet", "Calendar Servlet", null), "", path);
		}

		return null;
	}

	//@Override
	public void init(ComponentModule module) { this.module = module; }

}
