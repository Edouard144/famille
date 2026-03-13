package com.famille.servlets;

import com.famille.dao.FamilyDAO;
import com.famille.dao.ReminderDAO;
import com.famille.models.Reminder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@WebServlet("/api/reminders/*")
public class ReminderServlet extends HttpServlet {

    private final ReminderDAO reminderDAO = new ReminderDAO();
    private final FamilyDAO   familyDAO   = new FamilyDAO();

    // ── POST /api/reminders → schedule a new reminder ─────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            int userId   = (int) request.getAttribute("userId");
            int familyId = familyDAO.getFamilyIdByUserId(userId);

            // Expected: { "type":"vaccination", "message":"Inkingo ya Pacifique iri hafi",
            //             "scheduledAt":"2025-04-01T09:00:00" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            Reminder reminder = new Reminder();
            reminder.setFamilyId(familyId);
            reminder.setType(body.get("type").getAsString());
            reminder.setMessage(body.get("message").getAsString());
            reminder.setScheduledAt(
                    LocalDateTime.parse(body.get("scheduledAt").getAsString())
            );

            reminderDAO.saveReminder(reminder);

            response.setStatus(201);
            out.write("{\"message\": \"Ibibutso byashyizweho. (Reminder scheduled)\"}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}