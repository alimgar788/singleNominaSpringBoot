package com.nomina.singlenominaspringboot.repository;

import com.nomina.singlenominaspringboot.model.Empleado;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends CrudRepository<Empleado, String> {
    List<Empleado> findAllByEstadoEmpleadoIsTrue();

    List<Empleado> findAllByEstadoEmpleadoIsTrueAndCategoria(Integer categoria);

    List<Empleado> findAllByEstadoEmpleadoIsTrueAndNomina_Salario(Double salario);

    List<Empleado> findAllByEstadoEmpleadoIsTrueAndAnyos(Double anyos);

    List<Empleado> findAllByEstadoEmpleadoIsTrueAndNombreContainingIgnoreCase(String nombre);

    List<Empleado> findAllByEstadoEmpleadoIsTrueAndSexo(String sexo);

    List<Empleado> findAllByEstadoEmpleadoIsTrueAndDniContainingIgnoreCase(String dni);

    Optional<Empleado> findByDniAndEstadoEmpleadoIsTrue(String dni);

    @Override
    default void delete(Empleado empl) {
        empl.setEstadoEmpleado(false);
        this.save(empl);
    }
}
