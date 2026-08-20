package com.vit.result.dto;

import jakarta.validation.constraints.NotNull;

public class MarksRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long subjectId;

    @NotNull
    private Double mseMarks; // out of 30

    @NotNull
    private Double eseMarks; // out of 70

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Double getMseMarks() { return mseMarks; }
    public void setMseMarks(Double mseMarks) { this.mseMarks = mseMarks; }

    public Double getEseMarks() { return eseMarks; }
    public void setEseMarks(Double eseMarks) { this.eseMarks = eseMarks; }
}
