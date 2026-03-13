package com.famille.servlets;

import com.famille.dao.ChildDAO;
import com.famille.dao.SleepDAO;
import com.famille.models.Child;
import com.famille.models.SleepRecord;
import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@WebServlet("/api/sleep/*")
public class SleepServlet extends HttpServlet {

    private final SleepDAO sleepDAO = new SleepDAO();
    private final ChildDAO childDAO = new ChildDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalTime.class,
                    // Teach Gson how to handle LocalTime as "HH:mm" string
                    (JsonSerializer<LocalTime>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    // ── POST /api/sleep/{childId} → log sleep ─────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int childId = Integer.parseInt(request.getPathInfo().substring(1));

            // Expected: { "date":"2025-03-13", "bedtime":"20:30",
            //             "wakeTime":"06:00", "napMinutes": 45 }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            SleepRecord record = new SleepRecord();
            record.setChildId(childId);
            record.setDate(LocalDate.parse(body.get("date").getAsString()));
            record.setBedtime(LocalTime.parse(body.get("bedtime").getAsString()));
            record.setWakeTime(LocalTime.parse(body.get("wakeTime").getAsString()));
            record.setNapMinutes(body.get("napMinutes").getAsInt());

            sleepDAO.saveSleepRecord(record);

            // Return the calculated total sleep hours as feedback
            double totalHours = record.getTotalSleepHours();

            response.setStatus(201);
            out.write("{\"message\": \"Ibitotsi byanditswe. (Sleep logged)\", " +
                    "\"totalSleepHours\": " + totalHours + "}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── GET /api/sleep/{childId} → sleep history + recommendation ──
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int childId = Integer.parseInt(request.getPathInfo().substring(1));

            List<SleepRecord> records = sleepDAO.getRecentRecords(childId);

            // Get the child's age to add recommended hours to the response
            Child child = childDAO.findById(childId);
            String recommended = SleepRecord.getRecommendedHours(child.getAgeInMonths());

            // Build response with records + recommendation side by side
            JsonObject result = new JsonObject();
            result.addProperty("recommendedHours", recommended);
            result.add("records", gson.toJsonTree(records));

            out.write(gson.toJson(result));

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}