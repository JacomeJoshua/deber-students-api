package com.example.demo.services

import com.example.demo.dtos.StudentRequest
import com.example.demo.dtos.StudentResponse
import com.example.demo.entities.Student
import com.example.demo.exceptions.BadRequestException
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.mappers.StudentMapper
import com.example.demo.repositories.StudentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Optional

class StudentServiceTest {

    private val repository: StudentRepository = mockk()
    private val mapper: StudentMapper = mockk()
    private val service = StudentService(repository, mapper)

    @Test
    fun `getById deberia retornar StudentResponse si el estudiante existe`() {
        val id = 1L
        val fakeStudent = Student(id = id, name = "Ana Torres", email = "ana@puce.edu.ec")
        val expectedResponse = StudentResponse(id = id, name = "Ana Torres", email = "ana@puce.edu.ec")

        every { repository.findById(id) } returns Optional.of(fakeStudent)
        every { mapper.toResponse(fakeStudent) } returns expectedResponse

        val result = service.getById(id)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { repository.findById(id) }
        verify(exactly = 1) { mapper.toResponse(fakeStudent) }
    }

    @Test
    fun `getById deberia lanzar ResourceNotFoundException si el estudiante no existe`() {
        val id = 99L
        every { repository.findById(id) } returns Optional.empty()

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.getById(id)
        }

        assertEquals("Estudiante con ID $id no encontrado", exception.message)
        verify(exactly = 1) { repository.findById(id) }
    }

    @Test
    fun `create deberia lanzar BadRequestException si el nombre esta vacio`() {
        val badRequest = StudentRequest(name = "   ", email = "ana@puce.edu.ec")

        val exception = assertThrows(BadRequestException::class.java) {
            service.create(badRequest)
        }

        assertEquals("El nombre del estudiante no puede estar vacío", exception.message)
        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `update deberia guardar los cambios si el ID existe y los datos son validos`() {
        val id = 1L
        val existingStudent = Student(id = id, name = "Ana", email = "ana@puce.edu.ec")
        val updateRequest = StudentRequest(name = "Ana Torres", email = "ana.torres@puce.edu.ec")
        val updatedStudent = Student(id = id, name = "Ana Torres", email = "ana.torres@puce.edu.ec")
        val expectedResponse = StudentResponse(id = id, name = "Ana Torres", email = "ana.torres@puce.edu.ec")

        every { repository.findById(id) } returns Optional.of(existingStudent)
        every { repository.save(any()) } returns updatedStudent
        every { mapper.toResponse(updatedStudent) } returns expectedResponse

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

        assertEquals("No se puede eliminar: Estudiante con ID $id no encontrado", exception.message)
        verify(exactly = 1) { repository.existsById(id) }
        verify(exactly = 0) { repository.deleteById(any()) }
    }
}