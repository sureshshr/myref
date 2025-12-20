package com.example.spa.jaxrs;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Path("/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentResource {
	private static final StudentRepository repo = new StudentRepository();
	private static volatile boolean initialized = false;

	private static void ensureInit() {
		if (initialized) {
			return;
		}

		synchronized (StudentResource.class) {
			if (initialized) {
				return;
			}

			try {
				repo.initSchema();
				repo.seedIfEmpty();
				initialized = true;
			} catch (SQLException e) {
				throw new WebApplicationException("DB init failed", e, Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@GET
	public List<Student> list() {
		ensureInit();
		try {
			return repo.list();
		} catch (SQLException e) {
			throw new WebApplicationException("List failed", e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/{id}")
	public Student get(@PathParam("id") String id) {
		ensureInit();
		try {
			return repo.getById(id).orElseThrow(() -> new NotFoundException("Not found"));
		} catch (SQLException e) {
			throw new WebApplicationException("Get failed", e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	public Response create(StudentInput input) {
		ensureInit();
		try {
			Student created = repo.create(input);
			return Response.status(Response.Status.CREATED).entity(created).build();
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		} catch (SQLException e) {
			throw new WebApplicationException("Create failed", e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("/{id}")
	public Student update(@PathParam("id") String id, StudentInput input) {
		ensureInit();
		try {
			Optional<Student> updated = repo.update(id, input);
			if (updated.isEmpty()) {
				throw new NotFoundException("Not found");
			}
			return updated.get();
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		} catch (SQLException e) {
			throw new WebApplicationException("Update failed", e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") String id) {
		ensureInit();
		try {
			boolean deleted = repo.delete(id);
			if (!deleted) {
				throw new NotFoundException("Not found");
			}
			return Response.ok(new Ok()).build();
		} catch (SQLException e) {
			throw new WebApplicationException("Delete failed", e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	public static class Ok {
		public boolean ok = true;
	}
}
