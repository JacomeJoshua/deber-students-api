package com.example.demo.services

import com.example.demo.dtos.ProfessorResponse
import com.example.demo.dtos.SubjectRequest
import com.example.demo.dtos.SubjectResponse
import com.example.demo.entities.Professor
import com.example.demo.entities.Subject
import com.example.demo.expections.BadRequestException
import com.example.demo.expections.ResourceNotFoundException
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
        val req = SubjectRequest(name = "Estructuras de Datos", code = "COMP-201", professorId = 1L)
        val prof = Professor(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val savedSubject = Subject(id = 10L, name = "Estructuras de Datos", code = "COMP-201", professor = prof)
        val expectedResponse = SubjectResponse(
            id = 10L,
            name = "Estructuras de Datos",
            code = "COMP-201",
            professor = ProfessorResponse(1L, "Dr. Armando Casas", "armando@puce.edu.ec")
        )

        every { profRepository.findById(1L) } returns Optional.of(prof)
        every { mapper.toEntity(req, prof) } returns savedSubject
        every { repository.save(savedSubject) } returns savedSubject
        every { mapper.toResponse(savedSubject) } returns expectedResponse

        val result = service.create(req)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { profRepository.findById(1L) }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `create deberia lanzar BadRequestException si el nombre esta vacio`() {
        val req = SubjectRequest(name = "  ", code = "COMP-201", professorId = 1L)

        val exception = assertThrows(BadRequestException::class.java) {
            service.create(req)
        }

        assertEquals("El nombre de la materia no puede estar vacío", exception.message)
        verify(exactly = 0) { profRepository.findById(any()) }
    }

    @Test
    fun `create deberia lanzar ResourceNotFoundException si el profesor no existe`() {
        val req = SubjectRequest(name = "Estructuras de Datos", code = "COMP-201", professorId = 99L)
        every { profRepository.findById(99L) } returns Optional.empty()

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.create(req)
        }

        assertEquals("Profesor con ID 99 no encontrado", exception.message)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `getById deberia retornar SubjectResponse si la materia existe`() {
        val id = 10L
        val prof = Professor(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val subject = Subject(id = id, name = "Estructuras de Datos", code = "COMP-201", professor = prof)
        val expectedResponse = SubjectResponse(
            id = id,
            name = "Estructuras de Datos",
            code = "COMP-201",
            professor = ProfessorResponse(1L, "Dr. Armando Casas", "armando@puce.edu.ec")
        )

        every { repository.findById(id) } returns Optional.of(subject)
        every { mapper.toResponse(subject) } returns expectedResponse

        val result = service.getById(id)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `update deberia guardar cambios si existe materia y profesor`() {
        val id = 10L
        val req = SubjectRequest(name = "Estructuras Avanzadas", code = "COMP-202", professorId = 1L)
        val prof = Professor(id = 1L, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val existingSubject = Subject(id = id, name = "Estructuras de Datos", code = "COMP-201", professor = prof)
        val updatedSubject = Subject(id = id, name = "Estructuras Avanzadas", code = "COMP-202", professor = prof)
        val expectedResponse = SubjectResponse(
            id = id,
            name = "Estructuras Avanzadas",
            code = "COMP-202",
            professor = ProfessorResponse(1L, "Dr. Armando Casas", "armando@puce.edu.ec")
        )

        every { repository.findById(id) } returns Optional.of(existingSubject)
        every { profRepository.findById(1L) } returns Optional.of(prof)
        every { repository.save(any()) } returns updatedSubject
        every { mapper.toResponse(updatedSubject) } returns expectedResponse

        val result = service.update(id, req)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { profRepository.findById(1L) }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `delete deberia llamar deleteById si el ID existe`() {
        val id = 10L
        every { repository.existsById(id) } returns true
        every { repository.deleteById(id) } returns Unit

        service.delete(id)

        verify(exactly = 1) { repository.existsById(id) }
        verify(exactly = 1) { repository.deleteById(id) }
    }

    @Test
    fun `delete deberia lanzar ResourceNotFoundException si el ID no existe`() {
        val id = 99L
        every { repository.existsById(id) } returns false

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.delete(id)
        }

        assertEquals("No se puede eliminar: Materia con ID $id no encontrada", exception.message)
        verify(exactly = 1) { repository.existsById(id) }
        verify(exactly = 0) { repository.deleteById(any()) }
    }
}