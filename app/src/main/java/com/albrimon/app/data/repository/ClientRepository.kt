package com.albrimon.app.data.repository

import com.albrimon.app.domain.model.Client
import com.albrimon.app.domain.model.Measurements

// Datos de ejemplo. Más adelante este repositorio consultará la BD local (Room)
// y sincronizará con la API REST, como en el diagrama de secuencia.
object ClientRepository {
    val clients: List<Client> = listOf(
        Client(1, "Ana López", "S", 28, true, "55 1234 5678", Measurements(88, 68, 94, 60)),
        Client(2, "Valeria Torres", "M", 34, true, "55 1234 5678", Measurements(92, 72, 98, 62)),
        Client(3, "Sofía Ramírez", "L", 21, true, "55 2345 6789", Measurements(98, 78, 104, 64)),
        Client(4, "Daniela Cruz", "M", 40, true, "55 3456 7890", Measurements(94, 74, 100, 63)),
        Client(5, "Lucía Herrera", "S", 27, false, "55 4567 8901", Measurements(86, 66, 92, 59))
    )

    fun getById(id: Int): Client? = clients.firstOrNull { it.id == id }
}
