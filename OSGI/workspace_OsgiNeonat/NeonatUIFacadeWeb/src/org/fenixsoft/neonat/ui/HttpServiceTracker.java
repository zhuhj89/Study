package org.fenixsoft.neonat.ui;

import javax.servlet.Servlet;

import org.eclipse.equinox.http.helper.BundleEntryHttpContext;
import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.fenixsoft.neonat.useradmin.UserAdminAuthenticationHttpContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * 注册Servlet、JSP和静态资源
 * 
 * @author IcyFenix
 */
public class HttpServiceTracker extends ServiceTracker<HttpService, HttpService> {

	private BundleContext context;

	public HttpServiceTracker(BundleContext context) {
		super(context, HttpService.class, null);
		this.context = context;
	}

	@Override
	public HttpService addingService(ServiceReference<HttpService> reference) {
		HttpService hs = context.getService(reference);
		// 注册/WebContent目录为Web根目录，这样可以访问里面的静态资源
		HttpContext hc = new BundleEntryHttpContext(context.getBundle(), "/WebContent");
		// 注册/WebContent里面的*.jsp文件，这样JSP页面才可用
		Servlet jspServlet = new JspServlet(context.getBundle(), "/WebContent");
		try {
			hs.registerResources("/", "/", hc);
			//hs.registerServlet("/*.jsp", jspServlet, null, null);
			hs.registerServlet(
					"/*.jsp",
					jspServlet,
					null,
					new UserAdminAuthenticationHttpContext(hs
							.createDefaultHttpContext()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.addingService(reference);
	}
}
