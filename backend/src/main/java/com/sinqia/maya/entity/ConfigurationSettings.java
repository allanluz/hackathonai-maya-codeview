package com.sinqia.maya.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade para armazenamento de configura��es do sistema MAYA.
 * 
 * Permite configura��o flex�vel de par�metros como limites de complexidade,
 * severidade de issues, configura��es de integra��o, etc.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Entity
@Table(name = "configuration_settings", indexes = {
    @Index(name = "idx_key_name", columnList = "key_name", unique = true),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_data_type", columnList = "data_type")
})
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfigurationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Chave �nica da configura��o
     */
    @Column(name = "key_name", nullable = false, unique = true, length = 100)
    private String keyName;

    /**
     * Valor da configura��o
     */
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    /**
     * Tipo de dados do valor
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private DataType dataType = DataType.STRING;

    /**
     * Categoria da configura��o
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Descri��o da configura��o
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Indica se � uma configura��o sens�vel (senha, token, etc.)
     */
    @Column(name = "is_sensitive")
    private Boolean isSensitive = false;

    /**
     * Indica se � uma configura��o obrigat�ria
     */
    @Column(name = "is_required")
    private Boolean isRequired = false;

    /**
     * Indica se a configura��o est� ativa
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Valor padr�o da configura��o
     */
    @Column(name = "default_value", length = 500)
    private String defaultValue;

    /**
     * Data de cria��o
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Data da �ltima atualiza��o
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Tipos de dados suportados
     */
    public enum DataType {
        STRING("String"),
        INTEGER("Integer"),
        DOUBLE("Double"),
        BOOLEAN("Boolean"),
        JSON("JSON"),
        EMAIL("Email"),
        URL("URL"),
        PASSWORD("Password");

        private final String description;

        DataType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Constructor padr�o
     */
    public ConfigurationSettings() {}

    /**
     * Constructor com campos principais
     */
    public ConfigurationSettings(String keyName, String value, DataType dataType, String category) {
        this.keyName = keyName;
        this.value = value;
        this.dataType = dataType;
        this.category = category;
    }

    /**
     * Constructor completo
     */
    public ConfigurationSettings(String keyName, String value, DataType dataType, 
                               String category, String description, Boolean isSensitive) {
        this.keyName = keyName;
        this.value = value;
        this.dataType = dataType;
        this.category = category;
        this.description = description;
        this.isSensitive = isSensitive;
    }

    /**
     * Obter valor como String
     */
    public String getStringValue() {
        return value;
    }

    /**
     * Obter valor como Integer
     */
    public Integer getIntegerValue() {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obter valor como Double
     */
    public Double getDoubleValue() {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Obter valor como Boolean
     */
    public Boolean getBooleanValue() {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Definir valor a partir de Object
     */
    public void setValue(Object objectValue) {
        if (objectValue == null) {
            this.value = null;
            return;
        }
        this.value = objectValue.toString();
    }

    /**
     * Obter valor exib�vel (mascarar senhas)
     */
    public String getDisplayValue() {
        if (Boolean.TRUE.equals(isSensitive) && value != null && !value.trim().isEmpty()) {
            return "****";
        }
        return value;
    }

    /**
     * Validar se o valor est� conforme o tipo de dados
     */
    public boolean isValidValue() {
        if (value == null || value.trim().isEmpty()) {
            return !Boolean.TRUE.equals(isRequired);
        }

        return switch (dataType) {
            case INTEGER -> getIntegerValue() != null;
            case DOUBLE -> getDoubleValue() != null;
            case BOOLEAN -> value.trim().equalsIgnoreCase("true") || 
                           value.trim().equalsIgnoreCase("false");
            case EMAIL -> value.contains("@") && value.contains(".");
            case URL -> value.startsWith("http://") || value.startsWith("https://");
            case JSON -> isValidJson();
            default -> true; // STRING, PASSWORD s�o sempre v�lidos
        };
    }

    /**
     * Validar se o valor � um JSON v�lido
     */
    private boolean isValidJson() {
        try {
            // Valida��o simples - em implementa��o real usaria Jackson
            return value.trim().startsWith("{") && value.trim().endsWith("}") ||
                   value.trim().startsWith("[") && value.trim().endsWith("]");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Criar configura��o de sistema padr�o
     */
    public static ConfigurationSettings createSystemConfig(String key, String value, 
                                                          String category, String description) {
        ConfigurationSettings config = new ConfigurationSettings();
        config.setKeyName(key);
        config.setValue(value);
        config.setDataType(DataType.STRING);
        config.setCategory(category);
        config.setDescription(description);
        config.setIsSensitive(false);
        config.setIsRequired(false);
        return config;
    }

    /**
     * Criar configura��o sens�vel
     */
    public static ConfigurationSettings createSensitiveConfig(String key, String value, 
                                                            String category, String description) {
        ConfigurationSettings config = createSystemConfig(key, value, category, description);
        config.setDataType(DataType.PASSWORD);
        config.setIsSensitive(true);
        return config;
    }

    /**
     * Criar configura��o booleana
     */
    public static ConfigurationSettings createBooleanConfig(String key, Boolean value, 
                                                          String category, String description) {
        ConfigurationSettings config = createSystemConfig(key, value.toString(), category, description);
        config.setDataType(DataType.BOOLEAN);
        return config;
    }

    /**
     * Criar configura��o num�rica
     */
    public static ConfigurationSettings createNumericConfig(String key, Number value, 
                                                          String category, String description) {
        ConfigurationSettings config = createSystemConfig(key, value.toString(), category, description);
        config.setDataType(value instanceof Integer ? DataType.INTEGER : DataType.DOUBLE);
        return config;
    }

    /**
     * Get config value (alias for getValue)
     */
    public String getConfigValue() {
        return this.value;
    }

    /**
     * Set config value (alias for setValue)
     */
    public void setConfigValue(String configValue) {
        this.value = configValue;
    }

    /**
     * Set config key (alias for setKeyName)
     */
    public void setConfigKey(String configKey) {
        this.keyName = configKey;
    }

    /**
     * Set active status
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
