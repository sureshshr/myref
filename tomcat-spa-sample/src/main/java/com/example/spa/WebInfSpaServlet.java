package com.example.spa;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serves a SPA build from a protected WEB-INF folder.
 *
 * Note: Static resources under WEB-INF are not publicly accessible by default,
 * so we expose them via this servlet mapping (e.g. /app/*).
 */
public class WebInfSpaServlet extends HttpServlet {
	private static final String WEB_INF_APP_ROOT = "/WEB-INF/app";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serve(req, resp, true);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serve(req, resp, false);
	}

	private void serve(HttpServletRequest req, HttpServletResponse resp, boolean includeBody) throws IOException {
		String pathInfo = req.getPathInfo(); // part after /app
		String requested = (pathInfo == null || pathInfo.isBlank() || pathInfo.equals("/"))
				? "/index.html"
				: pathInfo;

		// Basic path traversal protection
		if (requested.contains("..")) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String resourcePath = WEB_INF_APP_ROOT + requested;
		ServletContext sc = req.getServletContext();

		InputStream in = sc.getResourceAsStream(resourcePath);
		if (in == null) {
			// SPA deep link: for non-file routes, serve index.html
			boolean looksLikeFile = requested.contains(".");
			if (!looksLikeFile) {
				resourcePath = WEB_INF_APP_ROOT + "/index.html";
				in = sc.getResourceAsStream(resourcePath);
			}
		}

		if (in == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String contentType = sc.getMimeType(resourcePath);
		if (contentType != null) {
			resp.setContentType(contentType);
		}
		resp.setHeader("Cache-Control", "no-cache");

		if (!includeBody) {
			in.close();
			return;
		}

		try (InputStream input = in; OutputStream out = resp.getOutputStream()) {
			input.transferTo(out);
		}
	}
}
