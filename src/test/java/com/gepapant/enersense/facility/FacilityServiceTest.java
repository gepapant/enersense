package com.gepapant.enersense.facility;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository repository;

    private FacilityService facilityService;

    @BeforeEach
    void setUp() {
        facilityService = new FacilityService(repository);
    }

    @Test
    void createShouldSaveFacilityAndReturnResponse() {

        FacilityCreateRequest request =
                new FacilityCreateRequest(
                        "Factory A",
                        "Thessaloniki"
                );

        when(repository.save(any(Facility.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        FacilityResponse response =
                facilityService.create(request);

        assertNotNull(response);

        verify(repository)
                .save(any(Facility.class));
    }

    @Test
    void createShouldTrimNameAndLocation() {

        FacilityCreateRequest request =
                new FacilityCreateRequest(
                        "  Factory A  ",
                        "  Thessaloniki  "
                );

        when(repository.save(any(Facility.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        FacilityResponse response =
                facilityService.create(request);

        assertNotNull(response);

        assertEquals(
                "Factory A",
                response.name()
        );

        assertEquals(
                "Thessaloniki",
                response.location()
        );

        verify(repository)
                .save(any(Facility.class));
    }

    @Test
    void findAllShouldReturnFacilitiesInRepositoryOrder() {

        Facility first =
                new Facility(
                        "Factory A",
                        "Thessaloniki"
                );

        Facility second =
                new Facility(
                        "Warehouse B",
                        "Athens"
                );

        when(repository.findAllByOrderByIdAsc())
                .thenReturn(
                        List.of(
                                first,
                                second
                        )
                );

        List<FacilityResponse> result =
                facilityService.findAll();

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Factory A",
                result.get(0).name()
        );

        assertEquals(
                "Warehouse B",
                result.get(1).name()
        );

        verify(repository)
                .findAllByOrderByIdAsc();
    }
}