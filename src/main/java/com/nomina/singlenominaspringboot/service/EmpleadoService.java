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

    /**
     * Obtiene una lista de empleados activos.
     *
     * @return Una lista de objetos Empleado cuyo estado de empleado es verdadero.
     */
    @Override
    public List<Empleado> obtenerListaEmpleados() {
        return empleadoRepository.findAllByEstadoEmpleadoIsTrue();
    }

    /**
     * Obtiene una lista filtrada de empleados activos basada en un campo y su valor asociado.
     *
     * @param campo Campo por el cual se va a filtrar la lista de empleados.
     * @param valor Valor asociado al campo para realizar el filtro.
     * @return Una lista de objetos Empleado filtrada por el campo y valor especificados.
     */
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

    /**
     * Actualiza la información de un empleado en la base de datos, realizando validaciones y actualizando su estado y nómina si es necesario.
     *
     * @param empl El objeto Empleado con la información actualizada a guardar.
     * @return El objeto Empleado actualizado en la base de datos.
     * @throws RuntimeException Si se encuentran errores de validación al actualizar o al manipular la información de la nómina.
     */
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

    /**
     * Registra un nuevo empleado en la base de datos o actualiza la información si el empleado ya existe o está inactivo.
     *
     * @param empl El objeto Empleado a registrar o actualizar.
     * @return El objeto Empleado registrado o actualizado en la base de datos.
     * @throws RuntimeException Si el DNI ya está asociado a un empleado registrado o si hay errores al actualizar la información del empleado.
     */
    @Override
    public Empleado registraEmpleado(Empleado empl) throws RuntimeException {
        Optional<Empleado> result = this.obtenerEmpleadoPorDni(empl.getDni());
        if (result.isEmpty() || !result.get().getEstadoEmpleado()) {
            return this.actualizaEmpleado(empl);
        } else {
            throw new RuntimeException("El DNI ya existe para un empleado registrado");
        }
    }

    /**
     * Busca un empleado activo por su número de identificación (DNI) en el repositorio de empleados.
     *
     * @param dni El número de identificación (DNI) del empleado a buscar.
     * @return Un Optional que puede contener al empleado activo si se encuentra por su DNI, o vacío si no existe.
     */
    @Override
    public Optional<Empleado> obtenerEmpleadoPorDni(String dni) {
        return empleadoRepository.findByDniAndEstadoEmpleadoIsTrue(dni);
    }

    /**
     * Elimina un empleado de la base de datos según su número de identificación (DNI).
     *
     * @param dni El número de identificación (DNI) del empleado a eliminar.
     * @return El objeto Empleado eliminado de la base de datos.
     * @throws RuntimeException Si se intenta eliminar un empleado que no existe en la base de datos.
     */
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

    /**
     * Obtiene el salario de un empleado por su número de identificación (DNI).
     *
     * @param dni El número de identificación (DNI) del empleado del cual se desea obtener el salario.
     * @return El salario del empleado correspondiente al DNI proporcionado.
     * @throws RuntimeException Si el DNI no está registrado en la base de datos o si hay problemas al acceder al salario.
     */
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
