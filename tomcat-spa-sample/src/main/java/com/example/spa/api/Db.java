package com.example.spa.api;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

import java.io.File;

public final class Db {
	private static volatile DataSource dataSource;

	private Db() {
	}

	public static DataSource getDataSource() {
		DataSource existing = dataSource;
		if (existing != null) {
			return existing;
		}

		synchronized (Db.class) {
			if (dataSource != null) {
				return dataSource;
			}

			String catalinaBase = System.getProperty("catalina.base");
			File baseDir = catalinaBase == null || catalinaBase.isBlank()
					? new File(System.getProperty("java.io.tmpdir"), "tomcat-spa-sample")
					: new File(catalinaBase);

			File dbDir = new File(baseDir, "h2");
			//noinspection ResultOfMethodCallIgnored
			dbDir.mkdirs();

			String dbFile = new File(dbDir, "employee_crud").getAbsolutePath();
			String url = "jdbc:h2:file:" + dbFile + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";

			JdbcDataSource ds = new JdbcDataSource();
			ds.setURL(url);
			ds.setUser("sa");
			ds.setPassword("sa");

			dataSource = ds;
			return ds;
		}
	}
}
