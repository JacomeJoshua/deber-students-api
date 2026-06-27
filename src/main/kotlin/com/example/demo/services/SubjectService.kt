package com.example.demo.services

import com.example.demo.dtos.SubjectRequest
import com.example.demo.dtos.SubjectResponse
import com.example.demo.mappers.SubjectMapper
import com.example.demo.entities.Subject
import com.example.demo.repositories.ProfessorRepository
import com.example.demo.repositories.SubjectRepository
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.exceptions.BadRequestException 
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val repository: SubjectRepository,
    private val profRepository: ProfessorRepository,
    private val mapper: SubjectMapper
) {
    fun create(req: SubjectRequest): SubjectResponse {
        if (req.name.isNullOrBlank()) throw BadRequestException("El nombre de la materia no puede estar vacío")
        if (req.code.isNullOrBlank()) throw BadRequestException("El código de la materia no puede estar vacío")

        val prof = profRepository.findById(req.professorId)
            .orElseThrow { ResourceNotFoundException("Profesor con ID ${req.professorId} no encontrado") }

        return mapper.toResponse(repository.save(mapper.toEntity(req, prof)))
    }

    fun getAll() = repository.findAll().map { mapper.toResponse(it) }

    fun getById(id: Long): SubjectResponse {
        val subject = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Materia con ID $id no encontrada") }
        return mapper.toResponse(subject)
    }

    fun update(id: Long, req: SubjectRequest): SubjectResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("Materia con ID $id no encontrada") }

        if (req.name.isNullOrBlank()) throw BadRequestException("El nombre de la materia no puede estar vacío")
        if (req.code.isNullOrBlank()) throw BadRequestException("El código de la materia no puede estar vacío")

        val prof = profRepository.findById(req.professorId)
            .orElseThrow { ResourceNotFoundException("Profesor con ID ${req.professorId} no encontrado") }

        val updated = Subject(id = existing.id, name = req.name, code = req.code, professor = prof)
        return mapper.toResponse(repository.save(updated))
    }

    fun delete(id: Long) {
        if (!repository.existsById(id))
            throw ResourceNotFoundException("No se puede eliminar: Materia con ID $id no encontrada")
        repository.deleteById(id)
    }
}