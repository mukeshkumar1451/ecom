package com.ecommerce.project.modal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name must contain at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 3, message = "buildingName name must contain at least 3 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 3, message = "city name must contain at least 3 characters")
    private String city;

    @NotBlank
    @Size(min = 3, message = "state name must contain at least 3 characters")
    private String state;

    @NotBlank
    @Size(min = 3, message = "country name must contain at least 3 characters")
    private String country;

    @NotBlank
    @Size(min = 6, message = "pinCode name must contain at least 6 characters")
    private String pinCode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users= new ArrayList<>();

    public Address(String street, String buildingName, String city, String state, String country, String pinCode) {
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pinCode = pinCode;
    }
}
