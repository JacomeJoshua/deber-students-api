package com.example.demo.service

import com.example.demo.dto.ProfessorRequest
import com.example.demo.dto.ProfessorResponse
import com.example.demo.Mappers.ProfessorMapper
import com.example.demo.entity.Professor
import com.example.demo.repository.ProfessorRepository
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.exceptions.BadRequestException 
import org.springframework.stereotype.Service

@Service
class ProfessorService(
    private val repository: ProfessorRepository,
    private val mapper: ProfessorMapper
) {
    fun create(req: ProfessorRequest): ProfessorResponse {
        if (req.name.isNullOrBlank()) throw BadRequestException("El nombre del profesor no puede estar vacío")
        if (req.email.isNullOrBlank()) throw BadRequestException("El email del profesor no puede estar vacío")

        return mapper.toResponse(repository.save(mapper.toEntity(req)))
    }

    fun getAll() = repository.findAll().map { mapper.toResponse(it) }

    fun getById(id: Long): ProfessorResponse {
        val prof = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Profesor con ID $id no encontrado") }
        return mapper.toResponse(prof)
    }

    fun update(id: Long, req: ProfessorRequest): ProfessorResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Profesor con ID $id no encontrado") }

        if (req.name.isNullOrBlank()) throw BadRequestException("El nombre del profesor no puede estar vacío")
        if (req.email.isNullOrBlank()) throw BadRequestException("El email del profesor no puede estar vacío")

        val updated = Professor(id = existing.id, name = req.name, email = req.email)
        return mapper.toResponse(repository.save(updated))
    }

    fun delete(id: Long) {
        if (!repository.existsById(id))
            throw ResourceNotFoundException("No se puede eliminar: Profesor con ID $id no encontrado")
        repository.deleteById(id)
    }
}