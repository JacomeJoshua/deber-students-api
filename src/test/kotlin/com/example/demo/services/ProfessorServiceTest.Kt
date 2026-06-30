package com.example.demo.services

import com.example.demo.dtos.SubjectRequest
import com.example.demo.dtos.SubjectResponse
import com.example.demo.entities.Subject
import com.example.demo.exceptions.BadRequestException
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.mappers.SubjectMapper
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
    private val mapper: SubjectMapper = mockk()
    private val service = SubjectService(repository, mapper)

    @Test
    fun `getById deberia retornar SubjectResponse si la materia existe`() {

        val id = 10L
        val fakeSubject = Subject(id = id, name = "Programación")
        val expectedResponse = SubjectResponse(id = id, name = "Programación")

        every { repository.findById(id) } returns Optional.of(fakeSubject)
        every { mapper.toResponse(fakeSubject) } returns expectedResponse


        val result = service.getById(id)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { mapper.toResponse(fakeSubject) }
    }

    @Test
    fun `getById deberia lanzar ResourceNotFoundException si la materia no existe`() {

        val id = 99L
        every { repository.findById(id) } returns Optional.empty()


        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.getById(id)
        }

        assertEquals("Materia con ID $id no encontrada", exception.message)
        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `create deberia guardar la materia cuando los datos son validos`() {

        val request = SubjectRequest(name = "Programación")
        val subjectEntity = Subject(name = "Programación")
        val savedSubject = Subject(id = 10L, name = "Programación")
        val expectedResponse = SubjectResponse(id = 10L, name = "Programación")

        every { mapper.toEntity(request) } returns subjectEntity
        every { repository.save(subjectEntity) } returns savedSubject
        every { mapper.toResponse(savedSubject) } returns expectedResponse


        val result = service.create(request)


        assertEquals(expectedResponse, result)
        verify(exactly = 1) { mapper.toEntity(request) }
        verify(exactly = 1) { repository.save(subjectEntity) }
        verify(exactly = 1) { mapper.toResponse(savedSubject) }
    }

    @Test
    fun `create deberia lanzar BadRequestException si el nombre esta vacio`() {

        val badRequest = SubjectRequest(name = "   ")


        val exception = assertThrows(BadRequestException::class.java) {
            service.create(badRequest)
        }

        assertEquals("El nombre de la materia no puede estar vacío", exception.message)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `getAll deberia retornar la lista de materias`() {

        val subject = Subject(id = 10L, name = "Programación")
        val response = SubjectResponse(id = 10L, name = "Programación")

        every { repository.findAll() } returns listOf(subject)
        every { mapper.toResponse(subject) } returns response


        val result = service.getAll()


        assertEquals(1, result.size)
        assertEquals(response, result[0])
        verify(exactly = 1) { repository.findAll() }
    }

    @Test
    fun `update deberia guardar los cambios si el ID existe y los datos son validos`() {

        val id = 10L
        val existingSubject = Subject(id = id, name = "Prog Vieja")
        val updateRequest = SubjectRequest(name = "Programación Avanzada")
        val updatedSubject = Subject(id = id, name = "Programación Avanzada")
        val expectedResponse = SubjectResponse(id = id, name = "Programación Avanzada")

        every { repository.findById(id) } returns Optional.of(existingSubject)
        every { repository.save(any()) } returns updatedSubject
        every { mapper.toResponse(updatedSubject) } returns expectedResponse


        val result = service.update(id, updateRequest)


        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
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