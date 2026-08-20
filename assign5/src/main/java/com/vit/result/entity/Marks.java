package com.vit.result.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "marks", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id"}))
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Marks scored out of 30 (weight 30%)
    private Double mseMarks;

    // Marks scored out of 70 (weight 70%)
    private Double eseMarks;

    public Marks() {}

    public Marks(Student student, Subject subject, Double mseMarks, Double eseMarks) {
        this.student = student;
        this.subject = subject;
        this.mseMarks = mseMarks;
        this.eseMarks = eseMarks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Double getMseMarks() { return mseMarks; }
    public void setMseMarks(Double mseMarks) { this.mseMarks = mseMarks; }

    public Double getEseMarks() { return eseMarks; }
    public void setEseMarks(Double eseMarks) { this.eseMarks = eseMarks; }
}
