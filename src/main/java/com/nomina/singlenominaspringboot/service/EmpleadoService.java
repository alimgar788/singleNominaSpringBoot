package com.nomina.singlenominaspringboot.service;

import com.nomina.singlenominaspringboot.model.Empleado;
import com.nomina.singlenominaspringboot.model.Nomina;
import com.nomina.singlenominaspringboot.repository.EmpleadoRepository;
import com.nomina.singlenominaspringboot.repository.NominaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class EmpleadoService implements EmpleadoServiceInterface {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private NominaRepository nominaRepository;

    @Autowired
    private Validator validador;

    @Override
    public List<Empleado> obtenerListaEmpleados() {
        return empleadoRepository.findAllByEstadoEmpleadoIsTrue();
    }

    @Override
    public List<Empleado> obtenerListaEmpleados(String campo, Object valor) {
        if (campo == null || valor == null || valor.equals("")) {
            return empleadoRepository.findAllByEstadoEmpleadoIsTrue();
        } else if (campo.equals("sexo")) {
            return empleadoRepository.findAllByEstadoEmpleadoIsTrueAndSexo(valor.toString());
        } else if (campo.equals("dni")) {
            return empleadoRepository.findAllByEstadoEmpleadoIsTrueAndDniContainingIgnoreCase(valor.toString());
        } else if (campo.equals("nombre")) {
            return empleadoRepository.findAllByEstadoEmpleadoIsTrueAndNombreContainingIgnoreCase(valor.toString());
        } else if (campo.equals("categoria")) {
            int valorNormalizado = Integer.parseInt(valor.toString().replace(",", "."));
            return empleadoRepository.findAllByEstadoEmpleadoIsTrueAndCategoria(valorNormalizado);
        } else if (campo.equals("salario")) {
            return empleadoRepository.findAllByEstadoEmpleadoIsTrueAndNomina_Salario(Double.parseDouble(valor.toString()));
        } else if (campo.equals("anyos")) {
            String valorNormalizado = valor.toString().replace(",", ".");
            double valorDouble = Double.parseDouble(valorNormalizado);
            return empleadoRepository.findAllByEstadoEmpleadoIsTrueAndAnyos(valorDouble);
        }
        return empleadoRepository.findAllByEstadoEmpleadoIsTrue();
    }

    @Override
    public Empleado actualizaEmpleado(Empleado empl) throws RuntimeException {

        Set<ConstraintViolation<Empleado>> errores = validador.validate(empl);
        if (errores.isEmpty()) {
            empl.setEstadoEmpleado(true);
            Empleado empleado = empleadoRepository.save(empl);
            Optional<Nomina> nominaResult = nominaRepository.findByEmpleado_Dni(empl.getDni());
            Nomina nomina = null;
            if (nominaResult.isEmpty()) {
                nomina = new Nomina();
                nomina.setEmpleado(empl);
            } else {
                nomina = nominaResult.get();
            }
            nomina.actualizaSueldo();
            nominaRepository.save(nomina);
            return empleado;
        } else {
            String mensaje = "";
            for (ConstraintViolation error : errores) {
                mensaje += " " + error.getMessageTemplate() + ",";
            }
            throw new RuntimeException(mensaje.substring(0, mensaje.length() - 1));
        }
    }

    @Override
    public Empleado registraEmpleado(Empleado empl) throws RuntimeException {
        Optional<Empleado> result = this.obtenerEmpleadoPorDni(empl.getDni());
        if (result.isEmpty() || !result.get().getEstadoEmpleado()) {
            return this.actualizaEmpleado(empl);
        } else {
            throw new RuntimeException("El DNI ya existe para un empleado registrado");
        }
    }

    @Override
    public Optional<Empleado> obtenerEmpleadoPorDni(String dni) {
        return empleadoRepository.findByDniAndEstadoEmpleadoIsTrue(dni);
    }

    @Override
    public Empleado eliminarEmpleado(String dni) throws RuntimeException {
        Optional<Empleado> empl = empleadoRepository.findById(dni);
        if (!empl.isEmpty()) {
            empleadoRepository.delete(empl.get());
            return empl.get();
        } else {
            throw new RuntimeException("No puede eliminar un empleado que no existe");
        }

    }

    @Override
    public Double obtenerSalarioPorDni(String dni) throws RuntimeException {
        Optional<Empleado> result = empleadoRepository.findByDniAndEstadoEmpleadoIsTrue(dni);
        if (!result.isEmpty()) {
            return result.get().getNomina().getSalario();
        } else {
            throw new RuntimeException("El DNI introducido no se encuentra registrado en la base de datos");
        }
    }

}
