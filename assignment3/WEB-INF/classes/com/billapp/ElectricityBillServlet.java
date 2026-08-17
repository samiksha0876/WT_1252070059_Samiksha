package com.billapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Electricity Bill Calculator Servlet
 *
 * Slab rates:
 *   First 50 units        -> Rs. 3.50 / unit
 *   Next 100 units (51-150)-> Rs. 4.00 / unit
 *   Next 100 units(151-250)-> Rs. 5.20 / unit
 *   Above 250 units        -> Rs. 6.50 / unit
 */
@WebServlet("/CalculateBill")
public class ElectricityBillServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        String unitsStr = request.getParameter("units");

        double units;
        try {
            units = Double.parseDouble(unitsStr);
            if (units < 0) throw new NumberFormatException();
        } catch (Exception e) {
            out.println(page("<div class='result error'>"
                    + "Please enter a valid, non-negative number of units."
                    + "<br><a href='index.html'>&larr; Go back</a></div>"));
            return;
        }

        double bill = calculateBill(units);
        double gst = bill * 0.05;          // sample 5% duty, remove if not needed
        double total = bill + gst;

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='result'>");
        sb.append("<h2>Electricity Bill Summary</h2>");
        if (name != null && !name.trim().isEmpty()) {
            sb.append("<p><strong>Consumer Name:</strong> ").append(escape(name)).append("</p>");
        }
        sb.append("<p><strong>Units Consumed:</strong> ").append(units).append(" units</p>");
        sb.append("<table>");
        sb.append("<tr><th>Slab</th><th>Rate/Unit</th></tr>");
        sb.append(buildSlabRows(units));
        sb.append("</table>");
        sb.append("<p><strong>Base Amount:</strong> Rs. ").append(String.format("%.2f", bill)).append("</p>");
        sb.append("<p><strong>Duty/Tax (5%):</strong> Rs. ").append(String.format("%.2f", gst)).append("</p>");
        sb.append("<p class='total'><strong>Total Payable:</strong> Rs. ").append(String.format("%.2f", total)).append("</p>");
        sb.append("<a class='back' href='index.html'>&larr; Calculate another bill</a>");
        sb.append("</div>");

        out.println(page(sb.toString()));
    }

    /** Core slab-wise calculation logic */
    private double calculateBill(double units) {
        double bill = 0;

        if (units <= 50) {
            bill = units * 3.50;
        } else if (units <= 150) {
            bill = 50 * 3.50
                 + (units - 50) * 4.00;
        } else if (units <= 250) {
            bill = 50 * 3.50
                 + 100 * 4.00
                 + (units - 150) * 5.20;
        } else {
            bill = 50 * 3.50
                 + 100 * 4.00
                 + 100 * 5.20
                 + (units - 250) * 6.50;
        }
        return bill;
    }

    /** Builds a human-readable breakdown of which slabs were used */
    private String buildSlabRows(double units) {
        StringBuilder rows = new StringBuilder();
        double remaining = units;

        double slab1 = Math.min(remaining, 50);
        rows.append(row("0 - 50 units", "Rs. 3.50", slab1));
        remaining -= slab1;

        double slab2 = Math.max(0, Math.min(remaining, 100));
        if (units > 50) rows.append(row("51 - 150 units", "Rs. 4.00", slab2));
        remaining -= slab2;

        double slab3 = Math.max(0, Math.min(remaining, 100));
        if (units > 150) rows.append(row("151 - 250 units", "Rs. 5.20", slab3));
        remaining -= slab3;

        double slab4 = Math.max(0, remaining);
        if (units > 250) rows.append(row("Above 250 units", "Rs. 6.50", slab4));

        return rows.toString();
    }

    private String row(String label, String rate, double unitsUsed) {
        if (unitsUsed <= 0) return "";
        return "<tr><td>" + label + " (" + trim(unitsUsed) + " units used)</td><td>" + rate + "</td></tr>";
    }

    private String trim(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Wraps the result fragment in a full styled HTML page */
    private String page(String bodyContent) {
        return "<!DOCTYPE html>"
            + "<html lang='en'><head><meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
            + "<title>Bill Result</title>"
            + "<link rel='stylesheet' href='css/style.css'></head>"
            + "<body><div class='container'>" + bodyContent + "</div></body></html>";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("index.html");
    }
}
