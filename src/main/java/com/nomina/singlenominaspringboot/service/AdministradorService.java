package com.nomina.singlenominaspringboot.service;

import com.nomina.singlenominaspringboot.model.Administrador;
import com.nomina.singlenominaspringboot.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdministradorService implements AdministradorServiceInterface {

    @Autowired
    private  AdministradorRepository  administradorRepository;

    /**
     * Accede a la sesión de un administrador comparando el DNI, email y contraseña proporcionados.
     *
     * @param admin Objeto Administrador con los datos de sesión a verificar.
     * @return Un Optional que puede contener el administrador si se encuentra en la sesión, o vacío si no.
     */

    @Override
    public Optional<Administrador> accederSesion(Administrador admin) {
        return administradorRepository.findByDniAndEmailAndContrasenya(admin.getDni(),admin.getEmail(), admin.getContrasenya());
    }
}
