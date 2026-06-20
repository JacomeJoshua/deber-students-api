package com.example.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "students")
class Student(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    val name: String,
    val email: String,

    @OneToMany(mappedBy = "student", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val enrollments: List<Enrollment> = mutableListOf()
)