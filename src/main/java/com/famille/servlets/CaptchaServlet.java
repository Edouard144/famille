package com.famille.servlets;

import com.famille.utils.CaptchaGenerator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

// This route is public — AuthFilter already whitelists /api/captcha
@WebServlet("/api/captcha")
public class CaptchaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Step 1 — Generate random 5-character answer text
        String captchaText = CaptchaGenerator.generateText();

        // Step 2 — Store the answer in the session
        // Session is tied to this user's browser/app session
        // When they submit signup, we'll read it back from here
        HttpSession session = request.getSession(true); // create session if none exists
        session.setAttribute("captchaAnswer", captchaText);
        session.setMaxInactiveInterval(5 * 60); // session lives 5 minutes

        // Step 3 — Generate the distorted image
        BufferedImage image = CaptchaGenerator.generateImage(captchaText);

        // Step 4 — Send the image as PNG directly in the response
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store"); // don't cache CAPTCHA

        OutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out); // write image bytes to response
        out.flush();
    }
}