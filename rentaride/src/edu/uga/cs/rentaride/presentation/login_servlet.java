package edu.uga.cs.rentaride.presentation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class admin_login_servlet
 */
@WebServlet("/login_servlet")
public class login_servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public login_servlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private boolean isValid(String email, String password)
    {
    		//check email and password against the database
    		

    		return true; //change to false later
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		System.out.println("Connecting to database...");
		Connection con = DatabaseAccess.connect();
		System.out.println("Connected to database successfully.\n");
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Login</title>");
		out.println("</head>");
		out.println("<body>");
		String email = request.getParameter("email");
		String pass = request.getParameter("password");
		String admin = request.getParameter("admin");
		
		/*
		 * 4 possible outcomes:
		 * isAdmin, isValid
		 * isAdmin, invalid credentials
		 * isCustomer, isValid
		 * isCustomer, invalid credentials
		 */
		
		if(admin == null) //Customer
		{
			if(isValid(email, pass)) //show user login home page
			{
				HttpSession session=request.getSession();  
		        session.setAttribute("email",email); 
				getServletConfig().getServletContext().getRequestDispatcher("/user_browse.html").forward(request, response);
			}
			else //show invalid login page; try again
			{
				getServletConfig().getServletContext().getRequestDispatcher("/invalid_login.html").forward(request, response);
			}
		}
		else //admin
		{
			if(isValid(email, pass)) //show admin login home page
			{
				HttpSession session=request.getSession();  
		        session.setAttribute("email",email); 
				getServletConfig().getServletContext().getRequestDispatcher("/admin_create_type.html").forward(request, response);
			}
			else //show invalid login page; try again
			{
				getServletConfig().getServletContext().getRequestDispatcher("/invalid_login.html").forward(request, response);
			}
		}
		out.println("</body></html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
