package com.ecommerce.project.controller;

import com.ecommerce.project.modal.Address;
import com.ecommerce.project.modal.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AuthUtil authUtil;

    @Autowired
    AddressService addressService;
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        User user = authUtil.loggedInUser();
        AddressDTO createdAddressDTOS = addressService.createAddress(addressDTO,user);
        return new ResponseEntity<AddressDTO>(createdAddressDTOS, HttpStatus.CREATED);

    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddress() {
        List<AddressDTO> addressDTOList = addressService.getAddress();
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
        AddressDTO addressDTO = addressService.getAddressById(addressId);
        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/user/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddress() {
        User user = authUtil.loggedInUser();
        List<AddressDTO> addressDTOList = addressService.getUserAddress(user);
        return new ResponseEntity<>(addressDTOList, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId, @Valid @RequestBody AddressDTO addressDTO) {
//
        AddressDTO updatedAddressDTO = addressService.updateAddressById(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddressById(@PathVariable Long addressId) {
       String status = addressService.deleteAddressById(addressId);
        return new ResponseEntity<>(status ,HttpStatus.OK);
    }


}
