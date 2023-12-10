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

    @Override
    public Optional<Administrador> accederSesion(Administrador admin) {
        return administradorRepository.findByDniAndEmailAndContrasenya(admin.getDni(),admin.getEmail(), admin.getContrasenya());
    }
}
