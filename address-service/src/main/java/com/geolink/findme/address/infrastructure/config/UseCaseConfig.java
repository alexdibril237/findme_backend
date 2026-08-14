package com.geolink.findme.address.infrastructure.config;

import com.geolink.findme.address.application.usecase.CreateAddressUseCase;
import com.geolink.findme.address.application.usecase.DeleteAddressUseCase;
import com.geolink.findme.address.application.usecase.ExportAddressUseCase;
import com.geolink.findme.address.application.usecase.GetAddressUseCase;
import com.geolink.findme.address.application.usecase.ListAddressesForAdminUseCase;
import com.geolink.findme.address.application.usecase.ListMyAddressesUseCase;
import com.geolink.findme.address.application.usecase.UpdateAddressUseCase;
import com.geolink.findme.address.application.usecase.UploadAddressPhotoUseCase;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import com.geolink.findme.address.domain.port.PhotoStoragePort;
import com.geolink.findme.address.domain.port.QrCodeGeneratorPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateAddressUseCase createAddressUseCase(AddressRepositoryPort addressRepository) {
        return new CreateAddressUseCase(addressRepository);
    }

    @Bean
    public ListMyAddressesUseCase listMyAddressesUseCase(AddressRepositoryPort addressRepository) {
        return new ListMyAddressesUseCase(addressRepository);
    }

    @Bean
    public GetAddressUseCase getAddressUseCase(AddressRepositoryPort addressRepository) {
        return new GetAddressUseCase(addressRepository);
    }

    @Bean
    public UpdateAddressUseCase updateAddressUseCase(AddressRepositoryPort addressRepository) {
        return new UpdateAddressUseCase(addressRepository);
    }

    @Bean
    public DeleteAddressUseCase deleteAddressUseCase(AddressRepositoryPort addressRepository) {
        return new DeleteAddressUseCase(addressRepository);
    }

    @Bean
    public UploadAddressPhotoUseCase uploadAddressPhotoUseCase(AddressRepositoryPort addressRepository,
                                                                 PhotoStoragePort photoStorage) {
        return new UploadAddressPhotoUseCase(addressRepository, photoStorage);
    }

    @Bean
    public ExportAddressUseCase exportAddressUseCase(AddressRepositoryPort addressRepository,
                                                       QrCodeGeneratorPort qrCodeGenerator) {
        return new ExportAddressUseCase(addressRepository, qrCodeGenerator);
    }

    @Bean
    public ListAddressesForAdminUseCase listAddressesForAdminUseCase(AddressRepositoryPort addressRepository) {
        return new ListAddressesForAdminUseCase(addressRepository);
    }
}
