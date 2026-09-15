package com.geolink.findme.unit.presentation.mapper;
import com.geolink.findme.presentation.mapper.AddressWebMapper;

import com.geolink.findme.business.model.AddressStatus;
import com.geolink.findme.business.service.AddressExportData;
import com.geolink.findme.data.entity.Address;
import com.geolink.findme.presentation.dto.AddressExportResponse;
import com.geolink.findme.presentation.dto.AddressResponse;
import com.geolink.findme.presentation.dto.AddressSummaryResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddressWebMapperTest {

    private final AddressWebMapper mapper = new AddressWebMapper();

    private Address sample() {
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setUserId(UUID.randomUUID());
        address.setLabel("Domicile");
        address.setCountry("Cameroun");
        address.setCity("Douala");
        address.setDistrict("Akwa");
        address.setStreet("Rue 1");
        address.setHouseNumber("12");
        address.setPostalCode("0000");
        address.setLatitude(4.05);
        address.setLongitude(9.7);
        address.setPhotoUrl("http://files/photo.jpg");
        address.setStatus(AddressStatus.VERIFIED);
        address.setAddressCode("FM-AKWA-0001");
        address.setCountryCode("CM");
        address.setCreatedAt(Instant.now());
        address.setUpdatedAt(Instant.now());
        return address;
    }

    @Test
    void toResponse_transporte_tous_les_champs() {
        Address address = sample();

        AddressResponse response = mapper.toResponse(address);

        assertThat(response.id()).isEqualTo(address.getId());
        assertThat(response.userId()).isEqualTo(address.getUserId());
        assertThat(response.pays()).isEqualTo("Cameroun");
        assertThat(response.ville()).isEqualTo("Douala");
        assertThat(response.quartier()).isEqualTo("Akwa");
        assertThat(response.rue()).isEqualTo("Rue 1");
        assertThat(response.numero()).isEqualTo("12");
        assertThat(response.codePostal()).isEqualTo("0000");
        assertThat(response.latitude()).isEqualTo(4.05);
        assertThat(response.longitude()).isEqualTo(9.7);
        assertThat(response.urlPhoto()).isEqualTo("http://files/photo.jpg");
        assertThat(response.status()).isEqualTo(AddressStatus.VERIFIED);
        assertThat(response.addressCode()).isEqualTo("FM-AKWA-0001");
        assertThat(response.countryCode()).isEqualTo("CM");
    }

    @Test
    void toSummaryResponse_ne_transporte_que_le_resume() {
        Address address = sample();

        AddressSummaryResponse response = mapper.toSummaryResponse(address);

        assertThat(response.id()).isEqualTo(address.getId());
        assertThat(response.ville()).isEqualTo("Douala");
        assertThat(response.quartier()).isEqualTo("Akwa");
        assertThat(response.dateCreation()).isEqualTo(address.getCreatedAt());
    }

    @Test
    void toExportResponse_enveloppe_l_adresse_et_le_qr_code() {
        Address address = sample();
        AddressExportData data = new AddressExportData(address, "base64png");

        AddressExportResponse response = mapper.toExportResponse(data);

        assertThat(response.address().id()).isEqualTo(address.getId());
        assertThat(response.qrCodePngBase64()).isEqualTo("base64png");
    }
}
