package com.famille.servlets;

import com.famille.dao.MedicationDAO;
import com.famille.models.Medication;
import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/medications/*")
public class MedicationServlet extends HttpServlet {

    private final MedicationDAO medicationDAO = new MedicationDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    // ── POST /api/medications/{childId} → add medication ──────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int childId = Integer.parseInt(request.getPathInfo().substring(1));

            // Expected: { "name":"Amoxicillin", "dose":"5ml",
            //             "frequency":"3x per day",
            //             "startDate":"2025-03-13", "endDate":"2025-03-20" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            Medication med = new Medication();
            med.setChildId(childId);
            med.setName(body.get("name").getAsString().trim());
            med.setDose(body.get("dose").getAsString().trim());
            med.setFrequency(body.get("frequency").getAsString().trim());
            med.setStartDate(LocalDate.parse(body.get("startDate").getAsString()));

            // End date is optional
            if (body.has("endDate") && !body.get("endDate").isJsonNull()) {
                med.setEndDate(LocalDate.parse(body.get("endDate").getAsString()));
            }

            int newId = medicationDAO.saveMedication(med);

            response.setStatus(201);
            out.write("{\"message\": \"Umuti wanditswe. (Medication saved)\", " +
                    "\"medicationId\": " + newId + "}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── GET /api/medications/{childId} → active medications ───────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int childId = Integer.parseInt(request.getPathInfo().substring(1));

            List<Medication> medications = medicationDAO.getActiveMedications(childId);
            out.write(gson.toJson(medications));

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── DELETE /api/medications/{id} → remove medication ──────────
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int medicationId = Integer.parseInt(request.getPathInfo().substring(1));

            medicationDAO.deleteMedication(medicationId);

            response.setStatus(200);
            out.write("{\"message\": \"Umuti gusezererwa. (Medication removed)\"}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}