package com.vit.result.service;

import com.vit.result.dto.ResultResponse;
import com.vit.result.dto.SubjectResultDTO;
import com.vit.result.entity.Marks;
import com.vit.result.entity.Student;
import com.vit.result.repository.MarksRepository;
import com.vit.result.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResultService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MarksRepository marksRepository;

    /**
     * MSE is weighted 30%, ESE is weighted 70%.
     * mseMarks is already recorded out of 30 and eseMarks out of 70,
     * so the subject total (out of 100) is simply mseMarks + eseMarks.
     */
    public ResultResponse calculateResult(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        List<Marks> marksList = marksRepository.findByStudentId(studentId);
        if (marksList.isEmpty()) {
            throw new RuntimeException("No marks entered yet for student id: " + studentId);
        }

        List<SubjectResultDTO> subjectResults = marksList.stream().map(m -> {
            double total = m.getMseMarks() + m.getEseMarks();
            String grade = gradeFor(total, 100.0);
            return new SubjectResultDTO(
                    m.getSubject().getName(),
                    m.getSubject().getCode(),
                    m.getMseMarks(),
                    m.getEseMarks(),
                    total,
                    grade
            );
        }).collect(Collectors.toList());

        double totalObtained = subjectResults.stream().mapToDouble(SubjectResultDTO::getTotalMarks).sum();
        double maxTotal = subjectResults.size() * 100.0;
        double percentage = (totalObtained / maxTotal) * 100.0;
        percentage = Math.round(percentage * 100.0) / 100.0;

        String overallGrade = gradeFor(percentage, 100.0);
        boolean failedAnySubject = subjectResults.stream().anyMatch(s -> s.getTotalMarks() < 40.0);
        String status = (!failedAnySubject && percentage >= 40.0) ? "PASS" : "FAIL";

        return new ResultResponse(
                student.getName(),
                student.getPrn(),
                student.getBranch(),
                student.getSemester(),
                subjectResults,
                totalObtained,
                maxTotal,
                percentage,
                overallGrade,
                status
        );
    }

    private String gradeFor(double marksOutOfHundred, double max) {
        double pct = (marksOutOfHundred / max) * 100.0;
        if (pct >= 75) return "A";
        if (pct >= 65) return "B";
        if (pct >= 55) return "C";
        if (pct >= 50) return "D";
        if (pct >= 40) return "E";
        return "F";
    }
}
