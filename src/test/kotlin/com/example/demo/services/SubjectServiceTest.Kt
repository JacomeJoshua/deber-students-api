package com.example.demo.services

import com.example.demo.dtos.ProfessorResponse
import com.example.demo.dtos.SubjectRequest
import com.example.demo.dtos.SubjectResponse
import com.example.demo.entities.Professor
import com.example.demo.entities.Subject
import com.example.demo.exceptions.BadRequestException
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.mappers.SubjectMapper
import com.example.demo.repositories.ProfessorRepository
import com.example.demo.repositories.SubjectRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Optional

class SubjectServiceTest {

    private val repository: SubjectRepository = mockk()
    private val profRepository: ProfessorRepository = mockk()
    private val mapper: SubjectMapper = mockk()
    private val service = SubjectService(repository, profRepository, mapper)

    @Test
    fun `create deberia guardar materia si el profesor existe y los datos son validos`() {
        // Arrange
        val req = SubjectRequest(name = "Estructuras de Datos", code = "COMP-201", professorId = 1L)
        val prof = Professor(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val savedSubject = Subject(id = 10L, name = "Estructuras de Datos", code = "COMP-201", professor = prof)
        val expectedResponse = SubjectResponse(
            id = 10L,
            name = "Estructuras de Datos",
            code = "COMP-201",
            professor = ProfessorResponse(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        )

        every { profRepository.findById(1L) } returns Optional.of(prof)
        every { mapper.toEntity(req, prof) } returns savedSubject
        every { repository.save(savedSubject) } returns savedSubject
        every { mapper.toResponse(savedSubject) } returns expectedResponse

        // Act
        val result = service.create(req)

        // Assert
        assertEquals(expectedResponse, result)
        verify(exactly = 1) { profRepository.findById(1L) }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `create deberia lanzar BadRequestException si el nombre esta vacio`() {
        // Arrange
        val req = SubjectRequest(name = "  ", code = "COMP-201", professorId = 1L)

        // Act & Assert
        val exception = assertThrows(BadRequestException::class.java) {
            service.create(req)
        }

        assertEquals("El nombre de la materia no puede estar vacío", exception.message)
        verify(exactly = 0) { profRepository.findById(any()) }
    }

    @Test
    fun `create deberia lanzar ResourceNotFoundException si el profesor no existe`() {
        // Arrange
        val req = SubjectRequest(name = "Estructuras de Datos", code = "COMP-201", professorId = 99L)
        every { profRepository.findById(99L) } returns Optional.empty()

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.create(req)
        }

        assertEquals("Profesor con ID 99 no encontrado", exception.message)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `getById deberia retornar SubjectResponse si la materia existe`() {
        // Arrange
        val id = 10L
        val prof = Professor(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val subject = Subject(id = id, name = "Estructuras de Datos", code = "COMP-201", professor = prof)
        val expectedResponse = SubjectResponse(
            id = id,
            name = "Estructuras de Datos",
            code = "COMP-201",
            professor = ProfessorResponse(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        )

        every { repository.findById(id) } returns Optional.of(subject)
        every { mapper.toResponse(subject) } returns expectedResponse

        // Act
        val result = service.getById(id)

        // Assert
        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `getById deberia lanzar ResourceNotFoundException si la materia no existe`() {
        // Arrange
        val id = 99L
        every { repository.findById(id) } returns Optional.empty()

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.getById(id)
        }

        assertEquals("Materia con ID $id no encontrada", exception.message)
        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `getAll deberia retornar la lista completa de materias`() {
        // Arrange
        val prof = Professor(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val subject = Subject(id = 10L, name = "Estructuras de Datos", code = "COMP-201", professor = prof)
        val response = SubjectResponse(
            id = 10L,
            name = "Estructuras de Datos",
            code = "COMP-201",
            professor = ProfessorResponse(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        )

        every { repository.findAll() } returns listOf(subject)
        every { mapper.toResponse(subject) } returns response

        // Act
        val result = service.getAll()

        // Assert
        assertEquals(1, result.size)
        assertEquals(response, result[0])
        verify(exactly = 1) { repository.findAll() }
    }

    @Test
    fun `update deberia guardar cambios si existe materia y profesor`() {
        // Arrange
        val id = 10L
        val req = SubjectRequest(name = "Estructuras Avanzadas", code = "COMP-202", professorId = 1L)
        val prof = Professor(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val existingSubject = Subject(id = id, name = "Estructuras de Datos", code = "COMP-201", professor = prof)
        val updatedSubject = Subject(id = id, name = "Estructuras Avanzadas", code = "COMP-202", professor = prof)
        val expectedResponse = SubjectResponse(
            id = id,
            name = "Estructuras Avanzadas",
            code = "COMP-202",
            professor = ProfessorResponse(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        )

        every { repository.findById(id) } returns Optional.of(existingSubject)
        every { profRepository.findById(1L) } returns Optional.of(prof)
        every { repository.save(any()) } returns updatedSubject
        every { mapper.toResponse(updatedSubject) } returns expectedResponse

        // Act
        val result = service.update(id, req)

        // Assert
        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { profRepository.findById(1L) }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `update deberia lanzar ResourceNotFoundException si la materia a actualizar no existe`() {
        // Arrange
        val id = 99L
        val req = SubjectRequest(name = "Estructuras Avanzadas", code = "COMP-202", professorId = 1L)
        every { repository.findById(id) } returns Optional.empty()

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.update(id, req)
        }

        assertEquals("Materia con ID $id no encontrada", exception.message)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `delete deberia llamar deleteById si el ID existe`() {
        // Arrange
        val id = 10L
        every { repository.existsById(id) } returns true
        every { repository.deleteById(id) } returns Unit

        // Act
        service.delete(id)

        // Assert
        verify(exactly = 1) { repository.existsById(id) }
        verify(exactly = 1) { repository.deleteById(id) }
    }

    @Test
    fun `delete deberia lanzar ResourceNotFoundException si el ID no existe`() {
        // Arrange
        val id = 99L
        every { repository.existsById(id) } returns false

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.delete(id)
        }

        assertEquals("No se puede eliminar: Materia con ID $id no encontrada", exception.message)
        verify(exactly = 1) { repository.existsById(id) }
        verify(exactly = 0) { repository.deleteById(any()) }
    }
}