package com.famille.servlets;

import com.famille.dao.FamilyDAO;
import com.famille.dao.PregnancyDAO;
import com.famille.models.Pregnancy;
import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/api/pregnancy/*")
public class PregnancyServlet extends HttpServlet {

    private final PregnancyDAO pregnancyDAO = new PregnancyDAO();
    private final FamilyDAO    familyDAO    = new FamilyDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    // ── POST /api/pregnancy → register pregnancy ───────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int userId   = (int) request.getAttribute("userId");
            int familyId = familyDAO.getFamilyIdByUserId(userId);

            // Expected: { "dueDate": "2025-11-20" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            LocalDate dueDate = LocalDate.parse(body.get("dueDate").getAsString());

            Pregnancy pregnancy = new Pregnancy();
            pregnancy.setFamilyId(familyId);
            pregnancy.setDueDate(dueDate);

            int newId = pregnancyDAO.createPregnancy(pregnancy);

            response.setStatus(201);
            out.write("{\"message\": \"Inda yanditswe. (Pregnancy registered)\", " +
                    "\"pregnancyId\": " + newId + ", " +
                    "\"currentWeek\": " + pregnancy.calculateCurrentWeek() + "}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── GET /api/pregnancy → get current pregnancy ─────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int userId     = (int) request.getAttribute("userId");
            int familyId   = familyDAO.getFamilyIdByUserId(userId);
            Pregnancy p    = pregnancyDAO.getByFamilyId(familyId);

            if (p == null) {
                response.setStatus(404);
                out.write("{\"message\": \"Nta nda ihari. (No active pregnancy)\"}");
                return;
            }

            // Recalculate the current week live before returning
            int liveWeek = p.calculateCurrentWeek();
            p.setCurrentWeek(liveWeek);

            // Also update it in the DB so it stays accurate
            pregnancyDAO.updateWeek(p.getId(), liveWeek);

            out.write(gson.toJson(p));

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}