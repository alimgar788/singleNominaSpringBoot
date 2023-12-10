package com.nomina.singlenominaspringboot.exception;
/**
 * La clase CierreRecursosException representa una excepción lanzada al producirse un error durante el cierre de recursos.
 */
public class CierreRecursosException extends Throwable {
    /**
     * Construye una nueva excepción con el mensaje especificado.
     *
     * @param s El mensaje de la excepción.
     */
    public CierreRecursosException(String s) {
        super(s);
    }
}
