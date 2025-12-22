package com.example.spa.jaxrs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS activation for containers that provide JAX-RS (e.g., WebSphere tWAS).
 *
 * On Tomcat, Jersey is wired via web.xml and this class is harmless.
 */
@ApplicationPath("/api-jaxrs")
public class RestApplication extends Application {
}
