package com.nomina.singlenominaspringboot.service;

import com.nomina.singlenominaspringboot.model.Empleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoServiceInterface {
    public Empleado actualizaEmpleado(Empleado empl);

    public Empleado registraEmpleado(Empleado empl);

    public Optional<Empleado> obtenerEmpleadoPorDni(String dni);

    public Empleado eliminarEmpleado(String dni);

    public List<Empleado> obtenerListaEmpleados();

    public List<Empleado> obtenerListaEmpleados(String campo, Object valor);

    public Double obtenerSalarioPorDni(String dni);

}
