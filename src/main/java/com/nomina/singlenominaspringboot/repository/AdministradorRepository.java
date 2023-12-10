package com.nomina.singlenominaspringboot.repository;

import com.nomina.singlenominaspringboot.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {

    Optional<Administrador> findByDniAndEmailAndContrasenya(String dni, String email, String contrasenya);
}
