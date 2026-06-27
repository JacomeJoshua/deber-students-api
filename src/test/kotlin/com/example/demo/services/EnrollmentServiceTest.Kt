package com.example.demo.services

import com.example.demo.dtos.EnrollmentRequest
import com.example.demo.dtos.EnrollmentResponse
import com.example.demo.dtos.StudentResponse
import com.example.demo.dtos.SubjectResponse
import com.example.demo.entities.Enrollment
import com.example.demo.entities.Student
import com.example.demo.entities.Subject
import com.example.demo.expections.BadRequestException
import com.example.demo.expections.ResourceNotFoundException
import com.example.demo.mappers.EnrollmentMapper
import com.example.demo.repositories.EnrollmentRepository
import com.example.demo.repositories.StudentRepository
import com.example.demo.repositories.SubjectRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Optional

class EnrollmentServiceTest {

    private val repo: EnrollmentRepository = mockk()
    private val sRepo: StudentRepository = mockk()
    private val subRepo: SubjectRepository = mockk()
    private val mapper: EnrollmentMapper = mockk()
    private val service = EnrollmentService(repo, sRepo, subRepo, mapper)

    @Test
    fun `enroll deberia guardar inscripcion si estudiante y materia existen`() {
        val req = EnrollmentRequest(studentId = 1L, subjectId = 10L)
        val student = Student(id = 1L, name = "Juan", email = "juan@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Programación", description = "Kotlin")
        val savedEnrollment = Enrollment(id = 5L, student = student, subject = subject)

        val expectedResponse = EnrollmentResponse(
            id = 5L,
            status = "ACTIVE",
            createdAt = null,
            student = StudentResponse(1L, "Juan", "juan@puce.edu.ec"),
            subject = SubjectResponse(10L, "Programación", "Kotlin")
        )

        every { sRepo.findById(1L) } returns Optional.of(student)
        every { subRepo.findById(10L) } returns Optional.of(subject)
        every { repo.save(any()) } returns savedEnrollment
        every { mapper.toResponse(savedEnrollment) } returns expectedResponse

        val result = service.enroll(req)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { sRepo.findById(1L) }
        verify(exactly = 1) { subRepo.findById(10L) }
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `enroll deberia lanzar ResourceNotFoundException si el estudiante no existe`() {
        val req = EnrollmentRequest(studentId = 99L, subjectId = 10L)
        every { sRepo.findById(99L) } returns Optional.empty()

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.enroll(req)
        }

        assertEquals("Estudiante con ID 99 no encontrado", exception.message)
        verify(exactly = 0) { subRepo.findById(any()) }
        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    fun `enroll deberia lanzar ResourceNotFoundException si la materia no existe`() {
        val req = EnrollmentRequest(studentId = 1L, subjectId = 99L)
        val student = Student(id = 1L, name = "Juan", email = "juan@puce.edu.ec")

        every { sRepo.findById(1L) } returns Optional.of(student)
        every { subRepo.findById(99L) } returns Optional.empty()

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.enroll(req)
        }

        assertEquals("Materia con ID 99 no encontrada", exception.message)
        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    fun `getById deberia retornar EnrollmentResponse si existe`() {
        val id = 5L
        val student = Student(id = 1L, name = "Juan", email = "juan@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Programación", description = "Kotlin")
        val enrollment = Enrollment(id = id, student = student, subject = subject)
        val expectedResponse = EnrollmentResponse(
            id = id,
            status = "ACTIVE",
            createdAt = null,
            student = StudentResponse(1L, "Juan", "juan@puce.edu.ec"),
            subject = SubjectResponse(10L, "Programación", "Kotlin")
        )

        every { repo.findById(id) } returns Optional.of(enrollment)
        every { mapper.toResponse(enrollment) } returns expectedResponse

        val result = service.getById(id)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repo.findById(id) }
    }

    @Test
    fun `updateStatus deberia actualizar estado si los datos son validos`() {
        val id = 5L
        val student = Student(id = 1L, name = "Juan", email = "juan@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Programación", description = "Kotlin")
        val enrollment = Enrollment(id = id, student = student, subject = subject, status = "PENDING")
        val updatedEnrollment = Enrollment(id = id, student = student, subject = subject, status = "APPROVED")
        val expectedResponse = EnrollmentResponse(
            id = id,
            status = "APPROVED",
            createdAt = null,
            student = StudentResponse(1L, "Juan", "juan@puce.edu.ec"),
            subject = SubjectResponse(10L, "Programación", "Kotlin")
        )

        every { repo.findById(id) } returns Optional.of(enrollment)
        every { repo.save(any()) } returns updatedEnrollment
        every { mapper.toResponse(updatedEnrollment) } returns expectedResponse

        val result = service.updateStatus(id, "APPROVED")

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repo.findById(id) }
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `updateStatus deberia lanzar BadRequestException si el nuevo estado esta vacio`() {
        val exception = assertThrows(BadRequestException::class.java) {
            service.updateStatus(1L, "   ")
        }

        assertEquals("El nuevo estado no puede estar vacío", exception.message)
        verify(exactly = 0) { repo.findById(any()) }
    }

    @Test
    fun `delete deberia llamar deleteById si el ID existe`() {
        val id = 1L
        every { repo.existsById(id) } returns true
        every { repo.deleteById(id) } returns Unit

        service.delete(id)

        verify(exactly = 1) { repo.existsById(id) }
        verify(exactly = 1) { repo.deleteById(id) }
    }

    @Test
    fun `delete deberia lanzar ResourceNotFoundException si el ID no existe`() {
        val id = 99L
        every { repo.existsById(id) } returns false

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.delete(id)
        }

        assertEquals("No se puede eliminar: Inscripción con ID $id no encontrada", exception.message)
        verify(exactly = 1) { repo.existsById(id) }
        verify(exactly = 0) { repo.deleteById(any()) }
    }
}