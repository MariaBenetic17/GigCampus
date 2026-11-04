package com.gigcampus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PublicControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / should return Home page with status 200 and Erode tagline")
    void homePageReturnsOk() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/home"))
                .andExpect(content().string(containsString("Local Businesses. Student Talent. Real Opportunities.")))
                .andExpect(content().string(containsString("GigCampus")));
    }

    @Test
    @DisplayName("GET /about should return About page with status 200")
    void aboutPageReturnsOk() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/about"))
                .andExpect(content().string(containsString("Bridging Erode MSMEs & Campus Talent")));
    }

    @Test
    @DisplayName("GET /how-it-works should return How It Works with status 200")
    void howItWorksReturnsOk() throws Exception {
        mockMvc.perform(get("/how-it-works"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/how-it-works"));
    }

    @Test
    @DisplayName("GET /gigs should return browse page with status 200 and search results")
    void browseGigsReturnsOk() throws Exception {
        mockMvc.perform(get("/gigs"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/browse"))
                .andExpect(model().attributeExists("gigs"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("GET /login should display login form with status 200")
    void loginPageReturnsOk() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("Welcome Back")));
    }

    @Test
    @DisplayName("GET /register/student should display student registration form")
    void studentRegisterPageReturnsOk() throws Exception {
        mockMvc.perform(get("/register/student"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-student"))
                .andExpect(content().string(containsString("Student Registration")));
    }

    @Test
    @DisplayName("GET /services should return services catalog with status 200")
    void servicesPageReturnsOk() throws Exception {
        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/services"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("GET /students should return browse talent page with status 200")
    void browseStudentsReturnsOk() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/students"))
                .andExpect(model().attributeExists("students"));
    }

    @Test
    @DisplayName("GET /forgot-password should display forgot password form with status 200")
    void forgotPasswordPageReturnsOk() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"))
                .andExpect(content().string(containsString("Forgot Password?")));
    }
}
