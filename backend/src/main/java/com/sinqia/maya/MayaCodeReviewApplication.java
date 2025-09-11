package com.sinqia.maya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Aplica��o principal do Sistema MAYA Code Review
 * 
 * Sistema de an�lise automatizada de c�digo focado nos padr�es Sinqia,
 * com detec��o especializada de vazamentos de conex�o e integra��o com IA.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class MayaCodeReviewApplication {

    public static void main(String[] args) {
        System.out.println("?? Iniciando MAYA Code Review System...");
        System.out.println("?? Sistema de an�lise automatizada de c�digo Sinqia");
        System.out.println("?? Detecta vazamentos de conex�o e problemas de qualidade");
        
        SpringApplication.run(MayaCodeReviewApplication.class, args);
        
        System.out.println("? MAYA Code Review System iniciado com sucesso!");
        System.out.println("?? Acesse: http://localhost:8081/swagger-ui.html");
    }
}
