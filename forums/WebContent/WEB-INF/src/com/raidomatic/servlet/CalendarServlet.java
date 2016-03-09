package com.raidomatic.servlet;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.webapp.DesignerFacesServlet;

import javax.faces.context.FacesContext;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import com.raidomatic.model.*;
import com.raidomatic.forum.model.Topic;
import lotus.domino.*;

public class CalendarServlet extends DesignerFacesServlet {
	private static final long serialVersionUID = -2304060657960557556L;
	FacesContext facesContext = null;

	@Override
	public void service(ServletRequest req, ServletResponse servletResponse) throws ServletException, IOException {
		HttpServletResponse res = (HttpServletResponse)servletResponse;
		res.setContentType("text/plain");
		ServletOutputStream out = res.getOutputStream();
		//PrintWriter out = res.getWriter();
		facesContext = this.getFacesContext(req, res);

		try {
			Session session = ExtLibUtil.getCurrentSession();
			if(session.getEffectiveUserName().equals("Anonymous")) {
				res.setStatus(401);
				res.addHeader("WWW-Authenticate", "Basic realm=\"" + req.getServerName() + "\"");
				out.println("Anonymous access prohibited");
			} else {
				res.setContentType("text/calendar");

				out.println("BEGIN:VCALENDAR");
				out.println("VERSION:2.0");
				out.println("PRODID:-//hacksw/handcal//NONSGML v1.0//EN");
				out.println("METHOD:PUBLISH");

				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
				format.setTimeZone(TimeZone.getTimeZone("UTC"));

				Signups Signups = (Signups)ExtLibUtil.resolveVariable(facesContext, "Signups");
				List<Signup> signups = Signups.getSignupsForPlayerName(session.getEffectiveUserName());
				for(Signup signup : signups) {
					try {
						Topic event = signup.getTopic();
						out.println("BEGIN:VEVENT");

						Date startTime = event.getEventDate();
						out.println("DTSTART:" + format.format(startTime));

						// Assume a three-hour event
						Calendar endTimeCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						endTimeCal.setTime(startTime);
						endTimeCal.add(Calendar.HOUR_OF_DAY, 3);
						out.println("DTEND:" + format.format(endTimeCal.getTime()));

						out.println("DTSTAMP:" + format.format(startTime));

						out.println("SUMMARY:" + event.getTitle().replace("\n", "\\n").replace(",", "\\,"));
						out.println("UID:" + event.getId());
						out.println("DESCRIPTION:http://therisentb.com/wow/forums.nsf/topics/" + event.getId());

						out.println("END:VEVENT");
					} catch(NullPointerException npe) {
						npe.printStackTrace(new PrintStream(out));
						break;
					}
				}

				out.println("END:VCALENDAR");
			}

		} catch(Exception e) {
			e.printStackTrace(new PrintStream(out));
		} finally {
			facesContext.responseComplete();
			facesContext.release();
			out.close();
		}
	}

	public static void printFeed(ServletRequest req, HttpServletResponse res) throws Exception {

		ServletOutputStream out = res.getOutputStream();
		FacesContext facesContext = FacesContext.getCurrentInstance();
		try {
			Session session = ExtLibUtil.getCurrentSession();
			if(session.getEffectiveUserName().equals("Anonymous")) {
				res.setStatus(401);
				res.addHeader("WWW-Authenticate", "Basic realm=\"" + req.getServerName() + "\"");
				out.println("Anonymous access prohibited");
			} else {
				res.setContentType("text/calendar");

				out.println("BEGIN:VCALENDAR");
				out.println("VERSION:2.0");
				out.println("PRODID:-//hacksw/handcal//NONSGML v1.0//EN");
				out.println("METHOD:PUBLISH");

				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
				format.setTimeZone(TimeZone.getTimeZone("UTC"));

				Signups Signups = (Signups)ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "Signups");
				List<Signup> signups = Signups.getSignupsForPlayerName(session.getEffectiveUserName());
				for(Signup signup : signups) {
					try {
						Topic event = signup.getTopic();
						out.println("BEGIN:VEVENT");

						Date startTime = event.getEventDate();
						out.println("DTSTART:" + format.format(startTime));

						// Assume a three-hour event
						Calendar endTimeCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						endTimeCal.setTime(startTime);
						endTimeCal.add(Calendar.HOUR_OF_DAY, 3);
						out.println("DTEND:" + format.format(endTimeCal.getTime()));

						out.println("DTSTAMP:" + format.format(startTime));

						out.println("SUMMARY:" + event.getTitle().replace("\n", "\\n").replace(",", "\\,"));
						out.println("UID:" + event.getId());
						out.println("DESCRIPTION:http://therisentb.com/wow/forums.nsf/topics/" + event.getId());

						out.println("END:VEVENT");
					} catch(NullPointerException npe) {
						npe.printStackTrace(new PrintStream(out));
						break;
					}
				}

				out.println("END:VCALENDAR");
			}
		} finally {

			facesContext.responseComplete();
			out.close();
		}
	}
}
