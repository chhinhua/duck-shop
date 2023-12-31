package com.hdshop.service.address;

import com.hdshop.dto.address.AddressDTO;
import com.hdshop.entity.Address;
import com.hdshop.entity.User;
import com.hdshop.exception.InvalidException;
import com.hdshop.exception.ResourceNotFoundException;
import com.hdshop.repository.AddressRepository;
import com.hdshop.service.user.UserService;
import com.hdshop.utils.PhoneNumberUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserService userService;
    private final MessageSource messageSource;
    private final ModelMapper modelMapper;

    @Override
    public List<AddressDTO> getAllAddressForUser(Principal principal) {
        String username = principal.getName();
        return addressRepository.findAllByUserUsernameAndIsDeletedIsFalse(username)
                .stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO addAddress(AddressDTO addressDTO, Principal principal) {
        validate(addressDTO);

        String username = principal.getName();
        User user = userService.findByUsername(username);

        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        address.setIsDefault(user.getAddresses().isEmpty());
        address.setIsDeleted(false);

        Address newAddress = addressRepository.save(address);

        return mapEntityToDTO(newAddress);
    }

    @Override
    public AddressDTO updateAddress(AddressDTO address, Long addressId) {
        Address existingAddress = findById(addressId);

        // Cập nhật các trường từ addressDTO vào existingAddress
        existingAddress.setFullName(address.getFullName());
        existingAddress.setPhoneNumber(address.getPhoneNumber());
        existingAddress.setCity(address.getCity());
        existingAddress.setDistrict(address.getDistrict());
        existingAddress.setWard(address.getWard());
        existingAddress.setOrderDetails(address.getOrderDetails());

        // Lưu cập nhật vào cơ sở dữ liệu
        Address updatedAddress = addressRepository.save(existingAddress);

        return mapEntityToDTO(updatedAddress);
    }

    @Override
    public AddressDTO getOne(Long addressId) {
        Address address = findById(addressId);
        return mapEntityToDTO(address);
    }

    @Override
    public List<AddressDTO> setDefault(Long addressId, Principal principal) {
        String username = principal.getName();

        // find the address
        Address newDefaultAddress = findById(addressId);

        User user = userService.findByUsername(username);

        // check the user's current default address
        Address currentDefaultAddress = user.getAddresses().stream()
                .filter(Address::getIsDefault)
                .findFirst()
                .orElse(null);

        if (currentDefaultAddress != null) {
            // cancel the current default address
            currentDefaultAddress.setIsDefault(false);
            addressRepository.save(currentDefaultAddress);
        }

        // set default
        newDefaultAddress.setIsDefault(true);
        addressRepository.save(newDefaultAddress);

        return addressRepository.findAllByUserUsernameAndIsDeletedIsFalse(username)
                .stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String delete(Long addressId, Principal principal) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("address-not-found")));
        address.setIsDeleted(true);
        addressRepository.save(address);

        return getMessage("deleted-successfully");
    }

    @Override
    public Address findById(Long addressId) {
        return addressRepository.findById(addressId).orElseThrow(() ->
                new ResourceNotFoundException(getMessage("retrieving-address-information-failed"))
        );
    }

    private void validate(AddressDTO address) {
        if (address.getFullName().isBlank()) {
            throw new InvalidException(getMessage("fullname-must-not-be-empty"));
        }
        if (address.getPhoneNumber().isBlank()) {
            throw new InvalidException(getMessage("phone-number-must-not-be-empty"));
        }
        if (!PhoneNumberUtils.isValidPhoneNumber(address.getPhoneNumber())) {
            throw new InvalidException(getMessage("invalid-phone-number"));
        }
        if (address.getCity().isBlank()) {
            throw new InvalidException(getMessage("city-must-not-be-empty"));
        }
        if (address.getDistrict().isBlank()) {
            throw new InvalidException(getMessage("district-must-not-be-empty"));
        }
        if (address.getWard().isBlank()) {
            throw new InvalidException(getMessage("ward-must-not-be-empty"));
        }
    }

    private AddressDTO mapEntityToDTO(Address address) {
        return modelMapper.map(address, AddressDTO.class);
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
