package com.vit.result.dto;

import java.util.List;

public class ResultResponse {
    private String studentName;
    private String prn;
    private String branch;
    private Integer semester;
    private List<SubjectResultDTO> subjects;
    private Double totalMarksObtained; // out of (100 * number of subjects)
    private Double maxTotalMarks;
    private Double percentage;
    private String overallGrade;
    private String resultStatus; // PASS / FAIL

    public ResultResponse(String studentName, String prn, String branch, Integer semester,
                           List<SubjectResultDTO> subjects, Double totalMarksObtained,
                           Double maxTotalMarks, Double percentage, String overallGrade,
                           String resultStatus) {
        this.studentName = studentName;
        this.prn = prn;
        this.branch = branch;
        this.semester = semester;
        this.subjects = subjects;
        this.totalMarksObtained = totalMarksObtained;
        this.maxTotalMarks = maxTotalMarks;
        this.percentage = percentage;
        this.overallGrade = overallGrade;
        this.resultStatus = resultStatus;
    }

    public String getStudentName() { return studentName; }
    public String getPrn() { return prn; }
    public String getBranch() { return branch; }
    public Integer getSemester() { return semester; }
    public List<SubjectResultDTO> getSubjects() { return subjects; }
    public Double getTotalMarksObtained() { return totalMarksObtained; }
    public Double getMaxTotalMarks() { return maxTotalMarks; }
    public Double getPercentage() { return percentage; }
    public String getOverallGrade() { return overallGrade; }
    public String getResultStatus() { return resultStatus; }
}
