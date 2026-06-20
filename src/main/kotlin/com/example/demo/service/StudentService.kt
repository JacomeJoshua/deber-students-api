package com.example.demo.service

import com.example.demo.dto.StudentRequest
import com.example.demo.dto.StudentResponse
import com.example.demo.Mappers.StudentMapper
import com.example.demo.entity.Student
import com.example.demo.repository.StudentRepository
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.exceptions.BadRequestException 
import org.springframework.stereotype.Service

@Service
class StudentService(
    private val repository: StudentRepository,
    private val mapper: StudentMapper
) {
    fun create(req: StudentRequest): StudentResponse {
        if (req.name.isNullOrBlank()) throw BadRequestException("El nombre del estudiante no puede estar vacío")
        if (req.email.isNullOrBlank()) throw BadRequestException("El email del estudiante no puede estar vacío")

        return mapper.toResponse(repository.save(mapper.toEntity(req)))
    }

    fun getAll() = repository.findAll().map { mapper.toResponse(it) }

    fun getById(id: Long): StudentResponse {
        val student = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Estudiante con ID $id no encontrado") }
        return mapper.toResponse(student)
    }

    fun update(id: Long, req: StudentRequest): StudentResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Estudiante con ID $id no encontrado") }

        if (req.name.isNullOrBlank()) throw BadRequestException("El nombre del estudiante no puede estar vacío")
        if (req.email.isNullOrBlank()) throw BadRequestException("El email del estudiante no puede estar vacío")

        val updated = Student(id = existing.id, name = req.name, email = req.email)
        return mapper.toResponse(repository.save(updated))
    }

    fun delete(id: Long) {
        if (!repository.existsById(id))
            throw ResourceNotFoundException("No se puede eliminar: Estudiante con ID $id no encontrado")
        repository.deleteById(id)
    }
}