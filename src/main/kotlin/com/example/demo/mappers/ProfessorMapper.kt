package com.example.demo.mappers

import com.example.demo.dtos.ProfessorRequest
import com.example.demo.dtos.ProfessorResponse
import com.example.demo.entities.Professor
import org.springframework.stereotype.Component

@Component
class ProfessorMapper {
    fun toEntity(req: ProfessorRequest) = Professor(name = req.name, email = req.email)

    fun toResponse(prof: Professor) = ProfessorResponse(
        id = prof.id,
        name = prof.name,
        email = prof.email
    )
}