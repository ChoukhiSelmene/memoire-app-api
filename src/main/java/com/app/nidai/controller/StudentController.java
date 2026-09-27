package com.app.nidai.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.nidai.dto.AttendanceDto;
import com.app.nidai.dto.AttendanceRequest;
import com.app.nidai.dto.VerseDto;
import com.app.nidai.service.StudentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
@RequestMapping(value = "api/students/{shortId}", produces = "application/json")
public class StudentController {

    private final StudentService studentService;

    // Injection par constructeur (comme vu précédemment)
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    
    @GetMapping("/acquis")
    public ResponseEntity<List<VerseDto>> getValidatedVerses(@PathVariable  String shortId) {
        try{
            // On délègue le travail au Service
            List<VerseDto> verses = studentService.getValidatedVerses(shortId);

            // On retourne un statut HTTP 200 (OK) avec la liste en corps de réponse
            return ResponseEntity.ok(verses);

        } catch (IllegalArgumentException e) {
            // Si le Service lève une exception (étudiant introuvable), on retourne un statut HTTP 404
            return ResponseEntity.notFound().build();
        }
        
    }

    @GetMapping("/attendances")
    public ResponseEntity<List<AttendanceDto>> getStudentAttendances(@PathVariable String shortId) {
        try {
            List<AttendanceDto> attendances = studentService.getStudentAttendances(shortId);
            return ResponseEntity.ok(attendances);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate-next")
    public ResponseEntity<VerseDto> validateNextVerse(@PathVariable String shortId) {
        try {
            VerseDto validatedVerse = studentService.validateNextVerse(shortId);
            return ResponseEntity.ok(validatedVerse);
        } catch(IllegalArgumentException e) {
            // L'étudiant n'existe pas
            return ResponseEntity.notFound().build();
        } catch(IllegalStateException e) {
            // L'étudiant a déjà fini le livre (le vers suivant n'existe pas)
            // On renvoie un code 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/attendances")
    public ResponseEntity<AttendanceDto> addAttendance(
        @PathVariable String shortId,
        @RequestBody AttendanceRequest request) {
        try{
            AttendanceDto saveAttendance = studentService.addAttendance(shortId, request);
            // On renvoie HTTP 200 avec le DTO du pointage nouvellement créé
            return ResponseEntity.ok(saveAttendance);
        } catch(IllegalArgumentException e) {
            // Si l'étudiant OU la séance n'existe pas
            return ResponseEntity.notFound().build();
        } catch(IllegalStateException e) {
            return ResponseEntity.status(409).build(); // 409 Conflict (Doublon)
        }
    }
    
    

}
