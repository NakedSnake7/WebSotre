
//src/main/java/com/WeedTitlan/server/dto/CustomerDTO.java

package com.WeedTitlan.server.dto;

import jakarta.validation.constraints.Email;  
import jakarta.validation.constraints.NotBlank;

public class CustomerDTO {
	@NotBlank(message = "El nombre completo es obligatorio")
 private String fullName;
    @Email(message = "Debe proporcionar un correo electrónico válido")
    @NotBlank(message = "El correo electrónico es obligatorio")
 private String email;
   @NotBlank(message = "El teléfono no puede estar vacío")
 private String phone;
   @NotBlank(message = "La dirección no puede estar vacía")
 private String address;

 // Getters y setters
 public String getFullName() {
     return fullName;
 }

 public void setFullName(String fullName) {
     this.fullName = fullName;
 }

 public String getEmail() {
     return email;
 }

 public void setEmail(String email) {
     this.email = email;
 }

 public String getPhone() {
     return phone;
 }

 public void setPhone(String phone) {
     this.phone = phone;
 }

 public String getAddress() {
     return address;
 }

 public void setAddress(String address) {
     this.address = address;
 }
}
