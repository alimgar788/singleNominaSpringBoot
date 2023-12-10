package com.nomina.singlenominaspringboot.repository;

import com.nomina.singlenominaspringboot.model.Nomina;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NominaRepository extends CrudRepository<Nomina, String> {

    Optional<Nomina> findByEmpleado_Dni(String dni);

}
