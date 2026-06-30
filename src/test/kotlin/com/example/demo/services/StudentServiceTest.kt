package com.example.demo.services

import com.example.demo.dtos.StudentRequest
import com.example.demo.dtos.StudentResponse
import com.example.demo.entities.Student
import com.example.demo.exceptions.BadRequestException
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.mappers.StudentMapper
import com.example.demo.repositories.StudentRepository
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
class StudentServiceTest {

    @Mock
    private lateinit var repository: StudentRepository

    @Mock
    private lateinit var mapper: StudentMapper

    @InjectMocks
    private lateinit var service: StudentService


    @Test
    fun `getById retorna StudentResponse cuando el estudiante existe`() {

        val id = 1L
        val student = Student(id = id, name = "Alexander Jácome", email = "alex@puce.edu.ec")
        val expectedResponse = StudentResponse(id = id, name = "Alexander Jácome", email = "alex@puce.edu.ec")

        `when`(repository.findById(id)).thenReturn(Optional.of(student))
        `when`(mapper.toResponse(student)).thenReturn(expectedResponse)


        val result = service.getById(id)

        assertEquals(expectedResponse, result)
        verify(repository, times(1)).findById(id)
        verify(mapper, times(1)).toResponse(student)
    }

    @Test
    fun `getById lanza ResourceNotFoundException cuando el estudiante no existe`() {

        val id = 99L
        `when`(repository.findById(id)).thenReturn(Optional.empty())


        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.getById(id)
        }

        assertEquals("Estudiante con ID $id no encontrado", exception.message)
        verify(repository, times(1)).findById(id)
    }


    @Test
    fun `create guarda al estudiante cuando los datos son validos`() {

        val request = StudentRequest(name = "Alexander", email = "alex@puce.edu.ec")
        val studentEntity = Student(name = "Alexander", email = "alex@puce.edu.ec")
        val savedStudent = Student(id = 1L, name = "Alexander", email = "alex@puce.edu.ec")
        val expectedResponse = StudentResponse(id = 1L, name = "Alexander", email = "alex@puce.edu.ec")

        `when`(mapper.toEntity(request)).thenReturn(studentEntity)
        `when`(repository.save(studentEntity)).thenReturn(savedStudent)
        `when`(mapper.toResponse(savedStudent)).thenReturn(expectedResponse)

        val result = service.create(request)


        assertEquals(expectedResponse, result)
        verify(mapper, times(1)).toEntity(request)
        verify(repository, times(1)).save(studentEntity)
        verify(mapper, times(1)).toResponse(savedStudent)
    }

    @Test
    fun `create lanza BadRequestException cuando el nombre esta vacio`() {

        val badRequest = StudentRequest(name = "   ", email = "alex@puce.edu.ec")


        val exception = assertThrows(BadRequestException::class.java) {
            service.create(badRequest)
        }

        assertEquals("El nombre del estudiante no puede estar vacío", exception.message)
    }

    @Test
    fun `create lanza BadRequestException cuando el email esta vacio`() {

        val badRequest = StudentRequest(name = "Alexander", email = "")


        val exception = assertThrows(BadRequestException::class.java) {
            service.create(badRequest)
        }

        assertEquals("El email del estudiante no puede estar vacío", exception.message)
    }



    @Test
    fun `getAll retorna la lista de estudiantes`() {

        val student = Student(id = 1L, name = "Alexander", email = "alex@puce.edu.ec")
        val response = StudentResponse(id = 1L, name = "Alexander", email = "alex@puce.edu.ec")

        `when`(repository.findAll()).thenReturn(listOf(student))
        `when`(mapper.toResponse(student)).thenReturn(response)


        val result = service.getAll()


        assertEquals(1, result.size)
        assertEquals(response, result[0])
        verify(repository, times(1)).findAll()
    }



    @Test
    fun `update modifica al estudiante cuando existe y los datos son validos`() {

        val id = 1L
        val request = StudentRequest(name = "Alexander Modificado", email = "modificado@puce.edu.ec")
        val existing = Student(id = id, name = "Alexander", email = "alex@puce.edu.ec")
        val updatedEntity = Student(id = id, name = "Alexander Modificado", email = "modificado@puce.edu.ec")
        val expectedResponse = StudentResponse(id = id, name = "Alexander Modificado", email = "modificado@puce.edu.ec")

        `when`(repository.findById(id)).thenReturn(Optional.of(existing))
        `when`(repository.save(any(Student::class.java))).thenReturn(updatedEntity)
        `when`(mapper.toResponse(updatedEntity)).thenReturn(expectedResponse)


        val result = service.update(id, request)


        assertEquals(expectedResponse, result)
        verify(repository, times(1)).findById(id)
        verify(repository, times(1)).save(any(Student::class.java))
    }

    @Test
    fun `update lanza ResourceNotFoundException cuando el estudiante no existe`() {

        val id = 99L
        val request = StudentRequest(name = "Alexander", email = "alex@puce.edu.ec")
        `when`(repository.findById(id)).thenReturn(Optional.empty())


        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.update(id, request)
        }

        assertEquals("Estudiante con ID $id no encontrado", exception.message)
    }

    @Test
    fun `update lanza BadRequestException cuando el nombre en request esta vacio`() {

        val id = 1L
        val request = StudentRequest(name = "", email = "alex@puce.edu.ec")
        val existing = Student(id = id, name = "Alexander", email = "alex@puce.edu.ec")

        `when`(repository.findById(id)).thenReturn(Optional.of(existing))

        val exception = assertThrows(BadRequestException::class.java) {
            service.update(id, request)
        }

        assertEquals("El nombre del estudiante no puede estar vacío", exception.message)
    }

    @Test
    fun `update lanza BadRequestException cuando el email en request esta vacio`() {

        val id = 1L
        val request = StudentRequest(name = "Alexander", email = "   ")
        val existing = Student(id = id, name = "Alexander", email = "alex@puce.edu.ec")

        `when`(repository.findById(id)).thenReturn(Optional.of(existing))


        val exception = assertThrows(BadRequestException::class.java) {
            service.update(id, request)
        }

        assertEquals("El email del estudiante no puede estar vacío", exception.message)
    }



    @Test
    fun `delete elimina al estudiante cuando el ID existe`() {

        val id = 1L
        `when`(repository.existsById(id)).thenReturn(true)


        service.delete(id)


        verify(repository, times(1)).existsById(id)
        verify(repository, times(1)).deleteById(id)
    }

    @Test
    fun `delete lanza ResourceNotFoundException cuando el ID no existe`() {

        val id = 99L
        `when`(repository.existsById(id)).thenReturn(false)

       
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            service.delete(id)
        }

        assertEquals("No se puede eliminar: Estudiante con ID $id no encontrado", exception.message)
        verify(repository, times(1)).existsById(id)
        verify(repository, times(0)).deleteById(any())
    }
}