package com.famille.servlets;

import com.famille.dao.ChildDAO;
import com.famille.dao.FamilyDAO;
import com.famille.models.Child;
import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/children/*")
public class ChildServlet extends HttpServlet {

    private final ChildDAO  childDAO  = new ChildDAO();
    private final FamilyDAO familyDAO = new FamilyDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    // teach Gson how to convert LocalDate to/from JSON string
                    (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    // ── POST /api/children → add a new child ──────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // AuthFilter already validated the token and attached userId
            int userId = (int) request.getAttribute("userId");

            // Get this user's family ID
            int familyId = familyDAO.getFamilyIdByUserId(userId);

            // Read child data from request body
            // Expected: { "name":"Kalisa", "birthDate":"2022-03-15",
            //             "bloodType":"A+", "gender":"Male" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            Child child = new Child();
            child.setFamilyId(familyId);
            child.setName(body.get("name").getAsString().trim());
            child.setBirthDate(LocalDate.parse(body.get("birthDate").getAsString()));
            child.setBloodType(body.get("bloodType").getAsString());
            child.setGender(body.get("gender").getAsString());

            int newId = childDAO.createChild(child);
            child.setId(newId);

            response.setStatus(201);
            out.write("{\"message\": \"Umwana yongewe. (Child added)\", \"childId\": " + newId + "}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── GET /api/children → get all children in family ────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int userId   = (int) request.getAttribute("userId");
            int familyId = familyDAO.getFamilyIdByUserId(userId);

            List<Child> children = childDAO.getChildrenByFamily(familyId);

            // Convert the list to JSON and send it
            out.write(gson.toJson(children));

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── PUT /api/children/{id} → edit a child ─────────────────────
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Extract child ID from URL: /api/children/3 → "3"
            String pathInfo = request.getPathInfo(); // "/3"
            int childId = Integer.parseInt(pathInfo.substring(1));

            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            Child child = new Child();
            child.setId(childId);
            child.setName(body.get("name").getAsString().trim());
            child.setBirthDate(LocalDate.parse(body.get("birthDate").getAsString()));
            child.setBloodType(body.get("bloodType").getAsString());
            child.setGender(body.get("gender").getAsString());

            childDAO.updateChild(child);

            response.setStatus(200);
            out.write("{\"message\": \"Amakuru y'umwana avuguruwe. (Child updated)\"}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}