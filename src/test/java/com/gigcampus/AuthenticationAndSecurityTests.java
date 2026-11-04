package com.gigcampus;

import com.gigcampus.dto.BusinessRegistrationDto;
import com.gigcampus.dto.StudentRegistrationDto;
import com.gigcampus.entity.Role;
import com.gigcampus.entity.User;
import com.gigcampus.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AuthenticationAndSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Student registration should encode password with BCrypt and create student profile")
    void testStudentRegistration() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setFullName("Anand Kumar");
        dto.setEmail("anand.test@kongu.ac.in");
        dto.setPassword("SecretPass@123");
        dto.setConfirmPassword("SecretPass@123");
        dto.setPhone("9876543210");
        dto.setCollegeName("Kongu Engineering College");
        dto.setDepartment("Mechanical Engineering");
        dto.setYearOfStudy("3rd Year");
        dto.setSkills("CAD, Python, Excel");
        dto.setLocation("Perundurai, Erode");

        User saved = userService.registerStudent(dto);

        assertNotNull(saved.getId());
        assertEquals("anand.test@kongu.ac.in", saved.getEmail());
        assertEquals(Role.ROLE_STUDENT, saved.getRole());
        assertTrue(passwordEncoder.matches("SecretPass@123", saved.getPassword()), "Password must be verified by BCrypt");
        assertNotEquals("SecretPass@123", saved.getPassword(), "Plaintext password must never be stored");

        var profileOpt = userService.getStudentProfileByUserId(saved.getId());
        assertTrue(profileOpt.isPresent());
        assertEquals("Kongu Engineering College", profileOpt.get().getCollegeName());
    }

    @Test
    @DisplayName("Business registration should create business profile with UNVERIFIED status")
    void testBusinessRegistration() {
        BusinessRegistrationDto dto = new BusinessRegistrationDto();
        dto.setFullName("M. Sivakumar");
        dto.setEmail("siva.textiles@erode.local");
        dto.setPassword("BusinessPass@123");
        dto.setConfirmPassword("BusinessPass@123");
        dto.setPhone("9842300001");
        dto.setBusinessName("Siva Powerloom Weaving Mills");
        dto.setBusinessCategory("Textiles & Garments");
        dto.setLocation("Veerappanchatram, Erode");
        dto.setDescription("Weaving mill manufacturing pure cotton lungis and dhotis.");

        User saved = userService.registerBusiness(dto);

        assertNotNull(saved.getId());
        assertEquals(Role.ROLE_BUSINESS, saved.getRole());
        assertTrue(passwordEncoder.matches("BusinessPass@123", saved.getPassword()));

        var profileOpt = userService.getBusinessProfileByUserId(saved.getId());
        assertTrue(profileOpt.isPresent());
        assertEquals("Siva Powerloom Weaving Mills", profileOpt.get().getBusinessName());
    }

    @Test
    @DisplayName("Duplicate email registration must throw IllegalArgumentException")
    void testDuplicateEmailRegistration() {
        StudentRegistrationDto dto1 = new StudentRegistrationDto();
        dto1.setFullName("Student One");
        dto1.setEmail("duplicate.check@erode.local");
        dto1.setPassword("Pass@123456");
        dto1.setConfirmPassword("Pass@123456");
        dto1.setPhone("9842100099");
        dto1.setCollegeName("IRTT Erode");
        dto1.setDepartment("EEE");
        userService.registerStudent(dto1);

        StudentRegistrationDto dto2 = new StudentRegistrationDto();
        dto2.setFullName("Student Two");
        dto2.setEmail("duplicate.check@erode.local");
        dto2.setPassword("Pass@123456");
        dto2.setConfirmPassword("Pass@123456");
        dto2.setPhone("9842100088");
        dto2.setCollegeName("Bannari Amman");
        dto2.setDepartment("ECE");

        assertThrows(IllegalArgumentException.class, () -> userService.registerStudent(dto2));
    }

    @Test
    @DisplayName("Passwords mismatch must throw IllegalArgumentException")
    void testPasswordMismatch() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setFullName("Test User");
        dto.setEmail("mismatch.test@erode.local");
        dto.setPassword("SecretOne@123");
        dto.setConfirmPassword("SecretTwo@123");
        dto.setPhone("9842100077");
        dto.setCollegeName("Vellalar College");
        dto.setDepartment("BCA");

        assertThrows(IllegalArgumentException.class, () -> userService.registerStudent(dto));
    }

    @Test
    @DisplayName("Unauthenticated access to protected routes must redirect to /login")
    void testProtectedRoutesRequireAuth() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/business/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
