package com.nomina.singlenominaspringboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "nominas")
public class Nomina {

    private static final int SUELDO_BASE[] = {50000, 70000, 90000, 110000, 130000, 150000, 170000, 190000, 210000, 230000};

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "dni")
    private Empleado empleado;

    @Min(value = 0, message = "El salario debe ser al menos 0")
    private Double salario;

    /**
     * Método que genera el sueldo del empleado en función a su categoría y antigüedad
     *
     * @return double
     */
    public double calculaSueldo() {
        return SUELDO_BASE[this.getEmpleado().getCategoria() - 1] + 5000 * this.getEmpleado().getAnyos();
    }

    public void actualizaSueldo() {
        this.setSalario(this.calculaSueldo());
    }
}
