package com.example.spa;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * For SPA deep-link routes (e.g. /app/about), forward to /index.html.
 *
 * This is the typical setup when deploying an Angular/React/Vue build output
 * to Tomcat: unknown server paths should be handled client-side.
 */
public class SpaFallbackFilter implements Filter {
	private static final Set<String> METHOD_ALLOWLIST = Set.of("GET", "HEAD");

	@Override
	public void init(FilterConfig filterConfig) {
		// no-op
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String method = httpRequest.getMethod();
		if (!METHOD_ALLOWLIST.contains(method)) {
			chain.doFilter(request, response);
			return;
		}

		String contextPath = httpRequest.getContextPath();
		String requestUri = httpRequest.getRequestURI();
		String path = requestUri.substring(contextPath.length());

		// /app/* is handled by WebInfSpaServlet (serves from WEB-INF/app)
		if (path.equals("/app") || path.startsWith("/app/")) {
			chain.doFilter(request, response);
			return;
		}

		// API requests should never be rewritten to the SPA.
		if (path.equals("/api") || path.startsWith("/api/")) {
			chain.doFilter(request, response);
			return;
		}

		// JAX-RS (Jersey) API requests should never be rewritten either.
		if (path.equals("/api-jaxrs") || path.startsWith("/api-jaxrs/")) {
			chain.doFilter(request, response);
			return;
		}

		// Let the default servlet serve real files and special endpoints.
		if (path.equals("/")
				|| path.equals("/index.html")
				|| path.startsWith("/assets/")
				|| path.startsWith("/WEB-INF/")
				|| path.startsWith("/META-INF/")
				|| path.contains(".")) {
			chain.doFilter(request, response);
			return;
		}

		// If Tomcat already decided this path maps to something real, don't interfere.
		// When it doesn't exist, we want SPA to handle it.
		if (httpRequest.getServletContext().getResource(path) != null) {
			chain.doFilter(request, response);
			return;
		}

		httpResponse.setHeader("Cache-Control", "no-cache");
		httpRequest.getRequestDispatcher("/index.html").forward(httpRequest, httpResponse);
	}

	@Override
	public void destroy() {
		// no-op
	}
}
