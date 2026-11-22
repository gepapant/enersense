package com.gepapant.enersense.facility;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class FacilityService {

    private final FacilityRepository repository;

    public FacilityService(
        FacilityRepository repository
    ) {
        this.repository = repository;
    }

    public FacilityResponse create(
        FacilityCreateRequest request
    ) {
        Facility facility = new Facility(
            request.name().trim(),
            request.location().trim()
        );

        Facility saved =
            repository.save(facility);

        return FacilityResponse.from(saved);
    }

    public List<FacilityResponse> findAll() {
        return repository
            .findAllByOrderByIdAsc()
            .stream()
            .map(FacilityResponse::from)
            .toList();
    }
}