package com.WeedTitlan.server;

import jakarta.persistence.Entity; 
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

@Entity
@Table(name = "users") // Especifica un nombre distinto para la tabla
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String name;

    @Email(message = "El correo electrónico no es válido")
    @NotEmpty(message = "El correo no puede estar vacío")
    private String email;

    // Validación mejorada para la contraseña
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$", 
             message = "La contraseña debe tener al menos 8 caracteres, con una mayúscula, un número y un carácter especial.")
    @NotEmpty(message = "La contraseña no puede estar vacía")
    private String password;

    // Constructor vacío necesario para JPA
    public User() {}

    // Constructor con parámetros
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Método toString para facilitar la depuración (sin datos sensibles)
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "'}";
    }

    // Método equals y hashCode para comparaciones
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
