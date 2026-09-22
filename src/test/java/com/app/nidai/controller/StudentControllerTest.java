package com.app.nidai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.app.nidai.dto.AttendanceDto;
import com.app.nidai.dto.AttendanceRequest;
import com.app.nidai.dto.VerseDto;
import com.app.nidai.entity.AttendanceStatus;
import com.app.nidai.service.StudentService;

import tools.jackson.databind.ObjectMapper;

// Démarre uniquement le contexte web pour ce contrôleur spécifique (très rapide)
@WebMvcTest(StudentController.class)
public class StudentControllerTest {

    @Autowired 
    private MockMvc mockMvc; // L'outil qui simule les requêtes HTTP (façon Postman)

    @MockitoBean 
    private StudentService studentService; // On remplace le vrai service par un Mock

    // 1. Nouvel outil pour transformer nos objets Java en texte JSON
    @Autowired 
    private ObjectMapper objectMapper;

    @Test
    void getValidatedVerses_ShouldReturn200AndJsonArray_WhenStudentExists() throws Exception {
        // Arrange : On prépare un faux retour pour notre service
        List<VerseDto> mockVerses = List.of(
            new VerseDto(60, 1, false),
            new VerseDto(60, 2, true)
        );
        when(studentService.getValidatedVerses("MEMO-TEST")).thenReturn(mockVerses);

        // Act & Assert : On lance la fausse requête HTTP et on vérifie la réponse
        mockMvc.perform(get("/api/students/MEMO-TEST/acquis")
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Vérifie le HTTP 200
                .andExpect(jsonPath("$.size()").value(2)) // Vérifie qu'il y a 2 éléments dans le tableau JSON
                .andExpect(jsonPath("$[0].chapterNumber").value(60))
                .andExpect(jsonPath("$[0].verseNumber").value(1));
    }

    @Test
    void getValidatedVerses_ShouldReturn404_WhenStudentNotFound() throws Exception {
        // Arrange : Le service simule la levée d'une exception si l'ID est inconnu
        when(studentService.getValidatedVerses("INCONNU"))
                .thenThrow(new IllegalArgumentException("Étudiant introuvable"));

        // Act & Assert : On s'attend à ce que le contrôleur intercepte l'erreur et renvoie un 404
        mockMvc.perform(get("/api/students/INCONNU/acquis"))
                .andExpect(status().isNotFound()); // Vérifie le HTTP 404
    }

    @Test 
    void validateNextVerse_ShouldReturn200_WhenValid() throws Exception {
        // Arrange
        VerseDto mockVerse = new VerseDto(59, 6, false);
        when(studentService.validateNextVerse("MEMO-TEST")).thenReturn(mockVerse);

        // Act & Assert
        mockMvc.perform(post("/api/students/MEMO-TEST/validate-next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapterNumber").value(59))
                .andExpect(jsonPath("$.verseNumber").value(6));
    }

    @Test 
    void addAttendance_ShouldReturn200_WhenValidRequest() throws Exception {
        // Arrange : On prépare l'objet entrant (Request) et l'objet sortant (Dto)
        AttendanceRequest request = new AttendanceRequest(1L, AttendanceStatus.ABSENT);
        AttendanceDto mockResponse = new AttendanceDto(LocalDate.now(), "Seance test", AttendanceStatus.ABSENT);

        // On précise que le service doit être appelé avec l'ID "MEMO-TEST" et n'importe quel objet AttendanceRequest
        when(studentService.addAttendance(eq("MEMO-TEST"), any(AttendanceRequest.class)))
            .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/students/MEMO-TEST/attendances")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ABSENT"));
    }

    @Test
    void addAttendance_ShouldReturn409_WhenDuplicate() throws Exception {
        // Arrange
        AttendanceRequest request = new AttendanceRequest(1L, AttendanceStatus.ABSENT);
        when(studentService.addAttendance(eq("MEMO-TEST"), any(AttendanceRequest.class)))
            .thenThrow(new IllegalStateException("Un pointage existe deja pour cet étudiant a cette seance."));

        // Act & Assert : On s'attend à recevoir le code HTTP 409 Conflict
        mockMvc.perform(post("/api/students/MEMO-TEST/attendances")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

}
