package com.example.demo.services

import com.example.demo.dtos.ProfessorRequest
import com.example.demo.dtos.ProfessorResponse
import com.example.demo.entities.Professor
import com.example.demo.expections.BadRequestException
import com.example.demo.expections.ResourceNotFoundException
import com.example.demo.mappers.ProfessorMapper
import com.example.demo.repositories.ProfessorRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Optional

class ProfessorServiceTest {

    private val repository: ProfessorRepository = mockk()
    private val mapper: ProfessorMapper = mockk()
    private val service = ProfessorService(repository, mapper)

    @Test
    fun `getById deberia retornar ProfessorResponse si el profesor existe`() {
        val id = 1L
        val fakeProfessor = Professor(id = id, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")
        val expectedResponse = ProfessorResponse(id = id, name = "Dr. Armando Casas", email = "armando@puce.edu.ec")

        every { repository.findById(id) } returns Optional.of(fakeProfessor)
        every { mapper.toResponse(fakeProfessor) } returns expectedResponse

        val result = service.getById(id)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { mapper.toResponse(fakeProfessor) }
    }

    @Test
    fun `getById deberia lanzar ResourceNotFoundException si el profesor no existe`() {
        val id = 99L
        every { repository.findById(id) } returns Optional.empty()

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.getById(id)
        }

        assertEquals("Profesor con ID $id no encontrado", exception.message)
        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `create deberia lanzar BadRequestException si el nombre esta vacio`() {
        val badRequest = ProfessorRequest(name = "   ", email = "armando@puce.edu.ec")

        val exception = assertThrows(BadRequestException::class.java) {
            service.create(badRequest)
        }

        assertEquals("El nombre del profesor no puede estar vacío", exception.message)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `create deberia lanzar BadRequestException si el email esta vacio`() {
        val badRequest = ProfessorRequest(name = "Dr. Armando Casas", email = "")

        val exception = assertThrows(BadRequestException::class.java) {
            service.create(badRequest)
        }

        assertEquals("El email del profesor no puede estar vacío", exception.message)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `update deberia guardar los cambios si el ID existe y los datos son validos`() {
        val id = 1L
        val existingProfessor = Professor(id = id, name = "Armando", email = "armando@puce.edu.ec")
        val updateRequest = ProfessorRequest(name = "Dr. Armando Casas", email = "armando.casas@puce.edu.ec")
        val updatedProfessor = Professor(id = id, name = "Dr. Armando Casas", email = "armando.casas@puce.edu.ec")
        val expectedResponse = ProfessorResponse(id = id, name = "Dr. Armando Casas", email = "armando.casas@puce.edu.ec")

        every { repository.findById(id) } returns Optional.of(existingProfessor)
        every { repository.save(any()) } returns updatedProfessor
        every { mapper.toResponse(updatedProfessor) } returns expectedResponse

        val result = service.update(id, updateRequest)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `delete deberia llamar deleteById si el ID existe`() {
        val id = 1L
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

        assertEquals("No se puede eliminar: Profesor con ID $id no encontrado", exception.message)
        verify(exactly = 1) { repository.existsById(id) }
        verify(exactly = 0) { repository.deleteById(any()) }
    }
}