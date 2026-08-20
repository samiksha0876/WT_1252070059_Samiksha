package com.vit.result.controller;

import com.vit.result.entity.Student;
import com.vit.result.entity.Subject;
import com.vit.result.entity.Marks;
import com.vit.result.dto.MarksRequest;
import com.vit.result.repository.StudentRepository;
import com.vit.result.repository.SubjectRepository;
import com.vit.result.repository.MarksRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private MarksRepository marksRepository;

    // ---- Students ----
    @PostMapping("/students")
    public ResponseEntity<Student> addStudent(@Valid @RequestBody Student student) {
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable Long id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---- Subjects ----
    @PostMapping("/subjects")
    public ResponseEntity<Subject> addSubject(@RequestBody Subject subject) {
        return ResponseEntity.ok(subjectRepository.save(subject));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectRepository.findAll());
    }

    // ---- Marks ----
    @PostMapping("/marks")
    public ResponseEntity<Marks> addMarks(@Valid @RequestBody MarksRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Marks marks = new Marks(student, subject, request.getMseMarks(), request.getEseMarks());
        return ResponseEntity.ok(marksRepository.save(marks));
    }

    @GetMapping("/marks/student/{studentId}")
    public ResponseEntity<List<Marks>> getMarksForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(marksRepository.findByStudentId(studentId));
    }
}
