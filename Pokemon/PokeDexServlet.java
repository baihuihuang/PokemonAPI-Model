package Pokemon;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Baihui
 */
//@WebServlet(name = "PokeDexServlet", urlPatterns = {"/PokeDexServlet/*"})
public class PokeDexServlet extends HttpServlet {

    // the model to call API 
    PokeDexModel model = null;
    
    // initialize the model for servlet
    @Override 
    public void init() {
        model = new PokeDexModel();
    }
    
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet PokeDexServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet PokeDexServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
     
        String header = request.getHeader("User-Agent");
        
        System.out.println("Console: doGET visited");
        String result = "";

        String pokeIndex = request.getPathInfo().substring(1);
        
        System.out.println("Index in path: " + pokeIndex);

        JSONObject pokemon = null;
        if (pokeIndex.equals("")) {
            response.setStatus(401);
            return;
        } else {
            // Use the model to get JSON ouotput from API
            pokemon = model.connectPokeAPI(pokeIndex);
        }

        
        try {
            model.mongoDBlog(pokemon, header);
        } catch (JSONException ex) {
            Logger.getLogger(PokeDexServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        result = pokemon.toString();
        PrintWriter out = response.getWriter();
        out.println(result);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>




}
