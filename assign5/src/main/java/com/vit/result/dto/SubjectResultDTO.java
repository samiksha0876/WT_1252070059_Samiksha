package com.vit.result.dto;

public class SubjectResultDTO {
    private String subjectName;
    private String subjectCode;
    private Double mseMarks;   // out of 30
    private Double eseMarks;   // out of 70
    private Double totalMarks; // mse + ese, out of 100
    private String grade;

    public SubjectResultDTO(String subjectName, String subjectCode, Double mseMarks,
                             Double eseMarks, Double totalMarks, String grade) {
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.mseMarks = mseMarks;
        this.eseMarks = eseMarks;
        this.totalMarks = totalMarks;
        this.grade = grade;
    }

    public String getSubjectName() { return subjectName; }
    public String getSubjectCode() { return subjectCode; }
    public Double getMseMarks() { return mseMarks; }
    public Double getEseMarks() { return eseMarks; }
    public Double getTotalMarks() { return totalMarks; }
    public String getGrade() { return grade; }
}
