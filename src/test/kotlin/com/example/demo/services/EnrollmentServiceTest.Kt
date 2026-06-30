package com.example.demo.services

import com.example.demo.dtos.EnrollmentRequest
import com.example.demo.dtos.EnrollmentResponse
import com.example.demo.dtos.StudentResponse
import com.example.demo.dtos.SubjectResponse
import com.example.demo.entities.Enrollment
import com.example.demo.entities.Student
import com.example.demo.entities.Subject
import com.example.demo.exceptions.BadRequestException
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.mappers.EnrollmentMapper
import com.example.demo.repositories.EnrollmentRepository
import com.example.demo.repositories.StudentRepository
import com.example.demo.repositories.SubjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.any
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class EnrollmentServiceTest {

    @Mock
    private lateinit var repo: EnrollmentRepository

    @Mock
    private lateinit var sRepo: StudentRepository

    @Mock
    private lateinit var subRepo: SubjectRepository

    @Mock
    private lateinit var mapper: EnrollmentMapper

    @InjectMocks
    private lateinit var service: EnrollmentService


    @Test
    fun `enroll deberia guardar inscripcion si estudiante y materia existen`() {
        val req = EnrollmentRequest(studentId = 1L, subjectId = 10L)
        val student = Student(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Programación")

        val savedEnrollment = Enrollment(id = 5L, student = student, subject = subject)
        val expectedResponse = EnrollmentResponse(
            id = 5L,
            status = "INSCRITO",
            createdAt = "",
            student = StudentResponse(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec"),
            subject = SubjectResponse(id = 10L, name = "Programación")
        )

        `when`(sRepo.findById(1L)).thenReturn(Optional.of(student))
        `when`(subRepo.findById(10L)).thenReturn(Optional.of(subject))
        `when`(repo.save(any(Enrollment::class.java))).thenReturn(savedEnrollment)
        `when`(mapper.toResponse(savedEnrollment)).thenReturn(expectedResponse)

        val result = service.enroll(req)

        assertEquals(expectedResponse, result)
        verify(sRepo, times(1)).findById(1L)
        verify(subRepo, times(1)).findById(10L)
        verify(repo, times(1)).save(any(Enrollment::class.java))
        verify(mapper, times(1)).toResponse(savedEnrollment)
    }

    @Test
    fun `enroll deberia lanzar ResourceNotFoundException si el estudiante no existe`() {
        val req = EnrollmentRequest(studentId = 99L, subjectId = 10L)

        `when`(sRepo.findById(99L)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.enroll(req)
        }

        assertEquals("Estudiante con ID 99 no encontrado", exception.message)
        verify(sRepo, times(1)).findById(99L)
    }

    @Test
    fun `enroll deberia lanzar ResourceNotFoundException si la materia no existe`() {
        val req = EnrollmentRequest(studentId = 1L, subjectId = 99L)
        val student = Student(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec")

        `when`(sRepo.findById(1L)).thenReturn(Optional.of(student))
        `when`(subRepo.findById(99L)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.enroll(req)
        }

        assertEquals("Materia con ID 99 no encontrada", exception.message)
        verify(sRepo, times(1)).findById(1L)
        verify(subRepo, times(1)).findById(99L)
    }

    @Test
    fun `getAll deberia retornar la lista de inscripciones`() {
        val student = Student(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Programación")
        val enrollment = Enrollment(id = 5L, student = student, subject = subject)
        val response = EnrollmentResponse(
            id = 5L,
            status = "INSCRITO",
            createdAt = "",
            student = StudentResponse(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec"),
            subject = SubjectResponse(id = 10L, name = "Programación")
        )

        `when`(repo.findAll()).thenReturn(listOf(enrollment))
        `when`(mapper.toResponse(enrollment)).thenReturn(response)

        val result = service.getAll()

        assertEquals(1, result.size)
        assertEquals(response, result[0])
        verify(repo, times(1)).findAll()
    }

    @Test
    fun `getById deberia retornar EnrollmentResponse si existe`() {
        val id = 5L
        val student = Student(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Programación")
        val enrollment = Enrollment(id = id, student = student, subject = subject)
        val expectedResponse = EnrollmentResponse(
            id = id,
            status = "INSCRITO",
            createdAt = "",
            student = StudentResponse(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec"),
            subject = SubjectResponse(id = 10L, name = "Programación")
        )

        `when`(repo.findById(id)).thenReturn(Optional.of(enrollment))
        `when`(mapper.toResponse(enrollment)).thenReturn(expectedResponse)

        val result = service.getById(id)

        assertEquals(expectedResponse, result)
        verify(repo, times(1)).findById(id)
        verify(mapper, times(1)).toResponse(enrollment)
    }

    @Test
    fun `getById deberia lanzar ResourceNotFoundException si no existe`() {
        val id = 99L

        `when`(repo.findById(id)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.getById(id)
        }

        assertEquals("Inscripción con ID $id no encontrada", exception.message)
        verify(repo, times(1)).findById(id)
    }

    @Test
    fun `updateStatus deberia actualizar estado si los datos son validos`() {
        val id = 5L
        val student = Student(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Programación")
        val enrollment = Enrollment(id = id, student = student, subject = subject)
        val updatedEnrollment = Enrollment(id = id, student = student, subject = subject)
        val expectedResponse = EnrollmentResponse(
            id = id,
            status = "APPROVED",
            createdAt = "",
            student = StudentResponse(id = 1L, name = "Alexander Jácome", email = "alex@puce.edu.ec"),
            subject = SubjectResponse(id = 10L, name = "Programación")
        )

        `when`(repo.findById(id)).thenReturn(Optional.of(enrollment))
        `when`(repo.save(any(Enrollment::class.java))).thenReturn(updatedEnrollment)
        `when`(mapper.toResponse(updatedEnrollment)).thenReturn(expectedResponse)

        val result = service.updateStatus(id, "APPROVED")

        assertEquals(expectedResponse, result)
        verify(repo, times(1)).findById(id)
        verify(repo, times(1)).save(any(Enrollment::class.java))
    }

    @Test
    fun `updateStatus deberia lanzar BadRequestException si el nuevo estado esta vacio`() {
        val exception = assertThrows(BadRequestException::class.java) {
            service.updateStatus(1L, "   ")
        }

        assertEquals("El nuevo estado no puede estar vacío", exception.message)
    }

    @Test
    fun `updateStatus deberia lanzar ResourceNotFoundException si la inscripcion no existe`() {
        val id = 99L

        `when`(repo.findById(id)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.updateStatus(id, "APPROVED")
        }

        assertEquals("Inscripción con ID $id no encontrada", exception.message)
        verify(repo, times(1)).findById(id)
    }

    @Test
    fun `delete elimina la inscripcion cuando el ID existe`() {
        val id = 1L

        `when`(repo.existsById(id)).thenReturn(true)

        service.delete(id)

        verify(repo, times(1)).existsById(id)
        verify(repo, times(1)).deleteById(id)
    }

    @Test
    fun `delete lanza ResourceNotFoundException cuando el ID no existe`() {
        val id = 99L

        `when`(repo.existsById(id)).thenReturn(false)

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.delete(id)
        }

        assertEquals("No se puede eliminar: Inscripción con ID $id no encontrada", exception.message)
        verify(repo, times(1)).existsById(id)
        verify(repo, times(0)).deleteById(any())
    }
}