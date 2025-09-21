package com.atlas.web;

import com.atlas.model.Course;
import com.atlas.repository.DynamoCourseDao;
import com.atlas.repository.DynamoLogDao;
import com.atlas.repository.DynamoStudentDao;
import com.atlas.service.CourseService;
import com.atlas.service.EnrollmentService;
import com.atlas.service.SessionStore;
import com.atlas.service.StudentService;


import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class WebApp {

    // make these static so they are accessible from the static main method and route lambdas
    private static final DynamoStudentDao studentDaoImpl = new DynamoStudentDao();
    private static final DynamoCourseDao courseDaoImpl = new DynamoCourseDao();
    private static final DynamoLogDao logDaoImpl = new DynamoLogDao();

    // constructor-injected services (no change to StudentService signature)
    private static final StudentService studentService = new StudentService(studentDaoImpl, logDaoImpl);
    private static final CourseService courseService = new CourseService(courseDaoImpl);
    private static final EnrollmentService enrollmentService = new EnrollmentService(studentDaoImpl, courseDaoImpl, logDaoImpl);

    public static void main(String[] args) {
        // run on a port that doesn't conflict with Jenkins (change via env if you prefer)
        String portEnv = System.getenv().getOrDefault("WEB_PORT", "3000");
        port(Integer.parseInt(portEnv));

        // home page
        get("/", (req, res) -> htmlPage("Atlas Academy",
                "<h1>Welcome to Atlas Academy 🎓</h1>"
                        + "<p><a href=\"/courses\">View Courses</a> | <a href=\"/login\">Login</a> | <a href=\"/signup\">Sign Up</a></p>"));

        // list courses
        get("/courses", (req, res) -> {
            List<Course> courses = courseService.list();
            String rows = courses.stream()
                    .map(c -> String.format("<li><b>%s</b> — %s<br/>enrolled: %d/%d — start:%s end:%s enrollBy:%s</li>",
                            c.getCourseId(),
                            c.getCourseName(),
                            c.getCurrentEnrolledCount(),
                            c.getMaxSeats(),
                            safe(c.getStartDate()),
                            safe(c.getEndDate()),
                            safe(c.getLatestEnrollmentBy())))
                    .collect(Collectors.joining("<hr/>"));
            String body = "<h2>Courses</h2><ul>" + rows + "</ul>" + "<p><a href=\"/\">Home</a></p>";
            return htmlPage("Courses", body);
        });

        // signup form
        get("/signup", (req, res) -> {
            String form = "<h2>Sign Up</h2>"
                    + "<form method='post' action='/signup'>" +
                    "StudentId: <input name='id'/><br/>" +
                    "Name: <input name='name'/><br/>" +
                    "Email: <input name='email'/><br/>" +
                    "Password: <input name='password' type='password'/><br/>" +
                    "<button type='submit'>Sign Up</button>" +
                    "</form>";
            return htmlPage("SignUp", form);
        });

        // signup submit
        post("/signup", (req, res) -> {
            String id = req.queryParams("id");
            String name = req.queryParams("name");
            String email = req.queryParams("email");
            String pwd = req.queryParams("password");
            try {
                studentService.register(id, name, email, pwd);
                return htmlPage("Signed Up", "<p>Account created. <a href='/login'>Login</a></p>");
            } catch (Exception ex) {
                return htmlPage("Error", "<p>Error: " + escape(ex.getMessage()) + "</p><p><a href='/signup'>Back</a></p>");
            }
        });

        // login form
        get("/login", (req, res) -> {
            String f = "<h2>Login</h2>" +
                    "<form method='post' action='/login'>" +
                    "Email: <input name='email'/><br/>" +
                    "Password: <input name='password' type='password'/><br/>" +
                    "<button type='submit'>Login</button>" +
                    "</form>";
            return htmlPage("Login", f);
        });

        // login submit
        post("/login", (req, res) -> {
            String email = req.queryParams("email");
            String pwd = req.queryParams("password");
            try {
                String token = studentService.login(email, pwd);
                // set token cookie (secure flag omitted for demo)
                res.cookie("/", "session", token, 60 * 30, false); // TTL 30 minutes
                res.redirect("/profile");
                return "";
            } catch (Exception ex) {
                return htmlPage("Login failed", "<p>" + escape(ex.getMessage()) + "</p><p><a href='/login'>Try again</a></p>");
            }
        });

        // profile
        get("/profile", (req, res) -> {
            String token = req.cookie("session");
            if (token == null || !SessionStore.isValid(token)) {
                return htmlPage("Session", "<p>Not logged in. <a href='/login'>Login</a></p>");
            }
            String studentId = SessionStore.getStudentId(token);
            String profileText = studentService.profile(studentId); // your service returns String
            // also show enroll & drop forms
            String body = "<h2>Profile</h2><pre>" + escape(profileText) + "</pre>"
                    + "<h3>Enroll</h3>" +
                    "<form method='post' action='/enroll'>" +
                    "CourseId: <input name='courseId'/> <button type='submit'>Enroll</button>" +
                    "</form>"
                    + "<h3>Drop</h3>" +
                    "<form method='post' action='/drop'>" +
                    "CourseId: <input name='courseId'/> <button type='submit'>Drop</button>" +
                    "</form>"
                    + "<p><a href='/courses'>View courses</a> | <a href='/logout'>Logout</a></p>";
            return htmlPage("Profile", body);
        });

        // enroll handler
        post("/enroll", (req, res) -> {
            String token = req.cookie("session");
            if (token == null || !SessionStore.isValid(token)) {
                return htmlPage("Error", "<p>Not logged in</p>");
            }
            String studentId = SessionStore.getStudentId(token);
            String courseId = req.queryParams("courseId");
            try {
                enrollmentService.enroll(studentId, courseId);
                res.redirect("/profile");
                return "";
            } catch (Exception ex) {
                return htmlPage("Enroll Error", "<p>" + escape(ex.getMessage()) + "</p><p><a href='/profile'>Back</a></p>");
            }
        });

        // drop handler
        post("/drop", (req, res) -> {
            String token = req.cookie("session");
            if (token == null || !SessionStore.isValid(token)) {
                return htmlPage("Error", "<p>Not logged in</p>");
            }
            String studentId = SessionStore.getStudentId(token);
            String courseId = req.queryParams("courseId");
            try {
                enrollmentService.drop(studentId, courseId);
                res.redirect("/profile");
                return "";
            } catch (Exception ex) {
                return htmlPage("Drop Error", "<p>" + escape(ex.getMessage()) + "</p><p><a href='/profile'>Back</a></p>");
            }
        });

        // logout
        get("/logout", (req, res) -> {
            String token = req.cookie("session");
            if (token != null) SessionStore.invalidate(token);
            res.removeCookie("/", "session");
            res.redirect("/");
            return "";
        });

        // simple 404 handler
        notFound((req, res) -> htmlPage("Not found", "<p>Not found</p>"));

        // after server started
        System.out.println("Web UI started at http://localhost:" + portEnv);
    }

    // helper to wrap simple HTML
    private static String htmlPage(String title, String body) {
        return "<!doctype html><html><head><title>" + escape(title) + "</title>"
                + "<style>"
                + "body{"
                + "  font-family: Arial, sans-serif;"
                + "  margin:40px;"
                + "  background-color:#1E1E2F;"   // dark background
                + "  color:#EAEAEA;"              // light gray text
                + "  text-align:center;"
                + "}"
                // Heading with popping purple background effect
                + "h1{"
                + "  color:#fff;"
                + "  background:linear-gradient(90deg,#6A0DAD,#BB86FC);" // gradient purple
                + "  display:inline-block;"
                + "  padding:15px 30px;"
                + "  border-radius:12px;"
                + "  box-shadow:0 0 25px rgba(187,134,252,0.8), 0 0 50px rgba(106,13,173,0.6);"
                + "  animation:pop 2s infinite alternate;"
                + "  margin-bottom:30px;"
                + "}"
                + "@keyframes pop{"
                + "  0%{transform:scale(1);}"
                + "  100%{transform:scale(1.05);}"
                + "}"
                // Links now purple buttons
                + "a{"
                + "  color:#6A0DAD;" // darker purple text
                + "  background:#EBD6FF;" // light purple background
                + "  text-decoration:none;"
                + "  font-weight:bold;"
                + "  margin:0 15px;"
                + "  padding:8px 14px;"
                + "  border-radius:8px;"
                + "  transition:all 0.3s ease;"
                + "}"
                + "a:hover{"
                + "  background:#BB86FC;"
                + "  color:#fff;"
                + "  box-shadow:0 0 12px rgba(187,134,252,0.8);"
                + "}"
                // Snow effect
                + ".snow{position:fixed;top:0;left:0;width:100%;height:100%;pointer-events:none;overflow:hidden;z-index:-1;}"
                + ".flake{position:absolute;background:white;border-radius:50%;opacity:0.8;animation:fall linear infinite;}"
                + "@keyframes fall{0%{transform:translateY(-10px);}100%{transform:translateY(100vh);}}"
                + "</style>"
                + "</head><body>"
                + body
                + "<div class='snow'></div>"
                + "<script>"
                + "const snow=document.querySelector('.snow');"
                + "for(let i=0;i<30;i++){"
                + "  const flake=document.createElement('div');"
                + "  flake.className='flake';"
                + "  flake.style.left=Math.random()*100+'vw';"
                + "  flake.style.width=flake.style.height=Math.random()*5+5+'px';"
                + "  flake.style.animationDuration=(Math.random()*5+5)+'s';"
                + "  flake.style.animationDelay=(Math.random()*5)+'s';"
                + "  snow.appendChild(flake);"
                + "}"
                + "</script>"
                + "</body></html>";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
