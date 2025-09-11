package com.sinqia.maya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Aplicação principal do Sistema MAYA Code Review
 * 
 * Sistema de análise automatizada de código focado nos padrões Sinqia,
 * com detecção especializada de vazamentos de conexão e integração com IA.
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
        System.out.println("?? Sistema de análise automatizada de código Sinqia");
        System.out.println("?? Detecta vazamentos de conexão e problemas de qualidade");
        
        SpringApplication.run(MayaCodeReviewApplication.class, args);
        
        System.out.println("? MAYA Code Review System iniciado com sucesso!");
        System.out.println("?? Acesse: http://localhost:8081/swagger-ui.html");
    }
}
