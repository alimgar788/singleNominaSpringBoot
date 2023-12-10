package com.nomina.singlenominaspringboot.service;


import com.nomina.singlenominaspringboot.model.Administrador;

import java.util.Optional;
public interface AdministradorServiceInterface {

    public Optional<Administrador> accederSesion(Administrador admin);

}
