package com.ecommerce.project.repositories;

import com.ecommerce.project.modal.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
