/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servlets;

import entity.Role;
import entity.User;
import entity.UserRole;
import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import session.RoleFacade;
import session.UserFacade;
import session.UserRoleFacade;
import tools.PasswordProtected;

/**
 *
 * @author pupil
 */
@WebServlet(name = "LoginServlet", urlPatterns = {
    "/showlogin",
    "/login",
    "/logout",
})

public class LoginServlet extends HttpServlet {
    @EJB UserFacade userFacade;
    @EJB RoleFacade roleFacade;
    @EJB UserRoleFacade userRoleFacade;

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        if(userFacade.count()>0) return;
        User user = new User();
        user.setFirstName("Daniil");
        user.setSecondName("Vasiljev");
        user.setPhone("59823871");
        user.setLogin("admin");
        user.setMoney(500);
        PasswordProtected passwordProtected = new PasswordProtected();
        String salt = passwordProtected.getSalt();
        user.setSalt(salt);
        String password = passwordProtected.getProtectedPassword("12345", salt);
        user.setPassword(password);
        userFacade.create(user);
        Role role = new Role();
        role.setRoleName("USER");
        roleFacade.create(role);
        UserRole ur = new UserRole();
        ur.setRole(role);
        ur.setUser(user);
        userRoleFacade.create(ur);
        role = new Role();
        role.setRoleName("DIRECTOR");
        roleFacade.create(role);
        ur = new UserRole();
        ur.setRole(role);
        ur.setUser(user);
        userRoleFacade.create(ur);
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
            request.setCharacterEncoding("UTF-8");
            String path = request.getServletPath();
            switch(path) {
                case "/showLogin":
                    request.getRequestDispatcher("/showLogin.jsp").forward(request, response);
                    break;
                case "/login":
                    String login = request.getParameter("login");
                    String password = request.getParameter("password");
                    //Authentification
                    User authUser = userFacade.findByLogin(login);
                    if(authUser == null){
                        request.setAttribute("info", "Неверный логин или пароль");
                        request.getRequestDispatcher("/showLogin").forward(request, response);
                        break;
                    }
                    //Authorization
                    String salt = authUser.getSalt();
                    PasswordProtected passwordProtected = new PasswordProtected();
                    password = passwordProtected.getProtectedPassword(password, salt);
                    if(!password.equals(authUser.getPassword())){
                        request.setAttribute("info", "Неверный логин или пароль");
                        request.getRequestDispatcher("/showLogin").forward(request, response);
                        break;
                    }
                    HttpSession session = request.getSession(true);
                    session.setAttribute("authUser", authUser);
                    request.setAttribute("info", "Привет, "+authUser.getFirstName());
                    request.getRequestDispatcher("/showIndex").forward(request, response);
                    break;
                case "/logout":
                    session = request.getSession(false);
                    if(session != null){
                        session.invalidate();
                        request.setAttribute("info", "Вы вышли");
                    }
                    request.getRequestDispatcher("/showIndex").forward(request, response);
                    break;
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
        processRequest(request, response);
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
