package com.nomina.singlenominaspringboot.exception;
/**
 * La clase DatosNoCorrectosException representa una excepción lanzada cuando se encuentran datos que no son correctos.
 */
public class DatosNoCorrectosException extends Throwable{
    /**
     * Construye una nueva excepción con el mensaje especificado y lo imprime en la consola.
     *
     * @param s El mensaje de la excepción.
     */
    public DatosNoCorrectosException(String s) {
        super(s);
    }
}