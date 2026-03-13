package com.famille.servlets;

import com.famille.dao.VaccinationDAO;
import com.famille.models.Vaccination;
import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/vaccinations/*")
public class VaccinationServlet extends HttpServlet {

    private final VaccinationDAO vaccinationDAO = new VaccinationDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    // ── GET /api/vaccinations/{childId} → full schedule ───────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Extract childId from URL: /api/vaccinations/5 → 5
            int childId = Integer.parseInt(request.getPathInfo().substring(1));

            List<Vaccination> list = vaccinationDAO.getByChildId(childId);
            out.write(gson.toJson(list));

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── PUT /api/vaccinations/{id}/done → mark vaccine done ────────
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // URL: /api/vaccinations/7/done → pathInfo = "/7/done"
            String[] parts = request.getPathInfo().split("/");
            int vaccinationId = Integer.parseInt(parts[1]);

            vaccinationDAO.markDone(vaccinationId);

            response.setStatus(200);
            out.write("{\"message\": \"Inkingo yahawe. (Vaccine marked as done)\"}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}