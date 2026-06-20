package com.example.demo.service

import com.example.demo.dto.EnrollmentRequest
import com.example.demo.dto.EnrollmentResponse
import com.example.demo.entity.Enrollment
import com.example.demo.Mappers.EnrollmentMapper
import com.example.demo.repository.*
import com.example.demo.exceptions.ResourceNotFoundException
import com.example.demo.exceptions.BadRequestException
import org.springframework.stereotype.Service

@Service
class EnrollmentService(
    private val repo: EnrollmentRepository,
    private val sRepo: StudentRepository,
    private val subRepo: SubjectRepository,
    private val mapper: EnrollmentMapper
) {
    fun enroll(req: EnrollmentRequest): EnrollmentResponse {
        val s = sRepo.findById(req.studentId)
            .orElseThrow { ResourceNotFoundException("Estudiante con ID ${req.studentId} no encontrado") }

        val sub = subRepo.findById(req.subjectId)
            .orElseThrow { ResourceNotFoundException("Materia con ID ${req.subjectId} no encontrada") }

        return mapper.toResponse(repo.save(Enrollment(student = s, subject = sub)))
    }

    fun getAll() = repo.findAll().map { mapper.toResponse(it) }

    fun getById(id: Long): EnrollmentResponse {
        val enrollment = repo.findById(id)
            .orElseThrow { ResourceNotFoundException("Inscripción con ID $id no encontrada") }
        return mapper.toResponse(enrollment)
    }

    fun updateStatus(id: Long, newStatus: String): EnrollmentResponse {
        if (newStatus.isNullOrBlank()) throw BadRequestException("El nuevo estado no puede estar vacío")

        val e = repo.findById(id)
            .orElseThrow { ResourceNotFoundException("Inscripción con ID $id no encontrada") }

        e.status = newStatus
        return mapper.toResponse(repo.save(e))
    }

    fun delete(id: Long) {
        if (!repo.existsById(id))
            throw ResourceNotFoundException("No se puede eliminar: Inscripción con ID $id no encontrada")
        repo.deleteById(id)
    }
}