package com.nomina.singlenominaspringboot.model;

import com.nomina.singlenominaspringboot.exception.DatosNoCorrectosException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "empleados")
public class Empleado {
    @Id
    @Pattern(regexp = "\\d{8}[A-Z]", message = "El DNI debe tener 8 dígitos seguidos de una letra mayúscula")
    private String dni;

    @NotBlank(message = "El nombre no puede estar en blanco")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String nombre;

    @Pattern(regexp = "[MFI]", message = "El sexo debe ser 'M', 'F' o 'I'")
    private String sexo;

    @Min(value = 1, message = "La categoría debe ser al menos 1")
    @Max(value = 9, message = "La categoría no puede ser mayor que 9")
    private Integer categoria;

    @Min(value = 0, message = "Los años deben ser al menos 0")
    // Presuponemos que tiene una jubilación máxima a los 70, con lo cual 70 - 16 = 54
    @Max(value = 54, message = "Los años deben cómo máximo 54")
    private Double anyos;

    @ColumnDefault("true")
    private Boolean estadoEmpleado;

    @OneToOne(mappedBy = "empleado")
    @PrimaryKeyJoinColumn
    private Nomina nomina;

    /**
     * Constructor con todos los parámetros heredados de la clase Persona.
     * Por defecto se le asigna la categoría 1 y los años de antigüedad en 0
     *
     * @param nombre
     * @param dni
     * @param sexo
     */
    public Empleado(String nombre, String dni, String sexo) {
        this.nombre = nombre;
        this.dni = dni;
        this.sexo = sexo;
        this.categoria = 1;
        this.anyos = 0.0;
        this.estadoEmpleado = true;
    }

    /**
     * Constructor con todos los parámetros. En este caso, en este constructor se controla que la asignación de los años
     * de antiguedad sean siempre igual o superior a 0.
     *
     * @param nombre
     * @param dni
     * @param sexo
     * @param categoria
     * @param anyos
     * @throws DatosNoCorrectosException
     */
    public Empleado(String nombre, String dni, String sexo, int categoria, double anyos) throws DatosNoCorrectosException {
        this.nombre = nombre;
        this.dni = dni;
        this.sexo = sexo;
        this.categoria = categoria;
        this.estadoEmpleado = true;
        if (anyos < 0) {
            throw new DatosNoCorrectosException("El anyo debe ser un numero superior a 0");
        }
        this.anyos = anyos;
    }

    /**
     * Método que establece la categoría del empleado, en este método se controla que la categoría se encuentre
     * entre las categorías permitidas.
     *
     * @param categoria
     * @throws DatosNoCorrectosException
     */
    public void setCategoria(int categoria) throws DatosNoCorrectosException {
        if (categoria >= 1 && categoria <= 10) {
            this.categoria = categoria;
        } else {
            throw new DatosNoCorrectosException("La categoría debe estar entre 1 y 10");
        }
    }

    public void setDni(String dni) {
        this.dni = dni.toUpperCase();
    }

    public String getSexoFormato() {
        String sexo = getSexo().toLowerCase();
        if (sexo.equals("f")) {
            return "Femenino";
        } else if (sexo.equals("m")) {
            return "Masculino";
        } else {
            return "N/C";
        }
    }
}
