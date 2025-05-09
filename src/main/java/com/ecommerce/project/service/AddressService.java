package com.ecommerce.project.service;

import com.ecommerce.project.modal.User;
import com.ecommerce.project.payload.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);


    List<AddressDTO> getAddress();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getUserAddress(User user);

    AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO);

    String deleteAddressById(Long addressId);
}
