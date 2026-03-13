package com.famille.servlets;

import com.famille.dao.ChildDAO;
import com.famille.dao.MealDAO;
import com.famille.models.Child;
import com.famille.models.Meal;
import com.famille.utils.MealSuggestions;
import com.google.gson.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/api/meals/*")
public class MealServlet extends HttpServlet {

    private final MealDAO  mealDAO  = new MealDAO();
    private final ChildDAO childDAO = new ChildDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.toString()))
            .create();

    // ── POST /api/meals → save today's meals ──────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Expected: { "childId":1, "date":"2025-03-13",
            //             "breakfast":"Porridge", "lunch":"Beans + Ugali",
            //             "dinner":"Isombe", "snacks":"Banana" }
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();

            Meal meal = new Meal();
            meal.setChildId(body.get("childId").getAsInt());
            meal.setDate(LocalDate.parse(body.get("date").getAsString()));
            meal.setBreakfast(body.get("breakfast").getAsString());
            meal.setLunch(body.get("lunch").getAsString());
            meal.setDinner(body.get("dinner").getAsString());
            meal.setSnacks(body.get("snacks").getAsString());

            mealDAO.saveMeal(meal); // upsert — insert or update

            response.setStatus(201);
            out.write("{\"message\": \"Amafunguro yanditswe. (Meals saved)\"}");

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }

    // ── GET /api/meals/{childId}             → recent meals ────────
    // ── GET /api/meals/{childId}/suggestions → food suggestions ────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo(); // "/5" or "/5/suggestions"
            String[] parts  = pathInfo.split("/");   // ["", "5"] or ["", "5", "suggestions"]

            int childId = Integer.parseInt(parts[1]);

            // Check if the URL ends with /suggestions
            boolean wantsSuggestions = parts.length > 2 && parts[2].equals("suggestions");

            if (wantsSuggestions) {
                // Return age-appropriate food suggestions for this child
                Child child = childDAO.findById(childId);

                if (child == null) {
                    response.setStatus(404);
                    out.write("{\"error\": \"Umwana ntaboneka. (Child not found)\"}");
                    return;
                }

                // Get age in months from the Child model
                int ageInMonths = child.getAgeInMonths();
                List<String> suggestions = MealSuggestions.getSuggestions(ageInMonths);

                // Return age + suggestions together
                JsonObject result = new JsonObject();
                result.addProperty("ageInMonths", ageInMonths);
                result.add("suggestions", gson.toJsonTree(suggestions));
                out.write(gson.toJson(result));

            } else {
                // Return last 7 days of meals
                List<Meal> meals = mealDAO.getRecentMeals(childId);
                out.write(gson.toJson(meals));
            }

        } catch (Exception e) {
            response.setStatus(500);
            out.write("{\"error\": \"Ikibazo cy'uburyo. (Server error)\"}");
            e.printStackTrace();
        }
    }
}